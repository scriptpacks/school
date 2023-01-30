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
_n_ep(15);
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

// FREQUENZA
global_misure = [
    ['secondi', 1, 'secondo'],
    ['minuti', 60, 'minuto'],
    ['ore', 3600, 'ora'],
    ['giorni', 86400, 'giorno'],
    ['settimane', 604800, 'settimana'],
    ['mesi', 2592000, 'mese'],
    ['anni', 31536000, 'anno']
];
// FREQUENZA
domanda_frequenza(player) -> (
    // Ogni ..n.. secondi , corrisponde a circa ..m.. volte al minuto
    // Ogni ..n.. secondi , corrisponde a ogni ..m.. minuti
    da_index = rand(length(global_misure)-2);
    misura_da = global_misure:da_index;
    a_index = da_index + rand(length(global_misure)-da_index-1)+1;
    if (a_index == da_index, a_index = a_index + 1);
    misura_a = global_misure:a_index;

    // numero a caso di secondi
    periodo_da = floor(rand(100*global_difficolta+100))+2;
    // secondi * 1 / 60 = minuti
    periodo_a = floor(periodo_da * misura_da:1 / misura_a:1);

    if(periodo_a < 1,
       
        risposta = str('ogni circa %.2f %s',periodo_a,misura_a:0);
    ,
        
        frequenza_a = 1/periodo_a;
        risposta = str('circa %.2f volte al %s',frequenza_a,misura_a:2);
    );

    if(rand(2),
        domanda = 'Ogni '+str('%.2f %s',periodo_da,misura_da:0)+', corrisponde a '+risposta+'?\n';
        risposte = ['Vero', 'Falso'],

        errata = if(rand(2),
            str('circa %.2f volte al %s',rand(1000)/100,misura_da:2),
            str('ogni circa %.2f %s',rand(1000)/100,misura_da:0)
        );
        domanda = 'Ogni '+str('%.2f %s',periodo_da,misura_da:0)+', corrisponde a circa '+errata+'?\n';
        if(risposta == errata,
            risposte = ['Vero', 'Falso'],
            risposte = ['Falso', 'Vero']
        )
    );

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        domanda, // domanda
        risposte, // risposte
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
       schedule(10, 'domanda_frequenza', player);
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
