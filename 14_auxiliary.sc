import('math','_euclidean');
import('totems','_give_random_totem');

global_r = 2;

__on_ricompensa(player, risposta) -> (
    _give_random_totem(player);

    global_r = max(2, global_r - 2)
);

__on_penitenza(player, risposta_data, risposta_corretta) -> (
    pos = pos(player);
    for(range(-global_r,global_r), x=_; for(range(-global_r,global_r), y=_; for(range(-global_r,global_r), z=_;
        pos1 = pos+[x,y,z];
        ra = rand(dist = _euclidean(pos, pos1));
        if(dist >2 && ra<global_r,
            schedule(dist + floor(rand(dist+1)), _(outer(pos1),outer(player))->(
                item = rand(item_list()); 
                if(!rand(3) && item ~ '_egg$' == null,
                    place_item(item,...pos1,'down',true)
                )
            ))
        )
    )));
    global_r += 1;
);

__on_player_collides_with_entity(player, entity)->(
    signal_event('domanda',player,player);
);

// NON TOCCARE 
handle_event('ricompensa', _(args) -> __on_ricompensa(...args));
handle_event('penitenza', _(args) -> __on_penitenza(...args));
