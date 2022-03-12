package top.lanscarlos.towerdefence.nms

import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

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

fun Player.analogEntity(loc: Location, isEnemy: Boolean = false): Entity {
//    NMSHandler.INSTANCE.analogEntity(this, loc)
    return loc.world!!.spawn(loc, ArmorStand::class.java) {
        it.customName = ""
        it.isCustomNameVisible = true
        it.isInvulnerable = true
        it.setGravity(false)
        // 设置装备颜色
        it.setHelmet(ItemStack(Material.LEATHER_HELMET).apply {
                itemMeta = (itemMeta as LeatherArmorMeta).apply { setColor(if (isEnemy) Color.RED else Color.LIME) }
        })
        it.setChestplate(ItemStack(Material.LEATHER_CHESTPLATE).apply {
                itemMeta = (itemMeta as LeatherArmorMeta).apply { setColor(if (isEnemy) Color.RED else Color.LIME) }
        })
        it.setLeggings(ItemStack(Material.LEATHER_LEGGINGS).apply {
                itemMeta = (itemMeta as LeatherArmorMeta).apply { setColor(if (isEnemy) Color.RED else Color.LIME) }
        })
        it.setBoots(ItemStack(Material.LEATHER_BOOTS).apply {
                itemMeta = (itemMeta as LeatherArmorMeta).apply { setColor(if (isEnemy) Color.RED else Color.LIME) }
        })

//        it.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING)
//        it.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING)
//        it.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING)
//        it.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING)
//        it.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING)
//        it.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING)
    }
}

fun Player.analogEntityDie(entity: Entity) {
    NMSHandler.INSTANCE.analogDie(this, entity)
}

fun Player.setBorderWarning() {
    NMSHandler.INSTANCE.setBorderWarning(this)
}

fun Player.resetBorderWarning() {
    NMSHandler.INSTANCE.resetBorderWarning(this)
}