package top.lanscarlos.towerdefence

import com.elmakers.mine.bukkit.api.magic.MagicAPI
import org.bukkit.Bukkit
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import taboolib.common.platform.Plugin
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.Language
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.towerdefence.internal.Context
import top.lanscarlos.towerdefence.internal.Game
import top.lanscarlos.towerdefence.internal.Occupation
import top.lanscarlos.towerdefence.internal.Region
import top.lanscarlos.towerdefence.nms.NMSHandler
import top.lanscarlos.towerdefence.utils.ifNotExists
import top.lanscarlos.towerdefence.utils.toConfig
import java.io.File

object TowerDefence : Plugin() {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val magicAPI by lazy {
        Bukkit.getPluginManager().getPlugin("Magic") as? MagicAPI
    }

    @Config("config.yml")
    lateinit var conf: Configuration
        private set

    override fun onEnable() {
        Language.default = "zh_CN"

        conf = File(plugin.dataFolder, "config.yml").ifNotExists {
            releaseResourceFile("config.yml", true)
        }.toConfig()

        Context.load()
        Region.load()
        Occupation.load()
        Game.load()
        info("Successfully running ExamplePlugin!")
    }
}