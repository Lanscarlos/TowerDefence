package top.lanscarlos.towerdefence.command

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.internal.Game
import top.lanscarlos.towerdefence.internal.Game.Companion.forceQuitGame
import top.lanscarlos.towerdefence.internal.Game.Companion.games
import top.lanscarlos.towerdefence.internal.Game.Companion.inGame
import top.lanscarlos.towerdefence.internal.Game.Companion.joinGame
import top.lanscarlos.towerdefence.internal.Game.Companion.quitGame

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.command
 *
 * @author Lanscarlos
 * @since 2022-03-09 11:32
 */

@CommandHeader(name = "towerdefence", aliases = ["td"], permission = "towerdefence.command")
object CommandMain {

    @CommandBody
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            sender.sendLang("Config-Reload-Succeeded")
        }
    }

    @CommandBody
    val region = CommandRegion.command

    @CommandBody
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

    @CommandBody
    val quit = subCommand {
        literal("force") {
            execute<Player> { sender, _, _ ->
                sender.forceQuitGame()
            }
        }
        execute<Player> { sender, _, _ ->
            sender.quitGame()
        }
    }

}