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
import('title_utils','_show_text_actionbar');
import('inventory_utils','_shuffle_inventory');
import('items_utils','_r_enchant');
import('streak','_add_streak','_get_streak');

__config()->{
    'commands'->{
        'risposta <int>' -> '_rispondi'
    }
};

_rispondi(int) -> _risposta(player(),int);
_set_time(100);
_n_ep(3);
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
    );

    // RICOMPENSA
    item_tuple = inventory_get(player, global_slot);
    if(item_tuple,
        item_tuple = _r_enchant(item_tuple);
        [item, count, nbt] = item_tuple;
        inventory_set(player, global_slot, count, item, nbt || '{}')
    );

    _force_closing_screen(player)
);
_penalita(player, r, corretta) -> (
    particle('wax_on', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#ffdd00 Accidenti! La risposta corretta era '+corretta));
    _start_countdown();
    
    // STREAK
    _add_streak(false);
    
    // PENALITA'
    _shuffle_inventory(player);

    _force_closing_screen(player)
);

// MODULO
domanda_modulo(player) ->
genera_domanda_libera(player, global_calcolatrice,
    a = floor(rand(250));
    if(!rand(10), a = a*-1);
    b = floor(rand(10))+2;
    a+' mod '+b+' = ?', // domanda
    a%b, // risposte
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

__on_close() -> (
    _force_closing_screen(player());
);

__on_player_switches_slot(player, from, to) -> (
    global_slot = to;
    if(_valid_time(),
        _stop_countdown();
        schedule(0, 'domanda_modulo', player);
    );
);
__on_player_swaps_hands(player) -> (
    global_slot = -1;
    if(_valid_time(),
        _stop_countdown();
        schedule(0, 'domanda_modulo', player);
    );
);
