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
        {'source' -> '/libs/math_utils.scl'},
        {'source' -> '/libs/blocks_utils.scl'},
        {'source' -> '/libs/icons.scl'}
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
import('blocks_utils','_surface_block','_rand_pos_around');
import('icons','_icon_item_json');

_rispondi(int) -> _risposta(player(),int);
_set_time(100);
_n_ep(7);
global_calcolatrice = true;

_modify_inventory(player, m) -> (
    for([range(inventory_size(player))],
        if(rand(100)< 4 * _get_streak(),
            item_tuple = inventory_get(player, _i);
            if(item_tuple,
                [item, count, nbt] = item_tuple;
                count = min(ceil(count * (100+m*global_percentuale)/100),999999);
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
        _modify_inventory(player,1)
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
        _modify_inventory(player,-1)
    );

    _force_closing_screen(player)
);

// PERCENTUALI
domanda_percentuali(player) -> (
    es = floor(rand(3));
    if(
        es == 0,
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
            domanda = str('Quanto è il %d%% di %d ?',global_percentuale,numero);
            percentuale = global_percentuale;
            global_percentuale += 100,
        es == 1, // elif
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

            domanda = str('Se a %d togliamo il %d%%?',numero,global_percentuale);
            percentuale=100-global_percentuale;
        , // else
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

            domanda = str('Se a %d aggiungiamo il %d%%?',numero,global_percentuale);
            percentuale=global_percentuale+100;
    );
    risposta = percentuale * numero / 100;

    if(global_decimali,
        r1 = risposta + if(!rand(2),-1,1)*floor((rand(10)+1)*100)/100;
        r2 = risposta + if(!rand(2),-1,1)*floor((rand(10)+1)*100)/100;
    , // else
        r1 = risposta + if(!rand(2),-1,1)*(floor(rand(10))+1);
        r2 = risposta + if(!rand(2),-1,1)*(floor(rand(10))+1);
        if(!rand(6), r1 = mcd*(floor(rand(floor(rand(20))+1))+1));
        if(!rand(6), r2 = mcd*(floor(rand(floor(rand(20))+1))+1));
        r1 = floor(r1);
        r2 = floor(r2);
    );
    

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        domanda, // domanda
        r2 = floor(rand(100));
        while(r2 == risposta || r2 == r1 || !r2, 127,
            r2 += if(rand(2),1,-1)*floor(rand(10))
        );

        risposte = [risposta,r1];
        if(r2 != risposta && r2 != r1,
            risposte = [...risposte, r2]
        );

        risposte, // risposte
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);



// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        if(!global_floor && _time_running(),
            center = pos(player);
            global_floor = in_dimension(player~'dimension',_surface_block(_rand_pos_around(center,20)));
            _show_block_title(player, global_floor);
        );

        if(_valid_time() && _time() < -800,
            _stop_countdown();
            global_floor = null;
            _start_countdown();
        );
        if(_valid_time(),
            pos1 = pos(player)-[0,0.1,0];
            pos2 = pos(player);
            if(in_dimension(player~'dimension',block(pos1) == global_floor || block(pos2) == global_floor),
                _stop_countdown();
                global_floor = null;
                schedule(0, 'domanda_percentuali', player);
            )
        );

        down=_countdown_string() || '';
        up=_countup_string(800) || '';
        if(
            global_floor,
                item = if(
                    global_floor=='lava','lava_bucket',
                    global_floor=='water','water_bucket',
                    global_floor=='tall_seagrass','seagrass',
                    global_floor=='kelp_plan','kelp',
                    global_floor=='frosted_ice','ice',
                    global_floor=='powder_snow','powder_snow_bucket',
                    global_floor
                );
                icon = _icon_item_json(item);

                json = [
                    {
                        'text' -> if(down != '', down+' - ', 
                                     up != '', up + ' - ',
                                     ''), 
                        'color' -> if(down != '', 'red', 'green')
                    },
                    icon,
                    {
                        'text' -> ' ('+global_floor+')',
                        'color' -> 'gray'
                    }
                ];
                
                _show_json_actionbar(player,json)
            , // elif
            down != '',
                _show_text_actionbar(player,down,'red')
            
            , // else
                _show_text_actionbar(player,'','red')
        );
    );
);

__on_close() -> (
    _force_closing_screen(player());
);
