package top.lanscarlos.towerdefence.command

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.database.Database
import top.lanscarlos.towerdefence.internal.Game
import top.lanscarlos.towerdefence.internal.Occupation
import top.lanscarlos.towerdefence.internal.Region
import top.lanscarlos.towerdefence.internal.WorkSpace

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.command
 *
 * @author Lanscarlos
 * @since 2022-03-11 19:59
 */
object CommandOccupation {

    val command = subCommand {
        literal("give", literal = give)
        literal("remove", literal = remove)
        literal("levelup", literal = levelup)
    }

    private val give: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "player") {
            suggestion<ProxyCommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            dynamic(commit = "occupation") {
                suggestion<ProxyCommandSender> { _, context ->
                    val player = Bukkit.getPlayer(context.argument(-1)) ?: error("Not Player: !!!${context.argument(-1)}")
                    val occupations = Database.getOccupations(player)
                    Occupation.cache.values.filter { it !in occupations }.map { it.id }
                }
                execute<ProxyCommandSender> { sender, context, arg ->
                    val player = Bukkit.getPlayer(context.argument(-1)) ?: error("Not Player: !!!${context.argument(-1)}")
                    Occupation.get(arg)?.also {
                        Database.insertOccupation(player, it)
                        sender.sendLang("Occupation-Give", player.name, arg)
                    } ?: sender.sendLang("Occupation-Not-Found", arg)

                }
            }
        }
    }

    private val remove: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "player") {
            dynamic(commit = "occupation") {
                suggestion<ProxyCommandSender> { _, context ->
                    val player = Bukkit.getPlayer(context.argument(-1)) ?: error("Not Player: !!!${context.argument(-1)}")
                    Database.getOccupations(player).map { it.id }
                }
                execute<ProxyCommandSender> { sender, context, arg ->
                    val player = Bukkit.getPlayer(context.argument(-1)) ?: error("Not Player: !!!${context.argument(-1)}")
                    Occupation.get(arg)?.also {
                        Database.deleteOccupation(player, it)
                        sender.sendLang("Occupation-Remove", player.name, arg)
                    } ?: sender.sendLang("Occupation-Not-Found", arg)
                }
            }
        }
    }

    private val levelup: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "player") {
            dynamic(commit = "occupation") {
                suggestion<ProxyCommandSender> { _, context ->
                    val player = Bukkit.getPlayer(context.argument(-1)) ?: error("Not Player: !!!${context.argument(-1)}")
                    Database.getOccupations(player).map { it.id }
                }
                execute<ProxyCommandSender> { sender, context, arg ->
                    val player = Bukkit.getPlayer(context.argument(-1)) ?: error("Not Player: !!!${context.argument(-1)}")
                    Occupation.get(arg)?.also {
                        val level = Database.getData(player, it)?.first ?: error("Player Data is null when execute levelup command！")
                        Database.updateLevel(player, it, level + 1)
                        sender.sendLang("Occupation-Level-Upe", player.name, it.id, level + 1)
                    } ?: sender.sendLang("Occupation-Not-Found", arg)
                }
            }
        }
    }

}