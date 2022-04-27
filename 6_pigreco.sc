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
_set_time(300);
_n_ep(6);
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
global_chests = ['abandoned_mineshaft','bastion_bridge','bastion_hoglin_stable','bastion_other','bastion_treasure','buried_treasure','desert_pyramid','end_city_treasure','igloo_chest','jungle_temple','jungle_temple_dispenser','nether_bridge','pillager_outpost','ruined_portal','shipwreck_map','shipwreck_supply','shipwreck_treasure','simple_dungeon','spawn_bonus_chest','stronghold_corridor','stronghold_crossing','stronghold_library','underwater_ruin_big','underwater_ruin_small','village/village_armorer','village/village_butcher','village/village_cartographer','village/village_desert_house','village/village_fisher','village/village_fletcher','village/village_mason','village/village_plains_house','village/village_savanna_house','village/village_shepherd','village/village_snowy_house','village/village_taiga_house','village/village_tannery','village/village_temple','village/village_toolsmith','village/village_weaponsmith','woodland_mansion'];
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
    if(inventory_size(global_chest) > 0,
        run(str('loot insert %d %d %d loot chests/%s',[
            ...pos(global_chest),
            rand(global_chests)
        ])),
        run(str('loot spawn %f %f %f loot chests/%s',[
            ...(pos(global_chest)+[0.5,1,0.5]),
            rand(global_chests)
        ]))
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
    schedule(0, _(outer(player))->
        run(str('execute as %s at @s run spreadplayers ~ ~ 10 100 under 100 false @s', player))
    );

    _force_closing_screen(player)
);

// PIGRECO
domanda_pigreco(player) -> (
    raggio = if(global_decimali, (floor(rand(30))+1)/4, floor(rand(10))+1);
    diametro = 2*raggio;
    circonferenza = diametro + 'π';
    area = raggio^2 + 'π';

    es = floor(rand(5));
    if(es == 0,
        // CIRCONFERENZA
        domanda = str('Se un cerchio ha raggio %s, quale è la sua circonferenza?',
            raggio
        );
        risposta = circonferenza;
        if(area != circonferenza, r1 = area, r1 = diametro),
       es == 1,
        domanda = str('Se un cerchio ha diametro %s, quale è la sua circonferenza?',
            diametro
        );
        risposta = circonferenza;
        if(area != circonferenza, r1 = area, r1 = raggio),
       es == 2,
        domanda = str('Se un cerchio ha area %s, quale è la sua circonferenza?',
            area
        );
        risposta = circonferenza;
        r1 = diametro,
        // DIAMETRO
       es == 3,
        domanda = str('Se un cerchio ha raggio %s, quale è il suo diametro?',
            raggio
        );
        risposta = diametro;
        r1 = area,
       es == 4,
        domanda = str('Se un cerchio ha area %s, quale è il suo diametro?',
            area
        );
        risposta = diametro;
        r1 = circonferenza,
       es == 5,
        domanda = str('Se un cerchio ha circonferenza %s, quale è il suo diametro?',
            circonferenza
        );
        risposta = diametro;
        r1 = raggio,
        // RAGGIO
       es == 6,
        domanda = str('Se un cerchio ha circonferenza %s, quale è il suo raggio?',
            circonferenza
        );
        risposta = raggio;
        r1 = diametro,
       es == 7,
        domanda = str('Se un cerchio ha area %s, quale è il suo raggio?',
            area
        );
        risposta = raggio;
        r1 = circonferenza,
       es == 8,
        domanda = str('Se un cerchio ha diametro %s, quale è il suo raggio?',
            diametro
        );
        risposta = raggio;
        r1 = 2*diametro,
        // AREA
       es == 9,
        domanda = str('Se un cerchio ha raggio %s, quale è la sua area?',
            raggio
        );
        risposta = area;
        if(area != circonferenza, r1 = circonferenza, r1 = diametro),
       es == 10,
        domanda = str('Se un cerchio ha diametro %s, quale è la sua area?',
            diametro
        );
        risposta = area;
        if(area != circonferenza, r1 = circonferenza, r1 = raggio),
       es == 11,
        domanda = str('Se un cerchio ha circonferenza %s, quale è la sua area?',
            circonferenza
        );
        risposta = area;
        r1 = diametro,
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

global_blocchi = ['smoker','cartography_table','crafting_table','chest','furnace','blast_furnace','barrel'];
__on_player_interacts_with_block(player, hand, block, face, hitvec) -> (
    if(_valid_time(),
        _stop_countdown();
        global_chest = block;
        schedule(0, 'domanda_pigreco', player);
    );
);
