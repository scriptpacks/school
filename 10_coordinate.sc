__config()->{
    'commands'->{
        'risposta <int>' -> '_rispondi',
        'difficolta <bool>' -> _(b) -> global_difficolta = b
    },
    'libraries' -> [
        {'source' -> '/libs/school.scl'},
        {'source' -> '/libs/countdown.scl'},
        {'source' -> '/libs/title_utils.scl'},
        {'source' -> '/libs/array_utils.scl'},
        {'source' -> '/libs/streak.scl'}
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
    '_countdown_string',
    '_set_time',
    '_valid_time',
    '_time'
);
import('title_utils',
    '_show_text_actionbar'
);
import('streak','_add_streak','_get_streak');
import('array_utils','_shuffle');

_rispondi(int) -> _risposta(player(),int);
_set_time(200);
_n_ep(10);
global_calcolatrice = false;

// RICOMPENSA
_ricompensa(player, r) -> (
    particle('happy_villager', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#00ff00 Esattamente! Ecco a te un fantastico premio!'));
    _start_countdown();

    // STREAK
    _add_streak(true);
    if(_get_streak()>=10,
        print(player('all'),format(str('#00ff00 %s ha risposto correttamente a '+_get_streak()+' domande consecutive!',player)));
        global_difficolta += 1;
    );

    // RICOMPENSA
    cute_mobs = ['allay','dolphin','axolotl','sniffer','armadillo'];
    schedule(1, _(outer(player)) -> spawn(rand(cute_mobs),pos(player)));

    schedule(0, _(outer(player)) -> _force_closing_screen(player))
);
_penalita(player, r, corretta) -> (
    particle('wax_on', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#ffdd00 Accidenti! La risposta corretta era '+corretta));
    _start_countdown();
    
    // STREAK
    _add_streak(false);
    global_difficolta = max(0, global_difficolta-1);
    
    // PENALITA'
    schedule(1, _(outer(player)) -> (
        modify(player, 'move', global_offset);
        destroy(pos(player), -1);
        set(pos(player),'light')
    ));

    schedule(0, _(outer(player)) -> _force_closing_screen(player))
);

// COORDINATE
domanda_coordinate(player) -> (
    coordinate = if(global_difficolta>10,
        map(pos(player()), round(_)),
        map(range(3), if(rand(2),1,-1)*floor(rand(26)))
    );
    global_offset = map(range(3), if(rand(2),1,-1)*floor(rand(26+global_difficolta*10))); 
    // lista di 3 numeri da -25 a 25 +- difficoltà*10
    
    es = rand(3);
    [domanda, risposte] = if(
        es < 1, domanda_coordinate_1(player,coordinate,global_offset),
        es < 2, domanda_coordinate_2(player,coordinate,global_offset),
                domanda_coordinate_3(player,coordinate,global_offset)
    );
    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        domanda, // domanda
        risposte, // risposta
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);

domanda_coordinate_1(player, coordinate, offset) -> (
    offset_map = reduce(['X','Y','Z'], _a:_ = offset:_i; _a, {});
    pairs = _shuffle(pairs(offset_map));
    args = reduce(pairs, [..._a, ..._],[]);
    domanda = ['Ti trovi a coordinate\n'
        {'text'->str('(%d, %d, %d)\n\n',... coordinate),'bold'->true},
        str('Se ti muovi di\n'+
        '%2$s blocchi sull\'asse delle %1$s, '+
        '%4$s sull\'asse delle %3$s, '+
        '%6$s su quella delle %5$s\n\n'+
        'A che coordinate ti troverai?',
        , args)
    ];
    risposta = coordinate + offset;
    r1 = coordinate - offset;
    while(r1 == risposta, 127,
        r1:floor(rand(3)) += if(rand(2),1,-1)*floor(rand(10))
    );
    r2 = coordinate + offset;
    while(r2 == risposta || r2 == r1, 127,
        r2:floor(rand(3)) += if(rand(2),1,-1)*floor(rand(10))
    );
    possibili_risposte = [risposta];
    if(r1 != risposta,
        possibili_risposte = [...possibili_risposte, r1]
    );
    if(r2 != risposta && r2 != r1,
        possibili_risposte = [...possibili_risposte, r2]
    );
    [domanda, map(possibili_risposte,'('+join(', ',_)+')')]
);

domanda_coordinate_2(player, coordinate, offset) -> (
    arrivo = coordinate + offset;
    es = floor(rand(3));
    offset_map = reduce(['X','Y','Z'], _a:_ = offset:_i; _a, {});
    pairs = _shuffle(pairs(offset_map));
    args = reduce(pairs, [..._a, ..._],[]);
    domanda = ['Ti trovi a coordinate\n'
        {'text'->str('(%d, %d, %d)\n\n',... arrivo),'bold'->true},
        str('Se prima ti eri mosso\n'+
        '%2$s blocchi sull\'asse delle %1$s, '+
        '%4$s sull\'asse delle %3$s, '+
        '%6$s su quella delle %5$s\n\n'+
        'A che coordinate ti trovavi prima?',
        , args)
    ];
    risposta = coordinate;
    r1 = arrivo + offset;
    while(r1 == risposta, 127,
        r1:floor(rand(3)) += if(rand(2),1,-1)*floor(rand(10))
    );
    r2 = coordinate + 0; //copy
    while(r2 == risposta || r2 == r1, 127,
        r2:floor(rand(3)) += if(rand(2),1,-1)*floor(rand(10))
    );
    possibili_risposte = [risposta];
    if(r1 != risposta,
        possibili_risposte = [...possibili_risposte, r1]
    );
    if(r2 != risposta && r2 != r1,
        possibili_risposte = [...possibili_risposte, r2]
    );
    [domanda, map(possibili_risposte,'('+join(', ',_)+')')]
);

    
domanda_coordinate_3(player, coordinate, offset) -> (
    arrivo = coordinate + offset;
    es = floor(rand(3));
    domanda = ['Ti trovi a coordinate\n'
        {'text'->str('(%d, %d, %d)\n\n',... coordinate),'bold'->true},
        'Vuoi arrivare a coordinate\n',
        {'text'->str('(%d, %d, %d)\n\n',... arrivo),'bold'->true},
        str('Di quanto dovrai muoverti sull\'asse %s?\n', ['X','Y','Z']:es)
    ];
    risposta = offset:es;
    r2 = if(rand(2),1,-1)*offset:((es+if(rand(2),1,2))%3);
    r1 = if(rand(2), -risposta, -r2);
    while(r1 == risposta, 127,
        r1 += if(rand(2),1,-1)*floor(rand(10))
    );
    while(r2 == risposta || r2 == r1, 127,
        r2 += if(rand(2),1,-1)*floor(rand(10))
    );
    possibili_risposte = [risposta];
    if(r1 != risposta,
        possibili_risposte = [...possibili_risposte, r1]
    );
    if(r2 != risposta && r2 != r1,
        possibili_risposte = [...possibili_risposte, r2]
    );
    [domanda, possibili_risposte]
);

// TEMPO FERMO
global_max_fermo = 100;
approssima(pos) -> map(pos, floor(_*100)/100);
if(player(), global_pos = approssima(pos(player())));
global_tick_fermo = 0;
fermo_da_troppo() -> global_tick_fermo > global_max_fermo;

// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        contdown=_countdown_string();
        if(contdown,
            _show_text_actionbar(player,contdown,'red'),
            
            tick = global_max_fermo-global_tick_fermo;
            text = str('Non fermarti! %.02f',max(0,tick/20));
            _show_text_actionbar(player,text,'gold')
        );

        if(_valid_time() && fermo_da_troppo(),
            _stop_countdown();
            global_quanto = amount;
            schedule(0, 'domanda_coordinate', player)
        );
        if(_valid_time() && (pos = approssima(pos(player()))) == global_pos,
            global_tick_fermo += 1,
            global_tick_fermo = 0
        );
        global_pos = pos
    )    
);

__on_player_connects(player)-> global_tick_fermo = -800;

__on_player_disconnects(player, reason)-> (
    _force_closing_screen(player);
);
__on_close() -> (
    _force_closing_screen(player());
);
_force_closing_screen(player());
