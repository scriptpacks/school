__config()->{
    'commands'->{
        'risposta <int>' -> '_rispondi',
        'difficolta <int>' -> _(b) -> global_difficolta = b
    },
    'libraries' -> [
        {'source' -> '/libs/school.scl'},
        {'source' -> '/libs/countdown.scl'},
        {'source' -> '/libs/title_utils.scl'},
        {'source' -> '/libs/array_utils.scl'},
        {'source' -> '/libs/totems.scl'},
        {'source' -> '/libs/icons.scl'},
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
    '_countup_string',
    '_set_time',
    '_valid_time',
    '_time'
);
import('title_utils',
    '_show_text_actionbar'
);
import('icons','_icon_item_json');
import('streak','_add_streak','_get_streak');
import('array_utils','_shuffle');
import('totems',
    '_on_player_uses_item',
    '_on_player_dies',
    '_on_player_respawns',
    '_on_player_rides',
    '_on_player_interacts_with_entity',
    '_on_player_swings_hand'
);

_rispondi(int) -> _risposta(player(),int);
_set_time(global_time = 10*20);
_n_ep(13);
global_calcolatrice = true;

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
    _set_time(max(200,global_time - global_difficolta*10));

    // RICOMPENSA
    signal_event('ricompensa', player, [player, r]);

    _force_closing_screen(player)
);

_penalita(player, r, corretta) -> (
    particle('wax_on', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#ffdd00 Accidenti! La risposta corretta era '+corretta));
    _start_countdown();
    
    // STREAK
    _add_streak(false);
    global_difficolta = max(0, global_difficolta-1);
    _set_time(max(200,global_time - global_difficolta*10));
    
    // PENALITA'
    signal_event('penitenza', player, [player, r, corretta]);

    _force_closing_screen(player)
);

// NOTAZIONE SCIENTIFICA
domanda_notazione_scientifica(player) -> (
    es = floor(rand(4));
    if(
        es == 0, domanda_1(player),
        es == 1, domanda_3(player),
        domanda_2(player)
    )
);
global_apici = ['⁰','¹','²','³','⁴','⁵','⁶','⁷','⁸','⁹'];
notazione_scientifica(s,e) -> str('%.3f · 10%s',s,
    join('',map(split(str(e)),
        if(_ == '-', '⁻',
           _ == 'n', 'ⁿ',
           global_apici:number(_)
        )
    ))
);
domanda_1(player) -> (
    s = floor(rand(1000+global_difficolta*100));
    while(abs(s) >= 10, 100, s = s/10);
    e = (-1)^floor(rand(2))*floor(rand(10+global_difficolta)+1);
    print(s);
    print(e);

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        'Il numero '+notazione_scientifica(s,e)+' è grande o piccolo?', // domanda
        if(e>0,['grande','piccolo'],['piccolo','grande']), // risposte
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);
domanda_2(player) -> (
    s = (-1)^floor(rand(2))*floor(rand(1000+global_difficolta*100));
    while(abs(s) >= 10, 100, s = s/10);
    e = (-1)^floor(rand(2))*min(35,floor(rand(10+global_difficolta)+1));

    genera_domanda_libera(player, global_calcolatrice,
        'A quanto corrisponde il seguente numero?\n'+notazione_scientifica(s,e), // domanda
        s*10^e, // risposta
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);
domanda_3(player) -> (
    s = (-1)^floor(rand(2))*floor(rand(1000+global_difficolta*100));
    while(abs(s) >= 10, 100, s = s/10);
    e = (-1)^floor(rand(2))*min(35,floor(rand(10+global_difficolta)+1));

    genera_domanda_libera(player, global_calcolatrice,
        'Quale è l\'esponente "n" della seguente ugualianza?\n'+
        (s*10^e)+'\n => ' +notazione_scientifica(s,'n'), // domanda
        e, // risposta
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

handle_event('domanda', _(player) -> (
    if(_valid_time(),
        _stop_countdown();
       schedule(10, 'domanda_notazione_scientifica', player);
    );
));

// CLOSE
__on_player_disconnects(player, reason)-> (
    _force_closing_screen(player);
);
__on_close() -> (
    for(player('all'), _force_closing_screen(_));
);
__on_start() -> (
    _force_closing_screen(player());
);

// TOTEM
__on_player_uses_item(player, item_tuple, hand)->_on_player_uses_item(player, item_tuple, hand);
__on_player_dies(player)->_on_player_dies(player);
__on_player_respawns(player)->_on_player_respawns(player);
__on_player_rides(player, forward, strafe, jumping, sneaking)->_on_player_rides(player, forward, strafe, jumping, sneaking);
__on_player_interacts_with_entity(player, entity, hand)->_on_player_interacts_with_entity(player, entity, hand);
__on_player_swings_hand(player, hand)->_on_player_swings_hand(player, hand);
