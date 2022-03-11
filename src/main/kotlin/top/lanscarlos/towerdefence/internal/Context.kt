package top.lanscarlos.towerdefence.internal

import taboolib.common.platform.function.console
import taboolib.module.lang.asLangText
import top.lanscarlos.towerdefence.TowerDefence
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

    fun load(): String {
        return try {
            val start = timing()
            val config = TowerDefence.config

            Force_Adventure_Mode = config.getBoolean("game-setting.force-adventure-mode")

            console().asLangText("Config-Load-Succeeded", timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            console().asLangText("Config-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }
}