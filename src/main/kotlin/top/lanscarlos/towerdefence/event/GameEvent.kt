package top.lanscarlos.towerdefence.event

import taboolib.platform.type.BukkitProxyEvent
import top.lanscarlos.towerdefence.internal.Game

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.event
 *
 * @author Lanscarlos
 * @since 2022-03-11 19:26
 */
open class GameEvent(
    val game: Game
) : BukkitProxyEvent()