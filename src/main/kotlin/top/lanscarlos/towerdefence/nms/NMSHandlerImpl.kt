package top.lanscarlos.towerdefence.nms

import net.minecraft.server.v1_16_R1.*
import net.minecraft.server.v1_16_R3.Packet
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntityLiving
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder
import net.minecraft.server.v1_16_R3.WorldBorder
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.common.reflect.Reflex.Companion.unsafeInstance
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

    val isUniversal = MinecraftVersion.isUniversal

    val majorLegacy = MinecraftVersion.majorLegacy

    var index = 12449599 + random(0, 256)

    /**
     * int 最大值           2,147,483,647
     * tr hologram               119,789 + (0~7763)
     * lib hologram          449,599,702
     * adyeshach npc             449,599 + (0~702)
     * TowerDefence analog      12449599 + (0~256)
     */
    fun nextIndex(): Int {
        return index++
    }

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

    override fun analogEntity(player: Player, location: Location) {
//        info("检测 spawnEntity - x1")
//        val connection = (player as CraftPlayer).handle.playerConnection
//        info("检测 spawnEntity - x2")
//        val entityId = nextIndex()
//        if (isUniversal) {
//            connection.sendPacket(setFields(
//                PacketPlayOutSpawnEntityLiving(),
//                "id" to entityId,
//                "uuid" to UUID.randomUUID(),
//                "x" to location.x,
//                "y" to location.y,
//                "z" to location.z,
//                "xa" to 0,
//                "ya" to 0,
//                "za" to 0,
//                "xRot" to (location.yaw * 256.0f / 360.0f).toInt().toByte(),
//                "yRot" to (location.pitch * 256.0f / 360.0f).toInt().toByte(),
//                "type" to getEntityTypeNMS(),
//                "data" to 0
//            )as Packet<*>)
//            connection.sendPacket(setFields(
//                PacketPlayOutEntityMetadata(),
//                "id" to entityId,
//                "packedItems" to listOf(
//                    DataWatcher.Item(DataWatcherObject(2, DataWatcherRegistry.d), "Zombiex") // 设置名字
//                )
//            ) as Packet<*>)
//            connection.sendPacket(setFields(
//                PacketPlayOutEntityMetadata(),
//                "id" to entityId,
//                "packedItems" to listOf(
//                    if (MinecraftVersion.major >= 5) {
//                        DataWatcher.Item(DataWatcherObject(3, DataWatcherRegistry.i), true)
//                    } else {
//                        net.minecraft.server.v1_11_R1.DataWatcher.Item(
//                            net.minecraft.server.v1_11_R1.DataWatcherObject(
//                                3,
//                                net.minecraft.server.v1_11_R1.DataWatcherRegistry.h
//                            ), true
//                        )
//                    } // 设置名字可见性
//                )
//            ) as Packet<*>)
//        } else {
//            connection.sendPacket(setFields(
//                PacketPlayOutSpawnEntityLiving(),
//                "a" to entityId,
//                "b" to UUID.randomUUID(),
//                "c" to when {
//                    majorLegacy >= 11400 -> IRegistry.ENTITY_TYPE.a(getEntityTypeNMS() as net.minecraft.server.v1_16_R1.EntityTypes<*>)
//                    majorLegacy == 11300 -> net.minecraft.server.v1_13_R2.IRegistry.ENTITY_TYPE.a(getEntityTypeNMS() as net.minecraft.server.v1_13_R2.EntityTypes<*>)
//                    else -> 54
//                },
//                "d" to location.x,
//                "e" to location.y,
//                "f" to location.z,
//                "g" to 0,
//                "h" to 0,
//                "i" to 0,
//                "j" to (location.yaw * 256.0f / 360.0f).toInt().toByte(),
//                "k" to (location.pitch * 256.0f / 360.0f).toInt().toByte(),
//                "l" to (location.yaw * 256.0f / 360.0f).toInt().toByte(),
//                "m" to if (majorLegacy >= 11500) null else DataWatcher(null)
//            )as Packet<*>)
//            connection.sendPacket(setFields(
//                PacketPlayOutEntityMetadata(),
//                "a" to entityId,
//                "b" to listOf(
//                    DataWatcher.Item(DataWatcherObject(2, DataWatcherRegistry.d), "Zombiex") // 设置名字
//                )
//            ) as Packet<*>)
//            connection.sendPacket(setFields(
//                PacketPlayOutEntityMetadata(),
//                "a" to entityId,
//                "b" to listOf(
//                    if (MinecraftVersion.major >= 5) {
//                        DataWatcher.Item(DataWatcherObject(3, DataWatcherRegistry.i), true)
//                    } else {
//                        net.minecraft.server.v1_11_R1.DataWatcher.Item(
//                            net.minecraft.server.v1_11_R1.DataWatcherObject(
//                                3,
//                                net.minecraft.server.v1_11_R1.DataWatcherRegistry.h
//                            ), true
//                        )
//                    } // 设置名字可见性
//                )
//            ) as Packet<*>)
//        }
//        info("检测 spawnEntity - x3")
    }

//    fun getEntityTypeNMS(): Any {
//        return if (MinecraftVersion.major >= 5) {
//            net.minecraft.server.v1_16_R1.EntityTypes::class.java.getProperty<Any>("ZOMBIE", fixed = true)!!
//        } else {
//            54
//        }
//    }
//
//    fun setFields(any: Any, vararg fields: Pair<String, Any?>): Any {
//        fields.forEach { (key, value) ->
//            if (value != null) {
//                any.setProperty(key, value)
//            }
//        }
//        return any
//    }
}