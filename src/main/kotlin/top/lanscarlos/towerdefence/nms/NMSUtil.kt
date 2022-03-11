package top.lanscarlos.towerdefence.nms

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.nms
 *
 * @author Lanscarlos
 * @since 2022-03-09 11:22
 */

fun Player.setBorder(loc: Location, size: Double) {
    NMSHandler.INSTANCE.setBorder(this, loc.world!!, loc.x, loc.z, size)
}

fun Player.setBorder(world: World, x: Double, z: Double, size: Double) {
    NMSHandler.INSTANCE.setBorder(this, world, x, z, size)
}

fun Player.resetBorder(world: World = this.world) {
    NMSHandler.INSTANCE.resetBorder(this, world)
}

fun Player.analogEntity(loc: Location): Entity {
//    NMSHandler.INSTANCE.analogEntity(this, loc)
    return loc.world!!.spawn(loc, ArmorStand::class.java) {
        it.customName = ""
        it.isCustomNameVisible = true
        it.isInvulnerable = true
        it.setGravity(false)
    }
}