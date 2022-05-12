global_positive_effects = [
    'hero_of_the_village‌',
    'dolphins_grace',
    'conduit_power',
    'slow_falling',
    'luck',
    'absorption',
    'health_boost',
    'night_vision',
    'invisibility',
    'water_breathing',
    'fire_resistance',
    'resistance',
    'jump_boost',
    'regeneration',
    'instant_health',
    'strength',
    'speed',
    'haste'
];
_r_positive_effect(player) -> (
    effect = rand(global_positive_effects);
    ticks = floor(rand(30*20)+30*20); // da 30 secondi a 1 minuto
    amplifier = floor(rand(10)+1); // da 1 a 10
    modify(player, 'effect', effect, ticks, amplifier)
)