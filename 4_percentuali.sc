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
    '_valid_time'
);
import('title_utils','_show_text_actionbar');
import('streak','_add_streak','_get_streak');
import('math_utils','_mcd');

_rispondi(int) -> _risposta(player(),int);
_set_time(100);
_n_ep(4);
global_calcolatrice = true;

// AUX
_modifica_inventario(player, m) -> (
    for([range(inventory_size(player))],
        if(rand(100)< 4 * (_get_streak()+2),
            item_tuple = inventory_get(player, _i);
            if(item_tuple,
                [item, count, nbt] = item_tuple;
                count = min(ceil(count * (100+m*global_percentuale)/100),1073741823);
                if(nbt:'Damage', nbt:'Damage'+= -global_percentuale*m);
                inventory_set(player, _i, count, item, nbt)
            )
        )
    );
);

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
    if(global_percentuale,
        _modifica_inventario(player, 1)
    );

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
    if(global_percentuale,
        _modifica_inventario(player, -1)
    );

    _force_closing_screen(player)
);

// PERCENTUALI
global_decimali = 0;
global_primi = [1,2,3,5,7,11,13,17,19];

domanda_percentuali(player) -> (
    tipo = floor(rand(3));
    if(
        tipo == 0, domanda_percentuale_di(player),
        tipo == 1, domanda_percentuale_somma(player),
        tipo == 2, domanda_percentuale_sottrazione(player)
    )
);

domanda_percentuale_di(player) ->
genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
    global_percentuale = floor(rand(101));
    if(global_decimali,
        numero = floor(rand(101));
        mcd = floor(rand(20));
    , // else
        if(global_percentuale <= 1,
            mcd = 1;
            numero = floor(rand(201)),
            mcd = _mcd(100,global_percentuale);
            numero = 100/mcd;
        );
        if(numero < 40,
            numero = numero*(floor(rand(floor(rand(global_primi))+1))+1),
            numero = numero*(floor(rand(floor(rand(8))+1))+1);
        );
    );
    
    str('Quanto è il %d%% di %d?', global_percentuale, numero), // domanda
    percentuale = global_percentuale;
    global_percentuale += 100;

    global_risposta_corretta = percentuale * numero / 100;

    if(global_decimali,
        r1 = global_risposta_corretta + floor((rand(10)+1)*100)/100;
        r2 = global_risposta_corretta + floor((rand(10)+1)*100)/100;
    , // else
        r1 = global_risposta_corretta + (floor(rand(10))+1);
        r2 = global_risposta_corretta + (floor(rand(10))+1);
        if(!rand(6), r1 = mcd*(floor(rand(floor(rand(20))+1))+1));
        if(!rand(6), r2 = mcd*(floor(rand(floor(rand(20))+1))+1));
        r1 = floor(r1);
        r2 = floor(r2);
    );

    if(r1 == global_risposta_corretta,
        r1 += (floor(rand(10))+1)
    );
    while(r2 == global_risposta_corretta || r2 == r1, 127,
        r2 += (floor(rand(10))+1)
    );

    possibili_risposte = [global_risposta_corretta,r1];
    if(r2 != global_risposta_corretta && r2 != r1,
        possibili_risposte += r2
    );

    possibili_risposte, // risposte
    _(p,r)->_ricompensa(p,r), // ricompensa
    _(p,r,c)->_penalita(p,r,c) // penalità
);

domanda_percentuale_somma(player) ->
genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
    global_percentuale = floor(rand(101));
    if(global_decimali,
        numero = floor(rand(101));
        mcd = floor(rand(20));
    , // else
        if(global_percentuale <= 1, // divisione per 0 ??
            mcd = 1;
            numero = floor(rand(201)),
            mcd = _mcd(100-global_percentuale,global_percentuale);
            numero = 100/mcd;
        );
        if(numero < 40,
            numero = numero*(floor(rand(floor(rand(global_primi))+1))+1),
            numero = numero*(floor(rand(floor(rand(8))+1))+1);
        );
    );

    str('Se a %d togliamo il %d%%?', numero, global_percentuale), // domanda
    percentuale=100-global_percentuale;

    global_risposta_corretta = percentuale * numero / 100;

    if(global_decimali,
        r1 = global_risposta_corretta + floor((rand(10)+1)*100)/100;
        r2 = global_risposta_corretta + floor((rand(10)+1)*100)/100;
    , // else
        r1 = global_risposta_corretta + (floor(rand(10))+1);
        r2 = global_risposta_corretta + (floor(rand(10))+1);
        if(!rand(6), r1 = mcd*(floor(rand(floor(rand(20))+1))+1));
        if(!rand(6), r2 = mcd*(floor(rand(floor(rand(20))+1))+1));
        r1 = floor(r1);
        r2 = floor(r2);
    );

    if(r1 == global_risposta_corretta,
        r1 += (floor(rand(10))+1)
    );
    while(r2 == global_risposta_corretta || r2 == r1, 127,
        r2 += (floor(rand(10))+1)
    );

    possibili_risposte = [global_risposta_corretta,r1];
    if(r2 != global_risposta_corretta && r2 != r1,
        possibili_risposte += r2
    );

    possibili_risposte, // risposte
    _(p,r)->_ricompensa(p,r), // ricompensa
    _(p,r,c)->_penalita(p,r,c) // penalità
);
domanda_percentuale_sottrazione(player) ->
genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
    global_percentuale = floor(rand(101));
    if(global_decimali,
        numero = floor(rand(101));
        mcd = floor(rand(20));
    , // else
        mcd = _mcd(100+global_percentuale,global_percentuale);
        numero = 100/mcd;
    );

    if(numero < 40,
        numero = numero*(floor(rand(floor(rand(global_primi))+1))+1),
        numero = numero*(floor(rand(floor(rand(8))+1))+1);
    );

    str('Se a %d aggiungiamo il %d%%?', numero, global_percentuale), // domanda
    percentuale=global_percentuale+100;

    global_risposta_corretta = percentuale * numero / 100;

    if(global_decimali,
        r1 = global_risposta_corretta + floor((rand(10)+1)*100)/100;
        r2 = global_risposta_corretta + floor((rand(10)+1)*100)/100;
    , // else
        r1 = global_risposta_corretta + (floor(rand(10))+1);
        r2 = global_risposta_corretta + (floor(rand(10))+1);
        if(!rand(6), r1 = mcd*(floor(rand(floor(rand(20))+1))+1));
        if(!rand(6), r2 = mcd*(floor(rand(floor(rand(20))+1))+1));
        r1 = floor(r1);
        r2 = floor(r2);
    );

    if(r1 == global_risposta_corretta,
        r1 += (floor(rand(10))+1)
    );
    while(r2 == global_risposta_corretta || r2 == r1, 127,
        r2 += (floor(rand(10))+1)
    );

    possibili_risposte = [global_risposta_corretta,r1];
    if(r2 != global_risposta_corretta && r2 != r1,
        possibili_risposte += r2
    );

    possibili_risposte, // risposte
    _(p,r)->_ricompensa(p,r), // ricompensa
    _(p,r,c)->_penalita(p,r,c) // penalità
);


// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        text=_countdown_string() || '';
        _show_text_actionbar(player,text,'red');
    );
);

__on_player_disconnects(player, reason)-> (
    _force_closing_screen(player);
);
__on_close() -> (
    _force_closing_screen(player());
);

__on_statistic(player, category, item, count) ->
if(category == 'used' && (item~'_pickaxe$'||item~'_axe$'||item~'_hoe$'||item~'_shovel$'),
    if(_valid_time(),
        _stop_countdown();
        schedule(0, 'domanda_percentuali', player);
    );
);
