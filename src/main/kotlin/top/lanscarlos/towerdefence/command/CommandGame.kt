package top.lanscarlos.towerdefence.command

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.internal.Game
import top.lanscarlos.towerdefence.internal.Region
import top.lanscarlos.towerdefence.internal.WorkSpace

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.command
 *
 * @author Lanscarlos
 * @since 2022-03-11 19:59
 */
object CommandGame {

    val command = subCommand {
        literal("stop", literal = stop)
        literal("restart", literal = restart)
    }

    private val stop: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "game") {
            suggestion<ProxyCommandSender> { _, _ ->
                Game.games.values.filter { it.isInGame() }.map { it.id }
            }
            execute<ProxyCommandSender> { sender, _, arg ->
                Game.get(arg)?.stop()?.let { sender.sendMessage(it) } ?: sender.sendLang("Game-Not-Found", arg)
            }
        }
    }

    private val restart: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "game") {
            suggestion<ProxyCommandSender> { _, _ ->
                Game.games.values.filter { it.isInGame() }.map { it.id }
            }
            execute<ProxyCommandSender> { sender, _, arg ->
                Game.get(arg)?.restart()?.let { sender.sendMessage(it) } ?: sender.sendLang("Game-Not-Found", arg)
            }
        }
    }

}