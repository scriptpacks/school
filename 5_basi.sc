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
import('array_utils','_shuffle');
import('streak','_add_streak','_get_streak');
import('math_utils','_base');
import('math','_euclidean');

_rispondi(int) -> _risposta(player(),int);
_set_time(300);
_n_ep(5);
global_calcolatrice = true;

// AUX
global_random = true;
_divora(player, pos) -> (
    item = inventory_get(player,global_slot);
    if(item:0 == global_item:1,
        block = first(
            if(global_random,_shuffle([diamond(pos,3,3)]),diamond(pos,3,3)),
            air(_) && _euclidean(pos(_),pos(player))>2 && rand(2)
        );
        if(block != null,
            pos = pos(block);
            particle('wax_on', pos);
            set(pos, global_block);
            inventory_set(player,global_slot, item:1 - 1, item:0, item:2);
            schedule(floor(rand(2)), '_divora', player, pos)
        )
    )
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
    );

    // RICOMPENSA
    if(global_slot != null && global_bonus != null,
        [item, count, nbt] = if(lista = inventory_get(player, global_slot), lista, [global_item:1, 0, null]);
        inventory_set(player, global_slot, count + number(global_bonus), item, nbt)
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
    if(global_slot != null && global_block_pos != null && global_block != null,
        schedule(10, '_divora', player, global_block_pos)
    );

    _force_closing_screen(player)
);

// PERCENTUALI
global_primi = [1,2,3,5,7,11,13,17,19];

domanda_basi(player) -> (
    tipo = floor(rand(2));
    if(
        tipo == 0, domanda_a_decimale(player),
        tipo == 1, domanda_da_decimale(player)
    )
);

domanda_a_decimale(player) ->
genera_domanda_libera(player, global_calcolatrice,
    base = floor(rand(8)+2);
    numero = floor(rand(100));
    global_bonus = numero_in_base = _base(numero,base);
    str('Il numero %d in base %d, quanto vale in base decimale?', numero_in_base, base), // domanda
    numero, // risposta
    _(p,r)->_ricompensa(p,r), // ricompensa
    _(p,r,c)->_penalita(p,r,c) // penalità
);

domanda_da_decimale(player) ->
genera_domanda_libera(player, global_calcolatrice,
    base = floor(rand(8)+2);
    numero = floor(rand(100));
    global_bonus = numero_in_base = _base(numero,base);
    str('Il numero %d in base decimale, quanto vale in base %d?', numero, base), // domanda
    numero_in_base, // risposta
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

__on_player_places_block(player, item_tuple, hand, block) -> (
    if(_valid_time(),
        _stop_countdown();
        global_item = [item_tuple:1, item_tuple:0];
        global_slot = if(hand=='mainhand',player~'selected_slot',-1);
        global_block_pos = pos(block);
        global_player_pos = pos(player);
        global_block = block;
        schedule(0, 'domanda_basi', player);
    );
);
