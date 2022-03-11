package top.lanscarlos.towerdefence.event

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import top.lanscarlos.towerdefence.internal.Game

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.event
 *
 * @author Lanscarlos
 * @since 2022-03-11 19:26
 */
class PlayerKillMobEvent(
    game: Game,
    val player: Player,
    val entity: Entity
) : GameEvent(game)