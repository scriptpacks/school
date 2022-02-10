__config()->{
    'commands' -> {
        '' -> _() -> print('Usa il comando '+format('gi /nick <nome>','?/nick ')+' per cambiare il tuo nome.'),
        'clear' -> ['_team_nome', null],
        '<nome>' -> '_team_nome'
    },
    'arguments' -> {
        'nome' -> {
            'type' -> 'text',
            'suggest' -> ['Luca', 'Alessandro', 'Marian', 'Marco', 'Filippo', 'Eleonora']
        }
    },
    'stay_loaded' -> true
};
_team_nome(nome)->(
    if(nome == null,
        team_leave(player());
    ,
        team_add(player()~'command_name');
        team_add(player()~'command_name',player());
        team_property(player(), 'displayName', nome);
        team_property(player(), 'prefix', nome+' · ');
    )
)
