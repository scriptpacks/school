__config()->{
    'commands'->{
        'risposta <int>' -> '_rispondi'
    },
    'libraries' -> [
        {'source' -> '/libs/school.scl'},
        {'source' -> '/libs/proporzioni.scl'},
        {'source' -> '/libs/countdown.scl'},
        {'source' -> '/libs/title_utils.scl'},
        {'source' -> '/libs/array_utils.scl'},
        {'source' -> '/libs/inventory_utils.scl'},
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
import('proporzioni',
    '_s_proporzione',
    '_proporzione_casuale'
);
import('countdown',
    '_start_countdown',
    '_stop_countdown',
    '_countdown_string',
    '_set_time',
    '_valid_time'
);
import('array_utils','_shuffle');
import('title_utils','_show_text_actionbar');
import('streak','_add_streak','_get_streak');

_rispondi(int) -> _risposta(player(),int);
_set_time(100);
_n_ep(2);

// RICOMPENSA
_ricompensa(player, r, entity) -> (
    particle('happy_villager', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#00ff00 Esattamente! Ecco a te un fantastico premio!'));
    _start_countdown();

    // STREAK
    _add_streak(true);
    if(_get_streak()>=10,
        print(player('all'),format(str('#00ff00 %s ha risposto correttamente a '+_get_streak()+' domande consecutive!',player)));
    );

    // RICOMPENSA
    if(entity,modify(entity, 'pickup_delay', 0));
    if(global_item,
        nbt = nbt(str('{Item:{id:"%s",Count:%d}}',global_item));
        nbt:'Owner' = player~'nbt':'UUID';
        spawn('item', pos(player), nbt)
    );

    _force_closing_screen(player)
);
_penalita(player, r, corretta, entity) -> (
    particle('wax_on', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#ffdd00 Accidenti! La risposta corretta era '+corretta));
    _start_countdown();
    
    // STREAK
    _add_streak(false);
    
    // PENALITA'
    if(global_item,
        run(str('clear %s %s %d', player, ... global_item));
    );

    _force_closing_screen(player)
);

// PROPORZIONI
domanda_proporzioni(player) ->
genera_domanda_multipla(player, false, str('/%s risposta ',system_info('app_name')),
    proporzione = _proporzione_casuale();
    index = floor(rand(4));
    r = proporzione:index;
    proporzione:index = null;
    _s_proporzione(proporzione)+'\n', // domanda
    if(index == 0,
        r1 = proporzione:1 * proporzione:3 / proporzione:2;
        r2 = proporzione:2 * proporzione:3 / proporzione:1,
       index == 1,
        r1 = proporzione:0 * proporzione:2 / proporzione:3;
        r2 = proporzione:2 * proporzione:3 / proporzione:0,
       index == 2,
        r1 = proporzione:1 * proporzione:3 / proporzione:0;
        r2 = proporzione:0 * proporzione:1 / proporzione:3,
       index == 3,
        r1 = proporzione:0 * proporzione:2 / proporzione:1;
        r2 = proporzione:0 * proporzione:1 / proporzione:2,
    );
    if(!rand(6), r1 = (floor(rand(25))+1));
    if(!rand(6), r2 = (floor(rand(25))+1));
    r1 = floor(r1);
    r2 = floor(r2);
    if(r1 == r,
        r1 += if(!rand(2),-1,1)*(floor(rand(10))+1)
    );
    while(r2 == r || r2 == r1, 127,
        r2 += if(!rand(2),-1,1)*(floor(rand(10))+1)
    );
    copy([r,r1,r2]), // risposte
    _(p,r,outer(entity))->_ricompensa(p,r,entity), // ricompensa
    _(p,r,c,outer(entity))->_penalita(p,r,c,entity) // penalità
);


// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        if((text=_countdown_string())!='', color='red',
           text=''; color='white'
        );
        _show_text_actionbar(player,text,color);
    );
);

__on_player_disconnects(player, reason)-> (
    _force_closing_screen(player);
);
__on_close() -> (
    _force_closing_screen(player());
);

__on_statistic(player, category, item, count) ->
if(category == 'crafted',
    global_item = [item,count];
    if(_valid_time(), 
        schedule(0, 'domanda_proporzioni', player);
    );
);
