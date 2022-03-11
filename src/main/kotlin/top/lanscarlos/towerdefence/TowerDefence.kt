package top.lanscarlos.towerdefence

import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import taboolib.common.platform.Plugin
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.towerdefence.internal.Context
import top.lanscarlos.towerdefence.internal.Game
import top.lanscarlos.towerdefence.internal.Region
import top.lanscarlos.towerdefence.nms.NMSHandler

object TowerDefence : Plugin() {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    @Config("config.yml")
    lateinit var config: Configuration
        private set

    override fun onEnable() {
        Context.load()
        Region.load()
        Game.load()
        info("Successfully running ExamplePlugin!")
    }
}