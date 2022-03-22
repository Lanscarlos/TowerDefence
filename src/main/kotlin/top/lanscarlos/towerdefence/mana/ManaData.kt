package top.lanscarlos.towerdefence.mana

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.Schedule

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.mana
 *
 * @author Lanscarlos
 * @since 2022-03-22 19:23
 */
class ManaData(
    val player: Player
) {

    var mana = 100
        set(value) {
            field = if (value >= maxMana) maxMana
            else if (value <= 0) 0
            else value
        }
    var maxMana = 100
        set(value) {
            field = value
            if (mana > value) {
                mana = value
            }
        }
    var recoverSpeed = 5
        set(value) {
            if (value >= 0) field = value
        }

    fun recover() {
        if (mana >= maxMana) return
        mana += recoverSpeed
    }

    companion object {

        val cache = mutableMapOf<Player, ManaData>()
        val Player.meta: ManaData
            get() {
                if (this in cache) return cache[this]!!
                val data = ManaData(this)
                cache[this] = data
                return data
            }

        @Schedule(period = 20)
        fun tick() {
            Bukkit.getOnlinePlayers().forEach {
                it.meta.recover()
            }
        }

    }
}