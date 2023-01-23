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

// PRIMI
domanda_primi(player) -> (
    if(rand(2),
        domanda_1(player), // è primo?
        domanda_2(player)        // divisibile per?
    )
);

global_primi = [2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,
                101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,179,181,
                191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,
                281,283,293,307,311,313,317,331,337,347,349,353,359,367,373,379,383,
                389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,479,487,
                491,499,503,509,521,523,541,547,557,563,569,571,577,587,593,599,601,
                607,613,617,619,631,641,643,647,653,659,661,673,677,683,691,701,709,
                719,727,733,739,743,751,757,761,769,773,787,797,809,811,821,823,827,
                829,839,853,857,859,863,877,881,883,887,907,911,919,929,937,941,947,
                953,967,971,977,983,991,997,1009,1013,1019,1021,1031,1033,1039,1049,
                1051,1061,1063,1069,1087,1091,1093,1097,1103,1109,1117,1123,1129,
                1151,1153,1163,1171,1181,1187,1193,1201,1213,1217,1223,1229,1231,
                1237,1249,1259,1277,1279,1283,1289,1291,1297,1301,1303,1307,1319,
                1321,1327,1361,1367,1373,1381,1399,1409,1423,1427,1429,1433,1439,
                1447,1451,1453,1459,1471,1481,1483,1487,1489,1493,1499,1511,1523,
                1531,1543,1549,1553,1559,1567,1571,1579,1583,1597,1601,1607,1609,
                1613,1619,1621,1627,1637,1657,1663,1667,1669,1693,1697,1699,1709,
                1721,1723,1733,1741,1747,1753,1759,1777,1783,1787,1789,1801,1811,
                1823,1831,1847,1861,1867,1871,1873,1877,1879,1889,1901,1907,1913,
                1931,1933,1949,1951,1973,1979,1987,1993,1997,1999,2003,2011,2017,
                2027,2029,2039,2053,2063,2069,2081,2083,2087,2089,2099,2111,2113,
                2129,2131,2137,2141,2143,2153,2161,2179,2203,2207,2213,2221,2237,
                2239,2243,2251,2267,2269,2273,2281,2287,2293,2297,2309,2311,2333,
                2339,2341,2347,2351,2357,2371,2377,2381,2383,2389,2393,2399,2411,
                2417,2423,2437,2441,2447,2459,2467,2473,2477];
is_prime(n) -> (
    if(n < 2, return(false));
    if(n == 2, return(true));
    if(n % 2 == 0, return(false));
    if(n < 2500, 
        if(global_primi~n!=null,return(true)),
    // else
        c_for(i=3; i<=n/2; i+=2,
            if(n % i == 0, return(false));
        );
    );
    true
);
domanda_1(player) -> (
    // E' primo?
    if(rand(2),
        numero = floor(rand(100*global_difficolta+100))+2,
        numero = rand(global_primi)
    );
    risposte = if(is_prime(numero), ['Sì', 'No'], ['No', 'Sì']);

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        str('%d è un numero primo?\n',numero), // domanda
        risposte, // risposte
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);
domanda_2(player) -> (
    // E' divisibile per ...?
    numero = floor(rand(100*global_difficolta+100))+2;
    divisore = floor(rand(10*global_difficolta+10))+2;
    risposte = if(numero%divisore==0, ['Sì', 'No'], ['No', 'Sì']);

    genera_domanda_multipla(player, global_calcolatrice, str('/%s risposta ',system_info('app_name')),
        str('%d è divisibile per %d?\n',numero, divisore), // domanda
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
       schedule(10, 'domanda_primi', player);
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
