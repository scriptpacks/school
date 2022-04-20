__config()->{
    'commands'->{
        'risposta <int>' -> '_rispondi',
        'decimali <bool>' -> _(b) -> global_decimali = b
    },
    'libraries' -> [
        {'source' -> '/libs/school.scl'},
        {'source' -> '/libs/countdown.scl'},
        {'source' -> '/libs/title_utils.scl'},
        {'source' -> '/libs/array_utils.scl'},
        {'source' -> '/libs/streak.scl'},
        {'source' -> '/libs/math_utils.scl'}
    ],
    'resources' -> [
        {'source' -> '/resources/trash_item.json'},
        {'source' -> '/resources/treasure_item.json'}
    ]
};

import('school',
    'genera_domanda_libera',
    'genera_domanda_multipla',
    '_n_ep',
    '_force_closing_screen',
    '_risposta',
    '_unfreeze'
);
import('countdown',
    '_start_countdown',
    '_stop_countdown',
    '_countup_string',
    '_countdown_string',
    '_set_time',
    '_valid_time',
    '_time_running',
    '_time'
);
import('title_utils',
    '_show_text_actionbar',
    '_show_block_title',
    '_show_json_actionbar'
);
import('streak','_add_streak','_get_streak');
import('math_utils','_mcd');
import('array_utils','_shuffle');

_rispondi(int) -> _risposta(player(),int);
_set_time(100);
_n_ep(8);
global_calcolatrice = true;

create_datapack(system_info('app_name'), {
    'data' -> { 'scarpet' -> { 'loot_tables' -> { system_info('app_name') -> { 
        'trash_item.json' ->  read_file('trash_item', 'json'),
        'treasure_item.json' -> read_file('treasure_item', 'json')
    } } } }
});

// RICOMPENSA
_ricompensa(player, r) -> (
    particle('happy_villager', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#00ff00 Esattamente! Ecco a te un fantastico premio!'));
    _start_countdown();

    // STREAK
    _add_streak(true);
    if(_get_streak()>=10,
        print(player('all'),format(str('#00ff00 %s ha risposto correttamente a '+_get_streak()+' domande consecutive!',player)));
        global_decimali += 3;
    );

    // RICOMPENSA
    run(str('execute in %s run loot spawn %f %f %f loot scarpet:%s/treasure_item',
        player~'dimension',
        ... (pos(player)+[0,player~'eye_height',0]),
        system_info('app_name')
    ));

    _force_closing_screen(player)
);
_penalita(player, r, corretta) -> (
    particle('wax_on', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#ffdd00 Accidenti! La risposta corretta era '+corretta));
    _start_countdown();
    
    // STREAK
    _add_streak(false);
    global_decimali = max(0, global_decimali-1);
    
    // PENALITA'
    loop(inventory_size(player),
        if(rand(4),
            drop_item(player, _, if(rand(3),0,rand(64)))
        )
    );
    run(str('execute in %s run loot spawn %f %f %f loot scarpet:%s/trash_item',
        player~'dimension',
        ... (pos(player)+[0,player~'eye_height',0]),
        system_info('app_name')
    ));

    _force_closing_screen(player)
);

_impagina_numeri(list) -> (
    righe = [];
    for(list,
        riga = if(_i%2, righe:floor(_i/2), '');
        loop(rand(8)+2, riga += ' ');
        riga += _;
        righe:floor(_i/2) = riga;
    );
    righe = _shuffle(righe);
    join('\n',righe);
);

// MEDIA
domanda_media(player) -> (
    lista = if(global_decimali,
        map(range(floor(rand(7)+5)),floor(rand(1000))/100),
        map(range(floor(rand(7)+5)),floor(rand(100)))
    );
    sum = reduce(lista,_a+_,0);
    len = length(lista);
    if(sum%len,
        len += 1;
        lista += len-sum%len;
        sum += lista:(-1);
    );
    media = sum/len;
    lista = _shuffle(lista);

    genera_domanda_libera(player, global_calcolatrice,
        str('Quanto vale la media dei seguenti numeri?\n\n%s', _impagina_numeri(lista)), // domanda
        media, // risposta
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);

// MODA
domanda_moda(player) -> (
    lista = if(global_decimali,
        map(range(floor(rand(3)+4)),floor(rand(1000))/100),
        map(range(floor(rand(3)+4)),floor(rand(100)))
    );
    loop(rand(8)+rand(4),
        lista += rand(lista)
    );
    occorrenze = {};
    for(lista, occorrenze:_ += 1);
    max_occorrenze = max(values(occorrenze));
    max_occorrenze_list = filter(keys(occorrenze),occorrenze:_ == max_occorrenze);
    if(length(max_occorrenze_list)>1,
        moda = rand(max_occorrenze_list);
        lista += moda
    , // else
        moda = max_occorrenze_list:0;
    );
    
    lista = _shuffle(lista);

    genera_domanda_libera(player, global_calcolatrice,
        str('Quanto vale la moda dei seguenti numeri?\n\n%s', _impagina_numeri(lista)), // domanda
        moda, // risposta
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);


// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        text=_countdown_string() || '';
        _show_text_actionbar(player,text,'red');
    );
);

__on_player_jumps(player)->
if(_valid_time(),
    _stop_countdown();
    global_chest = block;
    if(rand(2),
        schedule(0, 'domanda_media', player),
        schedule(0, 'domanda_moda', player)
    )
);

__on_close() -> (
    _force_closing_screen(player());
);
