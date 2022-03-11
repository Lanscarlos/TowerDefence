package top.lanscarlos.towerdefence.internal

import io.lumine.xikage.mythicmobs.MythicMobs
import org.bukkit.Location
import org.bukkit.entity.Entity
import taboolib.common.platform.function.warning

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.internal
 *
 * @author Lanscarlos
 * @since 2022-03-11 10:19
 */
class MobSpawner(
    val game: Game,
    val start: Int,
    val end: Int,
    val interval: Int,
    val mob: String,
    val amount: Int
) {

    var wait = interval

    /**
     * 周期函数
     *
     * @param schedule 当前游戏时间 tick
     * @param point 刷怪点数量
     * */
    fun tick(schedule: Int, point: Int) {
        if (schedule < start || schedule > end) return
        if (--wait == 0) {
            wait = interval
            game.region.mobSpawn.forEachIndexed { i, it ->
                if (i >= point) return
                spawn(it)
            }
        }
    }

    fun spawn(loc: Location) {
        if (amount <= 0) return
        else if (hook.getMythicMob(mob) == null) warning("Mob \"$mob\" is undefined!")
        else if (amount == 1)  game.addEntity(hook.spawnMythicMob(mob, loc))
        else for (i in 0 until amount) {
            game.addEntity(hook.spawnMythicMob(mob, loc))
        }
    }

    companion object {
        val hook by lazy { MythicMobs.inst().apiHelper }
    }
}