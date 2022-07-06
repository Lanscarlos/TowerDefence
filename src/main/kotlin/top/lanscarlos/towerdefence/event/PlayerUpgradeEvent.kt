package top.lanscarlos.towerdefence.event

import org.bukkit.entity.Player
import top.lanscarlos.towerdefence.internal.Game
import top.lanscarlos.towerdefence.internal.Occupation

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.event
 *
 * @author Lanscarlos
 * @since 2022-03-11 19:26
 */
class PlayerUpgradeEvent(
    game: Game,
    val player: Player,
    val occupation: Occupation,
    val level: Int
) : GameEvent(game)