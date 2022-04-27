__config()->{
    'commands'->{
        'risposta <int>' -> '_rispondi',
        'difficolta <bool>' -> _(b) -> global_difficolta = b
    },
    'libraries' -> [
        {'source' -> '/libs/school.scl'},
        {'source' -> '/libs/countdown.scl'},
        {'source' -> '/libs/title_utils.scl'},
        {'source' -> '/libs/streak.scl'},
        {'source' -> '/libs/attribute_utils.scl'}
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
import('attribute_utils','_add_max_health');

_rispondi(int) -> _risposta(player(),int);
_set_time(100);
_n_ep(9);
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

    // RICOMPENSA
    _add_max_health(player, global_quanto);

    _force_closing_screen(player)
);
_penalita(player, r, corretta) -> (
    particle('wax_on', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#ffdd00 Accidenti! La risposta corretta era '+corretta));
    _start_countdown();
    
    // STREAK
    _add_streak(false);
    global_difficolta = max(0, global_difficolta-1);
    
    // PENALITA'
    _add_max_health(player, -global_quanto);

    _force_closing_screen(player)
);

// FATTORIALE
domanda_fattoriale(player) -> (
    base_1 = floor(rand(6+global_difficolta/3));
    base_2 = floor(rand(base_1+1));
    operazione = floor(rand(4));
    if(operazione == 0 && base_1 < 7, // più
        segno_operazione = '+';
        global_risposta_corretta = fact(base_1) + fact(base_2);
    , operazione == 1 && base_1 < 8, // meno
        segno_operazione = '-';
        global_risposta_corretta = fact(base_1) - fact(base_2);
    , operazione == 2 && base_1 < 6, // per
        segno_operazione = '·';
        global_risposta_corretta = fact(base_1) * fact(base_2);
    ,  // diviso
        segno_operazione = ':';
        global_risposta_corretta = fact(base_1) / fact(base_2);
    );

    r1 = fact(base_1)-fact(ceil(rand(base_1)));
    if(r1 == global_risposta_corretta,
        r1 += if(rand(2),1,-1)*(floor(rand(10))+1)
    );

    r2 = floor(fact(base_2)*fact(ceil(rand(base_1)))^if(rand(2),1,-1));
    while(r2 == global_risposta_corretta || r2 == r1 || !r2, 127,
        r2 += if(rand(2),1,-1)*floor(rand(10))
    );

    risposte = [global_risposta_corretta,r1];
    if(r2 != global_risposta_corretta && r2 != r1,
        risposte = [...risposte, r2]
    );

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        str('Quanto vale %d! %s %d! ?',
            base_1,
            segno_operazione,
            base_2
        ), // domanda
        risposte, // risposta
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

__on_player_deals_damage(player, amount, entity) ->
if(_valid_time(),
    _stop_countdown();
    global_quanto = amount;
    schedule(0, 'domanda_fattoriale', player)
);
__on_player_takes_damage(player, amount, source, source_entity) -> 
if(_valid_time(),
    _stop_countdown();
    global_quanto = amount;
    schedule(0, 'domanda_fattoriale', player)
);

__on_player_disconnects(player, reason)-> (
    _force_closing_screen(player);
);
__on_close() -> (
    _force_closing_screen(player());
);
