package top.lanscarlos.towerdefence.nms

import net.minecraft.server.v1_16_R3.*
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common.util.random
import taboolib.module.nms.MinecraftVersion
import java.util.*

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.nms
 *
 * @author Lanscarlos
 * @since 2022-03-03 15:07
 */
class NMSHandlerImpl : NMSHandler {

    override fun setBorder(player: Player, world: World, x: Double, z: Double, size: Double) {
        val connection = (player as CraftPlayer).handle.playerConnection
        val worldBorder = WorldBorder()
        worldBorder.world = (world as CraftWorld).handle.worldBorder.world
        worldBorder.setCenter(x, z)
        worldBorder.size = size
        connection.sendPacket(PacketPlayOutWorldBorder(
            worldBorder,
            PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER
        ))
        connection.sendPacket(PacketPlayOutWorldBorder(
            worldBorder,
            PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE
        ))
    }

    override fun resetBorder(player: Player, world: World) {
        val connection = (player as CraftPlayer).handle.playerConnection
        val worldBorder = (world as CraftWorld).handle.worldBorder
        connection.sendPacket(PacketPlayOutWorldBorder(
            worldBorder,
            PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER
        ))
        connection.sendPacket(PacketPlayOutWorldBorder(
            worldBorder,
            PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE
        ))
    }

    override fun setBorderWarning(player: Player) {
        val connection = (player as CraftPlayer).handle.playerConnection
        val worldBorder = WorldBorder()
        worldBorder.world = (player.world as CraftWorld).handle.worldBorder.world
        var distance = 40000000
        val max: Int = 300000000
        submit(period = 3) {
            distance += 10000000
            worldBorder.warningDistance = distance
            connection.sendPacket(PacketPlayOutWorldBorder(
                worldBorder,
                PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS
            ))
            if (distance >= max) {
                cancel()
                return@submit
            }
        }
    }

    override fun resetBorderWarning(player: Player) {
        val connection = (player as CraftPlayer).handle.playerConnection
        connection.sendPacket(PacketPlayOutWorldBorder(
            (player.world as CraftWorld).handle.worldBorder,
            PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS
        ))
    }

    override fun analogDie(player: Player, entity: Entity) {
        val connection = (player as CraftPlayer).handle.playerConnection
        info("发送假死数据包...")
        connection.sendPacket(PacketPlayOutEntityStatus((entity as CraftEntity).handle, 3.toByte()))
    }
//
//    override fun analogDieScreen(player: Player) {
//        val connection = (player as CraftPlayer).handle.playerConnection
//        info("发送假死屏幕数据包...")
//        connection.sendPacket(PacketPlayOutCombatEvent(
//            CombatTracker(player.handle),
//            PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED,
////            ChatMessage("you were die hahaha!")
//        ))
//    }

}