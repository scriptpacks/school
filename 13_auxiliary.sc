__on_ricompensa(player, risposta) -> (
    print(player, 'Esatto!')
);

__on_penitenza(player, risposta_data, risposta_corretta) -> (
    print(player, 'Penitenza!')
);


__on_player_stops_sneaking(player) -> (
    signal_event('domanda',player,player);
);


// NON TOCCARE 
handle_event('ricompensa', _(args) -> __on_ricompensa(...args));
handle_event('penitenza', _(args) -> __on_penitenza(...args));
