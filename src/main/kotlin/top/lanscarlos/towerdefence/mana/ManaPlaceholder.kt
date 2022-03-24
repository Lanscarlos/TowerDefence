package top.lanscarlos.towerdefence.mana

import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion
import top.lanscarlos.towerdefence.mana.ManaData.Companion.meta

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.mana
 *
 * @author Lanscarlos
 * @since 2022-03-22 19:46
 */
object ManaPlaceholder : PlaceholderExpansion {

    override val identifier: String
        get() = "mana"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val meta = player?.meta ?: return "UNKNOWN_PLAYER"
        return when (args) {
            "mana" -> meta.mana.format()
            "maxmana" -> meta.maxMana.format()
            "resmana" -> meta.recoverSpeed.format()
            else -> "UNKNOWN_ARG"
        }
    }

    fun Double.format(): String {
        return "%.2f".format(this)
    }

}