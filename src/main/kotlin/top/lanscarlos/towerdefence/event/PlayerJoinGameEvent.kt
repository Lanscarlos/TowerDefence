package top.lanscarlos.towerdefence.event

import org.bukkit.entity.Player
import top.lanscarlos.towerdefence.internal.Game

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.event
 *
 * @author Lanscarlos
 * @since 2022-03-11 19:26
 */
class PlayerJoinGameEvent(
    game: Game,
    val player: Player
) : GameEvent(game)