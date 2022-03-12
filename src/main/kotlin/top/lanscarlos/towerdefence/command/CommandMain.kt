package top.lanscarlos.towerdefence.command

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.internal.Context
import top.lanscarlos.towerdefence.internal.Game
import top.lanscarlos.towerdefence.internal.Game.Companion.forceQuitGame
import top.lanscarlos.towerdefence.internal.Game.Companion.games
import top.lanscarlos.towerdefence.internal.Game.Companion.inGame
import top.lanscarlos.towerdefence.internal.Game.Companion.joinGame
import top.lanscarlos.towerdefence.internal.Game.Companion.quitGame
import top.lanscarlos.towerdefence.internal.Region
import top.lanscarlos.towerdefence.nms.NMSHandler

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.command
 *
 * @author Lanscarlos
 * @since 2022-03-09 11:32
 */

@CommandHeader(name = "towerdefence", aliases = ["td"], permission = "towerdefence.command")
object CommandMain {

    @CommandBody(permission = "towerdefence.command.reload")
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            Context.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
            Region.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
            Game.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    @CommandBody(permission = "towerdefence.command.region")
    val region = CommandRegion.command

    @CommandBody(permission = "towerdefence.command.game")
    val game = CommandGame.command

    @CommandBody(permission = "towerdefence.command.join")
    val join = subCommand {
        dynamic {
            suggestion<Player> { sender, _ ->
                val game = sender.inGame()?.id
                games.values.filter { !it.isInGame() && it.id != game }.map { it.id }
            }
            execute<Player> { sender, _, arg ->
                sender.joinGame(arg)
            }
        }
    }

    @CommandBody(permission = "towerdefence.command.quit")
    val quit = subCommand {
        literal("force", permission = "towerdefence.command.quit.force") {
            execute<Player> { sender, _, _ ->
                sender.forceQuitGame()
            }
        }
        execute<Player> { sender, _, _ ->
            sender.quitGame()
        }
    }

}