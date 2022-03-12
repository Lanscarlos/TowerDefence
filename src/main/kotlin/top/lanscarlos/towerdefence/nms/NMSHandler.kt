package top.lanscarlos.towerdefence.nms

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.nms
 *
 * @author Lanscarlos
 * @since 2022-03-03 15:03
 */
interface NMSHandler {

    fun setBorder(player: Player, world: World, x: Double, z: Double, size: Double)

    fun resetBorder(player: Player, world: World = player.world)

    fun setBorderWarning(player: Player)

    fun resetBorderWarning(player: Player)

    fun analogDie(player: Player, entity: Entity)

//    fun analogDieScreen(player: Player)

//    /**
//     * 模拟实体
//     * */
//    fun analogEntity(player: Player, location: Location)

    companion object {
        val INSTANCE by lazy {
            nmsProxy<NMSHandler>()
        }
    }
}