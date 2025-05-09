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
        {'source' -> '/libs/inventory_utils.scl'},
        {'source' -> '/libs/blocks_utils.scl'},
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
    '_show_json_actionbar',
    '_show_block_title'
);
import('icons','_icon_item_json');
import('blocks_utils','_random_block_item_around','_random_block_around','_block_to_item');
import('inventory_utils','_random_item_inventory');
import('streak','_add_streak','_get_streak');
import('array_utils','_shuffle');
import('totems',
    '_give_random_totem',
    '_on_player_uses_item',
    '_on_player_dies',
    '_on_player_respawns',
    '_on_player_rides',
    '_on_player_interacts_with_entity',
    '_on_player_swings_hand'
);
import('math','_euclidean');

_rispondi(int) -> _risposta(player(),int);
_set_time(global_time = 30*20);
_n_ep(12);
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
    _give_random_totem(player);

    schedule(0, _(outer(player)) -> _force_closing_screen(player))
);
_materializza(player,r) -> (
    pos = pos(player);
    for(range(-r,r), x=_; for(range(-r,r), y=_; for(range(-r,r), z=_;
        pos1 = pos+[x,y,z];
        ra = rand(dist = _euclidean(pos, pos1));
        if(dist >2 && ra<r,
            schedule(dist + floor(rand(dist+1)), _(outer(pos1),outer(player))->(
                if(!rand(3),
                    place_item(rand(item_list()),...pos1,'down',true)
                )
            ))
        )
    )))
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
    schedule(20, '_materializza', player, 2 + _get_streak());

    schedule(0, _(outer(player)) -> _force_closing_screen(player))
);

// FIBONACCI
_fibonacci(n,m) -> (
    list = if(n>m,[m,n],[n,m]);
    loop(rand(3)+2,
        list += list:(-1)+list:(-2)
    );
    list
);

domanda_fibonacci(player) -> (
    global_difficolta = max(global_difficolta,0);
    list = _fibonacci(floor(rand(10+global_difficolta)),floor(rand(10+global_difficolta)+1));
    index = floor(rand(length(list)));
    risposta = list:index;
    list:index = '?';
    domanda = 'Completa la successione!\n\n' + join(', ',list);

    genera_domanda_libera(player, global_calcolatrice,
        domanda, // domanda
        risposta, // risposta
        _(p,r)->_ricompensa(p,r), // ricompensa
        _(p,r,c)->_penalita(p,r,c) // penalità
    );
);

// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        if(player && player~'gamemode'!='survival', return());

        has_item = inventory_find(player,global_item) != null;
        if(global_item,
            if((text=_countdown_string())!='', color='red',
                (text=_countup_string(200))!='', color ='green',
                text=''; color='white'
            );
            json_icon = _icon_item_json(global_item);
            json_text = [
                '',
                json_icon,
                {
                    'text'->'('+global_item+') ',
                    'color'->'light_gray'
                },
                {
                    'text' -> text, 
                    'color' -> color
                },
                if(has_item, {
                    'text' -> ' ✔',
                    'color' -> '#00FF00'
                },'')
            ];
            _show_json_actionbar(player,json_text);

            if(_valid_time() && !has_item,
                global_item = null;
                _stop_countdown();

                in_dimension(player~'dimension',particle('block_marker barrier',pos(player)+[0,player~'eye_height',0]+player~'look',1,0.1,0.3));
                schedule(10, 'domanda_fibonacci', player);

            );
        );
       
        if(_valid_time() && _time()<-200 || _time()>0 && !global_item,
            _start_countdown();
            global_item = in_dimension(player~'dimension',_random_block_item_around(player));
            _show_block_title(player, global_item);
        )
    )    
);

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
__on_player_dies(player)->(
    _on_player_dies(player);
    _stop_countdown()
);
__on_player_respawns(player)->(
    _on_player_respawns(player);
    _start_countdown()
);
__on_player_rides(player, forward, strafe, jumping, sneaking)->_on_player_rides(player, forward, strafe, jumping, sneaking);
__on_player_interacts_with_entity(player, entity, hand)->_on_player_interacts_with_entity(player, entity, hand);
__on_player_swings_hand(player, hand)->_on_player_swings_hand(player, hand);
