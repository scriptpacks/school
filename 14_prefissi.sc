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
_n_ep(14);
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

// PREFISSI
domanda_prefissi(player) -> (
    es = floor(rand(3));
    if(
        es == 0, domanda_1(player), // quale è il più grande?
        es == 1, domanda_3(player), // come si chiama?
        domanda_2(player)           // conversione in notazione scientifica
    )
);
global_apici = ['⁰','¹','²','³','⁴','⁵','⁶','⁷','⁸','⁹'];
notazione_scientifica(e) -> if(e==0,'1',str('10%s',
    join('',map(split(str(e)),
        if(_ == '-', '⁻',
           _ == 'n', 'ⁿ',
           global_apici:number(_)
        )
    ))
));
global_prefissi = {
    'yocto' -> ['y',-24],
    'zepto' -> ['z',-21],
    'atto' -> ['a',-18],
    'femto' -> ['f',-15],
    'pico' -> ['p',-12],
    'nano' -> ['n',-9],
    'micro' -> ['μ',-6],
    'milli' -> ['m',-3],
    'centi' -> ['c',-2],
    'deci' -> ['d',-1],
    '' -> ['',0],
    'deca' -> ['da',1],
    'etto' -> ['h',2],
    'kilo' -> ['k',3],
    'mega' -> ['M',6],
    'giga' -> ['G',9],
    'tera' -> ['T',12],
    'peta' -> ['P',15],
    'exa' -> ['E',18],
    'zetta' -> ['Z',21],
    'yotta' -> ['Y',24]
};
global_unita = {
    'metri' -> 'm',
    'grammi' -> 'g',
    'secondi' -> 's',
    'byte' -> 'B',
};
domanda_1(player) -> (
    // Più grande?
    u = rand(keys(global_unita));

    prefix_index_length = length(keys(global_prefissi));
    prefix_index_size = floor(prefix_index_length*(1+min(global_difficolta/10,1))/2);
    prefix_index_min = floor((prefix_index_length-prefix_index_size)/2);

    prefix_a_index = floor(rand(prefix_index_size))+prefix_index_min;
    prefix_a = keys(global_prefissi):prefix_a_index;
    prefix_a_value = global_prefissi:prefix_a;

    prefix_b_index = floor(rand(prefix_index_size))+prefix_index_min;
    prefix_b = keys(global_prefissi):prefix_b_index;
    prefix_b_value = global_prefissi:prefix_b;

    a = floor(rand(10+global_difficolta*10));
    A = a+' '+prefix_a_value:0+global_unita:u;

    b = floor(rand(10+global_difficolta*10));
    B = b+ ' '+prefix_b_value:0+global_unita:u;
    
    risposte = [];
    if((D_prefix = prefix_a_index - prefix_b_index) > 4,
        risposte = [A, B, 'sono uguali'],
    , D_prefix < -4,
        risposte = [B, A, 'sono uguali'],
    , a * 10^prefix_a_value:1 > b * 10^prefix_b_value:1,
        risposte = [A, B, 'sono uguali'],
    , a * 10^prefix_a_value:1 < b * 10^prefix_b_value:1,
        risposte = [B, A, 'sono uguali'],
    ,
        risposte = ['sono uguali', A, B]
    );

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        'Quale è il più grande?', // domanda
        risposte, // risposte
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);
domanda_2(player) -> (
    // Come si chiama?
    u = rand(keys(global_unita));
    prefix = rand(keys(global_prefissi));

    genera_domanda_libera(player, global_calcolatrice,
        'Come si chiamano i "'+global_prefissi:prefix:0+global_unita:u+'"?\n', // domanda
        prefix+u, // risposta
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);
domanda_3(player) -> (
    // Quanto vale?
    u = rand(keys(global_unita));

    prefix_index_length = length(keys(global_prefissi));
    prefix_index_size = floor(prefix_index_length*(1+min(global_difficolta/10,1))/2);
    prefix_index_min = floor((prefix_index_length-prefix_index_size)/2);

    prefix_a_index = floor(rand(prefix_index_size))+prefix_index_min;
    prefix_a = keys(global_prefissi):prefix_a_index;
    prefix_a_value = global_prefissi:prefix_a;

    shift_b = floor(rand(2+max(0,global_difficolta))+1);;
    prefix_b_index = max(0,min(prefix_a_index + (-1^floor(rand(2)))*shift_b, prefix_index_length));
    if(prefix_a_index == prefix_b_index, 
        prefix_b_index = (prefix_index_length + prefix_b_index + (-1^floor(rand(2)))) % prefix_index_length
    );
    prefix_b = keys(global_prefissi):prefix_b_index;
    prefix_b_value = global_prefissi:prefix_b;
    
    risposte = [notazione_scientifica(prefix_a_value:1)+' '+u, 
                notazione_scientifica(prefix_b_value:1)+' '+u];
    

    shift_c = floor(rand(2+max(0,global_difficolta))+1);
    prefix_c_index = max(0,min(prefix_c_index + (-1^floor(rand(2)))*shift_c, prefix_index_length));
    if(prefix_a_index == prefix_c_index, 
        prefix_c_index = (prefix_index_length + prefix_c_index + (-1^floor(rand(2)))) % prefix_index_length
    );
    prefix_c = keys(global_prefissi):prefix_c_index;
    prefix_c_value = global_prefissi:prefix_c;

    if(prefix_c != prefix_a && prefix_c != prefix_b,
        risposte += notazione_scientifica(prefix_c_value:1)+' '+u
    );
    
    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        'Quanto vale un '+global_prefissi:prefix_a:0+global_unita:u+'?\n', // domanda
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
       schedule(10, 'domanda_prefissi', player);
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
