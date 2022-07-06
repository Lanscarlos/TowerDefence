package top.lanscarlos.towerdefence.script

import org.bukkit.entity.Player
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.*
import top.lanscarlos.towerdefence.internal.Game.Companion.inGame
import top.lanscarlos.towerdefence.internal.Occupation

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.script
 *
 * @author Lanscarlos
 * @since 2022-07-06 17:44
 */
object ActionOccupation {
    @KetherParser(["occupation"], namespace = "towerdefence", shared = true)
    fun parser() = scriptParser {
        val occupation = it.next(ArgTypes.ACTION)
        actionNow {
            val id = newFrame(occupation).run<Any?>()?.get()?.toString() ?: return@actionNow false
            val player = script().sender as? Player ?: return@actionNow false
            val cache = player.inGame()?.cache?.get(player) ?: return@actionNow false
            cache.occupation = Occupation.get(id)
            return@actionNow true
        }
    }
}