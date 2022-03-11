package top.lanscarlos.towerdefence.internal

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.module.chat.colored
import taboolib.module.lang.asLangText
import taboolib.platform.util.asLangText
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.nms.analogEntity
import top.lanscarlos.towerdefence.nms.resetBorder
import top.lanscarlos.towerdefence.nms.setBorder
import top.lanscarlos.towerdefence.utils.toDouble

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.internal
 *
 * @author Lanscarlos
 * @since 2022-03-09 16:58
 */
class WorkSpace(
    val player: Player,
    val region: Region
) {

    var center = region.center.clone()
    var size = region.size
    val playersSpawn = mutableListOf<Pair<Location, Entity>>()
    val mobSpawn = mutableListOf<Pair<Location, Entity>>()

    init {
        region.editing = true
        region.playersSpawn.forEach {
            playersSpawn.add(it to player.analogEntity(it))
        }
        region.mobSpawn.forEach {
            mobSpawn.add(it to player.analogEntity(it))
        }
        analogEntity()

        // 玩家在区域之外
        if (!player.location.isInBounds()) {
            // 将编辑者传送到中心
            player.teleport(center)
        }
        display()
    }

    /**
     * 移动区域
     * 根据玩家朝向移动相应坐标
     *
     * @param distance 移动距离
     * @return 提示信息
     * */
    fun move(distance: Double): String {
        fun Float.inRange(vararg ranges: Pair<Int, Int>): Boolean {
            ranges.forEach {
                if (this >= it.first && this < it.second) return true
            }
            return false
        }
        val yaw = player.eyeLocation.yaw
        val msg = when {
            yaw.inRange(135 to 225, -225 to -135) -> {
                // North move -Z
                info("North move -Z")
                center.z -= distance
                console().asLangText("WorkSpace-Move-Success", region.id, "北", distance)
            }
            yaw.inRange(0 to 45, 315 to 360, -45 to 0, -360 to -315) -> {
                // South move +Z
                info("South move +Z")
                center.z += distance
                console().asLangText("WorkSpace-Move-Success", region.id, "南", distance)
            }
            yaw.inRange(45 to 135, -315 to -225) -> {
                // West move -X
                info("West move -X")
                center.x -= distance
                console().asLangText("WorkSpace-Move-Success", region.id, "西", distance)
            }
            yaw.inRange(225 to 315, -135 to -45) -> {
                // East move +X
                info("East move +X")
                center.x += distance
                console().asLangText("WorkSpace-Move-Success", region.id, "东", distance)
            }
            else -> {
                console().asLangText("WorkSpace-Move-Failed", region.id)
            }
        }
        display()
        analogEntity()
        return msg
    }

    fun center(loc: Location) {
        center = loc.clone()
        display()
    }

    fun center(x: Double, z: Double) {
        center.x = x
        center.z = z
        display()
    }

    fun size(size: Double) {
        this.size = size
        display()
    }

    fun addMobSpawnPoint(): String {
//        val loc = player.getTargetBlock(null, 100).location.clone().add(0.5, 1.0, 0.5)
        // 玩家当前位置
        val loc = player.location.block.location.clone().add(0.5, 0.0, 0.5)
        if (!loc.isInBounds()) return player.asLangText("WorkSpace-MobSpawn-Add-Failed")
        mobSpawn.add(loc to player.analogEntity(loc))
        analogEntity()
        return player.asLangText("WorkSpace-MobSpawn-Add-Success", region.id)
    }

    fun removeMobSpawnPoint(index: Int) {
        mobSpawn.removeAt(index).second.remove()
        player.sendLang("WorkSpace-MobSpawn-Remove-Success", region.id)
        analogEntity()
    }

    fun addPlayerSpawnPoint(): String {
//        val loc = player.getTargetBlock(null, 100).location.clone().add(0.5, 1.0, 0.5)
        val loc = player.location.block.location.clone().add(0.5, 0.0, 0.5)
        if (!loc.isInBounds()) return player.asLangText("WorkSpace-PlayerSpawn-Add-Failed")
        playersSpawn.add(loc to player.analogEntity(loc))
        analogEntity()
        return player.asLangText("WorkSpace-PlayerSpawn-Add-Success", region.id)
    }

    fun removePlayerSpawnPoint(index: Int) {
        playersSpawn.removeAt(index).second.remove()
        player.sendLang("WorkSpace-PlayerSpawn-Remove-Success", region.id)
        analogEntity()
    }

    fun reset() {
        center = region.center.clone()
        size = region.size
        display()
    }

    /**
     * 退出编辑模式
     * */
    fun exit() {
        region.center = center
        region.size = size
        region.playersSpawn.clear()
        region.playersSpawn.addAll(playersSpawn.map { it.first })
        region.mobSpawn.clear()
        region.mobSpawn.addAll(mobSpawn.map { it.first })
        region.save()
        player.resetBorder(center.world!!)
        playersSpawn.forEach {
            it.second.remove()
        }
        mobSpawn.forEach {
            it.second.remove()
        }
        cache.remove(player)
    }

    /**
     * 显示边界
     * */
    fun display() {
        player.setBorder(center, size)
    }

    /**
     * 显示实体
     * */
    fun analogEntity() {
        var count = 1
        var iterator = playersSpawn.iterator()
        while (iterator.hasNext()) {
            val point = iterator.next()
            if (!point.first.isInBounds()) {
                point.second.remove()
                iterator.remove()
                player.sendLang("WorkSpace-PlayerSpawn-OutOfBounds", count)
                continue
            }
            point.second.customName = "&a出生点 ${count++}".colored()
        }
        count = 1
        iterator = mobSpawn.iterator()
        while (iterator.hasNext()) {
            val point = iterator.next()
            if (!point.first.isInBounds()) {
                point.second.remove()
                iterator.remove()
                player.sendLang("WorkSpace-MobSpawn-OutOfBounds", count)
                continue
            }
            point.second.customName = "&c刷怪点 ${count++}".colored()
        }
    }

    fun Location.isInBounds(): Boolean {
        val radius = size / 2
        return x >= center.x - radius && x <= center.x + radius && z >= center.z - radius && z <= center.z + radius
    }

    companion object {

        val cache = mutableMapOf<Player, WorkSpace>()

        fun create(player: Player, id: String, loc: Location, size: Double = 20.0): WorkSpace {
            if (player in cache) {
                cache[player]!!.exit()
            }
            return WorkSpace(player, Region.create(id, loc, size)).also { cache[player] = it }
        }

        /**
         * 进入编辑模式
         * */
        fun edit(player: Player, region: Region): WorkSpace {
            if (player in cache) {
                cache[player]!!.exit()
            }
            return WorkSpace(player, region).also { cache[player] = it }
        }

        fun get(player: Player): WorkSpace? {
            return cache[player]
        }

        @SubscribeEvent
        fun e(e: PlayerQuitEvent) {
            if (e.player !in cache) return
            cache.remove(e.player)!!.exit()
        }

        @SubscribeEvent
        fun e(e: EntityDeathEvent) {
            if (e.entityType != EntityType.ARMOR_STAND) return
            cache.values.forEach { ws ->
                ws.playersSpawn.indexOfFirst { e.entity == it.second }.let {
                    if (it < 0) return@let
                    ws.removePlayerSpawnPoint(it)
                }
                ws.mobSpawn.indexOfFirst { e.entity == it.second }.let {
                    if (it < 0) return@let
                    ws.removeMobSpawnPoint(it)
                }
            }
        }

        @Awake(LifeCycle.DISABLE)
        fun onDisable() {
            cache.values.forEach { it.exit() }
        }
    }

}