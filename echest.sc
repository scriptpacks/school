__config()->{
    'commands'->{
        '' -> 'open',
    }
};

open() -> (
    player = player();
    if(!player,
        print(format('r Player not found'));
        exit();
    );
    screen=create_screen(player,'generic_9x3','Ender Chest',_(screen, player, action, data)->(        
        if(action=='close',
            drop_item(screen,-1);
            sound('block.ender_chest.close', pos(player), 1, 1, 'block')
        );
        if(action=='slot_update' && data:'slot' >= 0 && data:'slot' < 27,
            item_tuple = inventory_get(screen, data:'slot');
            [item, count, nbt] = item_tuple || [null, 0, null];
            inventory_set('enderchest',player,data:'slot', count, item, nbt)
        );
    ));
    loop(inventory_size('enderchest',player),
        item_tuple = inventory_get('enderchest',player, _);
        [item, count, nbt] = item_tuple || [null, 0, null];
        inventory_set(screen,_, count, item, nbt)
    );
    sound('block.ender_chest.open', pos(player), 1, 1, 'block')
)