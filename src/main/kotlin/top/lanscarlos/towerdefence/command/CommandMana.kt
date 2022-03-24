package top.lanscarlos.towerdefence.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.TowerDefence
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.mana += arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.mana = arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.mana -= arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.maxMana += arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.maxMana = arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.maxMana -= arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.recoverSpeed += arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.recoverSpeed = arg.toDouble()
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
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-1))!!
                        player.meta.recoverSpeed -= arg.toDouble()
                        sender.sendLang("Command-ResMana-Sub-Success", player.name, arg)
                    }
                }
            }
        }
    }

    @CommandBody
    val cast = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            dynamic(commit = "skill") {
                dynamic(commit = "mana") {
                    dynamic(commit = "args") {
                        execute<CommandSender> { sender, context, arg ->
                            val player = Bukkit.getPlayerExact(context.argument(-3))!!
                            if (player.meta.mana < context.argument(-1).toDouble()) {
                                sender.sendLang("Command-Cast-Failed", player.name, context.argument(-1))
                                return@execute
                            }
                            player.meta.mana -= context.argument(-1).toDouble()
                            TowerDefence.magicAPI!!.cast(context.argument(-2), arg.split(" ").toTypedArray(), sender, player)
                            sender.sendLang("Command-Cast-Success", player.name, context.argument(-2))
                        }
                    }
                    execute<CommandSender> { sender, context, arg ->
                        val player = Bukkit.getPlayerExact(context.argument(-2))!!
                        if (player.meta.mana < arg.toDouble()) {
                            sender.sendLang("Command-Cast-Failed", player.name, arg)
                            return@execute
                        }
                        player.meta.mana -= arg.toDouble()
                        TowerDefence.magicAPI!!.cast(context.argument(-1), arrayOf(), sender, player)
                        sender.sendLang("Command-Cast-Success", player.name, context.argument(-1))
                    }
                }
            }
        }
    }

}