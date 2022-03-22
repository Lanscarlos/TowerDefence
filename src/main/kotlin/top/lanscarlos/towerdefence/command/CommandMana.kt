package top.lanscarlos.towerdefence.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import top.lanscarlos.towerdefence.mana.ManaData.Companion.meta

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.command
 *
 * @author Lanscarlos
 * @since 2022-03-22 19:12
 */
@CommandHeader(name = "mu", permission = "towerdefence.command.mana")
object CommandMana {

    /**
     * /mu mana set player mana
     * */
    @CommandBody
    val mana = subCommand {
        literal("add") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.mana += arg.toInt()
                        sender.sendLang("Command-Mana-Add-Success", player.name, arg)
                    }
                }
            }
        }
        literal("set") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.mana = arg.toInt()
                        sender.sendLang("Command-Mana-Set-Success", player.name, arg)
                    }
                }
            }
        }
        literal("sub") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.mana -= arg.toInt()
                        sender.sendLang("Command-Mana-Sub-Success", player.name, arg)
                    }
                }
            }
        }
    }

    @CommandBody
    val maxmana = subCommand {
        literal("add") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.maxMana += arg.toInt()
                        sender.sendLang("Command-MaxMana-Add-Success", player.name, arg)
                    }
                }
            }
        }
        literal("set") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.maxMana = arg.toInt()
                        sender.sendLang("Command-MaxMana-Set-Success", player.name, arg)
                    }
                }
            }
        }
        literal("sub") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.maxMana -= arg.toInt()
                        sender.sendLang("Command-MaxMana-Sub-Success", player.name, arg)
                    }
                }
            }
        }
    }

    @CommandBody
    val resmana = subCommand {
        literal("add") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.recoverSpeed += arg.toInt()
                        sender.sendLang("Command-ResMana-Add-Success", player.name, arg)
                    }
                }
            }
        }
        literal("set") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.recoverSpeed = arg.toInt()
                        sender.sendLang("Command-ResMana-Set-Success", player.name, arg)
                    }
                }
            }
        }
        literal("sub") {
            dynamic(commit = "player") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                dynamic(commit = "arg") {
                    execute<ProxyCommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.recoverSpeed -= arg.toInt()
                        sender.sendLang("Command-ResMana-Sub-Success", player.name, arg)
                    }
                }
            }
        }
    }

}