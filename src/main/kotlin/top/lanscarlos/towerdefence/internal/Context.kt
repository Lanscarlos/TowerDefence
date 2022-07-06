package top.lanscarlos.towerdefence.internal

import taboolib.common.platform.function.console
import taboolib.module.lang.asLangText
import top.lanscarlos.towerdefence.TowerDefence
import top.lanscarlos.towerdefence.database.Database
import top.lanscarlos.towerdefence.utils.timing

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.internal
 *
 * @author Lanscarlos
 * @since 2022-03-11 16:58
 */
object Context {
    var Force_Adventure_Mode = true
        private set
    var Default_Occupation = "example-occupation"
        private set
    var Exp_Conversion_Efficiency = 0.5
        private set

    fun load(): String {
        return try {
            val start = timing()
            val config = TowerDefence.conf
            Force_Adventure_Mode = config.getBoolean("game-setting.force-adventure-mode")
            Default_Occupation = config.getString("game-setting.default-occupation") ?: Default_Occupation
            Exp_Conversion_Efficiency = config.getDouble("game-setting.exp-conversion-efficiency", Exp_Conversion_Efficiency)

            Database.setup(config)

            console().asLangText("Config-Load-Succeeded", timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Config-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }
}