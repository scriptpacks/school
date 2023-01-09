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
        {'source' -> '/libs/effects_utils.scl'},
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
import('effects_utils','_r_positive_effect');

_rispondi(int) -> _risposta(player(),int);
_set_time(200);
_n_ep(11);
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
    loop(_get_streak(),
        _r_positive_effect(player)
    );

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
    loop(_get_streak(),
        delete(global_checkpoints:(-1));
        if(length(global_checkpoints) > 0,
            global_pos:player = global_checkpoints:(-1),
            run(str('execute as %s in %s at @s run spreadplayers ~ ~ 10 100 under 100 false @s', player, player~'dimension'));
            global_pos:player = pos(player);
        )
    );

    _force_closing_screen(player)
);

// PITAGORA
global_terne = [[3,4,5],[5,12,13],[7,24,25],[8,15,17],[9,40,41],[11,60,61],[12,35,37],[13,84,85],[16,63,65],[20,21,29],[28,45,53],[33,56,65],[36,77,85],[39,80,89],[48,55,73],[65,72,97]];
global_terne_difficili = [[20,99,101],[60,91,109],[15,112,113],[44,117,125],[88,105,137],[17,144,145],[24,143,145],[51,140,149],[85,132,157],[119,120,169],[52,165,173],[19,180,181],[57,176,185],[104,153,185],[95,168,193],[28,195,197],[84,187,205],[133,156,205],[21,220,221],[140,171,221],[60,221,229],[105,208,233],[120,209,241],[32,255,257],[23,264,265],[96,247,265],[69,260,269],[115,252,277],[160,231,281],[161,240,289],[68,285,293]];

domanda_pitagora(player) -> (
    terna = if(global_difficolta>10,
        rand([...global_terne_difficili,...global_terne]),
        [...rand(global_terne)];
    );

    i = floor(rand(3));
    risposte = [terna:i];
    terna:i = '?';
    domanda = str('Completa la terna pitagorica:\n\n' +
                  '       •\n' +
                  '  %1$3s         %3$s\n'+
                  '       •       •\n'+
                  '         %2$3s\n\n',
        ... _shuffle([terna:0,terna:1]),
        terna:2
    );

    // R1
    r1 = if(rand(2),
        terna:(i+1)+terna:(i+2), // somma
        floor(sqrt(abs(terna:(i+1) * terna:(i+2)))) // radice prodotto
    );
    while(r1 == risposte:0 || r1 < 0, 127,
        r1 += if(rand(2),1,-1)*floor(rand(10))
    );
    if(r1 != risposte:0,
        risposte = [...risposte, r1]
    );
    // R2
    r2 = if(rand(2),
        terna:(i+1)+terna:(i+2), // somma quadrati
        floor(sqrt(terna:(i+1)+terna:(i+2))) // radice somma
    );
    while(r2 == risposte:0 || r2 == r1 || r2 < 0, 127,
        r2 += if(rand(2),1,-1)*floor(rand(10))
    );
    if(r2 != risposte:0 && r2 != r1,
        risposte = [...risposte, r2]
    );
    // R3
    r3 = if(rand(2),
        abs(terna:(i+1)^2-terna:(i+2)^2), // differenza quadrati
        abs(terna:(i+1)-terna:(i+2)) // differenza
    );
    while(r3 == risposte:0 || r3 == r1 || r3 == r1 || r3 < 0, 127,
        r3 += if(rand(2),1,-1)*floor(rand(10))
    );
    if(r3 != risposte:0 && r3 != r1 && r3 != r2,
        risposte = [...risposte, r3]
    );

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        domanda, // domanda
        risposte, // risposta
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);


// TEMPO senza picchiare mob
global_max_senza_picchiare = 15*20; // 15 secondi
if(player(),global_pos = pos(player()));
global_tick_senza_picchiare = 0;
passivo_per_troppo() -> global_tick_senza_picchiare > global_max_senza_picchiare;
global_checkpoints = [];
global_last_mob = [null,null];

// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        contdown=_countdown_string();
        if(contdown,
            _show_text_actionbar(player,contdown,'red'),
            
            tick = global_max_senza_picchiare - global_tick_senza_picchiare;
            if(global_last_mob:0,
                text = str('Picchia un mob che non sia %s! %.02f',global_last_mob:1,max(0,tick/20)),
                text = str('Picchia un mob! %.02f',max(0,tick/20))            
            );
            _show_text_actionbar(player,text,'gold')
        );

        if(_valid_time() && passivo_per_troppo(),
            _stop_countdown();
            global_pos = pos(player);
            global_tick_senza_picchiare = 0;
            schedule(0, 'domanda_pitagora', player)
        );
        if(_valid_time(),
            global_tick_senza_picchiare += 1;
        )
    )    
);

// reset conteggio
__on_player_attacks_entity(player, entity)->(
    if(entity ~ 'type' != global_last_mob:0,
        global_last_mob:0 = entity~'type';
        global_last_mob:1 = str(entity);
        global_tick_senza_picchiare = 0
    )
);

__on_player_connects(player)-> global_tick_senza_picchiare = -1000;

__on_player_disconnects(player, reason)-> (
    _force_closing_screen(player);
);
__on_close() -> (
    for(player('all'), _force_closing_screen(_));
);
_force_closing_screen(player());
