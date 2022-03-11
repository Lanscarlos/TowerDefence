package top.lanscarlos.towerdefence.command

import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.asLangText
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.internal.Region
import top.lanscarlos.towerdefence.internal.WorkSpace
import top.lanscarlos.towerdefence.utils.toDouble
import top.lanscarlos.towerdefence.utils.toInt

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.command
 *
 * @author Lanscarlos
 * @since 2022-03-09 20:02
 */
object CommandRegion {

    val command = subCommand {
        literal("create", literal = create)
        literal("edit", literal = edit)
        literal("move", literal = move)
        literal("center", literal = center)
        literal("size", literal = size)
        literal("exit", literal = exit)
        literal("mob", literal = mob)
        literal("player", literal = player)
    }

    private val create: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "arg") {
            execute<Player> { sender, _, arg ->
                if (arg in Region.regions) {
                    sender.sendLang("Region-Already-Existed")
                    return@execute
                }
                WorkSpace.create(sender, arg, sender.location)
                sender.sendLang("WorkSpace-Create-Success", arg)
            }
        }
    }

    private val edit: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "arg") {
            suggestion<Player> { _, _ ->
                val editing = WorkSpace.cache.values.map { it.region.id }
                Region.regions.keys.filter { it !in editing }.toList()
            }
            execute<Player> { sender, _, arg ->
                Region.get(arg)?.let {
                    WorkSpace.edit(sender, it)
                    sender.sendLang("WorkSpace-Edit-Success", arg)
                } ?: sender.sendLang("Region-Not-Found", arg)
            }
        }
    }

    private val move: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "distance") {
            execute<Player> { sender, _, arg ->
                val msg = WorkSpace.get(sender)?.move(arg.toDouble(0.0)) ?: sender.asLangText("WorkSpace-Not-Found")
                sender.sendMessage(msg)
            }
        }
    }

    private val center: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "x", optional = true) {
            dynamic(commit = "z", optional = true) {
                execute<Player> { sender, context, arg ->
                    val msg = WorkSpace.get(sender)?.let {
                        it.center(context.argument(-1).toDouble(0.0), arg.toDouble(0.0))
                        sender.asLangText("WorkSpace-Center-Success", it.region.id, "%.2f".format(it.center.x), "%.2f".format(it.center.z))
                    } ?: sender.asLangText("WorkSpace-Not-Found")
                    sender.sendMessage(msg)
                }
            }
        }
        execute<Player> { sender, _, _ ->
            val msg = WorkSpace.get(sender)?.let {
                it.center(sender.location.x, sender.location.z)
                sender.asLangText("WorkSpace-Center-Success", it.region.id, it.center.x, it.center.z)
            } ?: sender.asLangText("WorkSpace-Not-Found")
            sender.sendMessage(msg)
        }
    }

    private val size: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "size") {
            execute<Player> { sender, _, arg ->
                val msg = WorkSpace.get(sender)?.let {
                    it.size(arg.toDouble(20.0))
                    sender.asLangText("WorkSpace-Size-Success", it.region.id, it.size)
                } ?: sender.asLangText("WorkSpace-Not-Found")
                sender.sendMessage(msg)
            }
        }
    }

    private val exit: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<Player> { sender, _, _ ->
            val msg = WorkSpace.get(sender)?.let {
                it.exit()
                sender.asLangText("WorkSpace-Exit-Success", it.region.id)
            } ?: sender.asLangText("WorkSpace-Not-Found")
            sender.sendMessage(msg)
        }
    }

    private val mob: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<Player> { sender, _, _ ->
            val msg = WorkSpace.get(sender)?.let {
                it.addMobSpawnPoint()
            } ?: sender.asLangText("WorkSpace-Not-Found")
            sender.sendMessage(msg)
        }
    }

    private val player: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<Player> { sender, _, _ ->
            val msg = WorkSpace.get(sender)?.let {
                it.addPlayerSpawnPoint()
            } ?: sender.asLangText("WorkSpace-Not-Found")
            sender.sendMessage(msg)
        }
    }

}