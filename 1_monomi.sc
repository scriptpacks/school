__config()->{
    'commands'->{
        'risposta <int>' -> '_rispondi'
    },
    'libraries' -> [
        {'source' -> '/libs/school.scl'},
        {'source' -> '/libs/icons.scl'},
        {'source' -> '/libs/polinomi.scl'},
        {'source' -> '/libs/countdown.scl'},
        {'source' -> '/libs/array_utils.scl'},
        {'source' -> '/libs/title_utils.scl'},
        {'source' -> '/libs/items_utils.scl'},
        {'source' -> '/libs/inventory_utils.scl'},
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
import('polinomi',
    '_random_polinomio_from_inventory',
    '_s_polinomio',
    '_riduci_polinomio'
);
import('countdown',
    '_start_countdown',
    '_stop_countdown',
    '_countdown_string',
    '_set_time',
    '_valid_time'
);
import('array_utils','_shuffle');
import('title_utils','_show_text_actionbar');
import('streak','_add_streak','_get_streak');

_rispondi(int) -> _risposta(player(),int);
_set_time(200);

// RICOMPENSA
_ricompensa(player, r, entity) -> (
    particle('happy_villager', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#00ff00 Esattamente! Ecco a te un fantastico premio!'));
    _start_countdown();

    // STREAK
    _add_streak(true);
    if(_get_streak()>=10,
        print(player('all'),format(str('#00ff00 %s ha risposto correttamente a '+_get_streak()+' domande consecutive!',player)));
    );

    // RICOMPENSA
    if(entity,modify(entity, 'pickup_delay', 0));
    if(global_premio,
        nbt = nbt(str('{Item:{id:"%s",count:%d}}',global_premio,floor(rand(_get_streak()))+1));
        nbt:'Owner' = player~'nbt':'UUID';
        spawn('item', pos(player), nbt)
    );

    schedule(0, _(outer(player)) -> _force_closing_screen(player));
);
_penalita(player, r, corretta, entity) -> (
    particle('wax_on', pos(player)+[0,player~'eye_height',0]+player~'look');
    print(player, format('#ffdd00 Accidenti! La risposta corretta era '+corretta));
    _start_countdown();
    
    // STREAK
    _add_streak(false);
    
    // PENALITA'
    if(entity,modify(entity,'remove'));

    schedule(0, _(outer(player)) -> _force_closing_screen(player));
);

// MONOMI
domanda_monomi(player, entity) ->
genera_domanda_multipla(player, false, str('/%s risposta ',system_info('app_name')),
    polinomio = _random_polinomio_from_inventory(player);
    global_polinomio = polinomio;
    global_premio = polinomio:(-1):1;
    d = _s_polinomio(polinomio);
    d += '=\n';
    d, // domanda
    r1 = _riduci_polinomio(polinomio);
    r2 = copy(r1);
    r3 = copy(r1);
    r2:(rand(length(r2))):0 += if(!rand(6),-1,1)*(floor(rand(10))+1);
    r3:(rand(length(r2))):0 += if(!rand(6),-1,1)*(floor(rand(10))+1);
    copy([
        _s_polinomio(r1),
        _s_polinomio(r2),
        _s_polinomio(r3)
    ]), // risposte
    _(p,r,outer(entity))->_ricompensa(p,r,entity), // ricompensa
    _(p,r,c,outer(entity))->_penalita(p,r,c,entity) // penalità
);


// EVENTI
__on_tick() -> (
    player = player();
    if(player,
        if((text=_countdown_string())!='', color='red',
           text=''; color='white'
        );
        _show_text_actionbar(player,text,color);
    );
);

__on_player_disconnects(player, reason)-> (
    _force_closing_screen(player);
);
__on_close() -> (
    _force_closing_screen(player());
);

__on_player_collides_with_entity(player, entity)->
if(_valid_time() && entity~'type' == 'item' && 
   entity~'pickup_delay'<=0 && inventory_find(player, null) != null,
    _stop_countdown();
    modify(entity, 'pickup_delay', 32767);
    modify(entity, 'despawn_timer', 32767);
    domanda_monomi(player, entity);
)
