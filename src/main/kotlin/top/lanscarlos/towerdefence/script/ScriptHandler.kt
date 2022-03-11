package top.lanscarlos.towerdefence.script

import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.kether.KetherShell
import top.lanscarlos.towerdefence.event.*
import java.util.concurrent.CompletableFuture

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.script
 *
 * @author Lanscarlos
 * @since 2022-03-11 19:34
 */
object ScriptHandler {

    @SubscribeEvent
    fun e(e: PlayerJoinGameEvent) {
        eval(e.player, e.game.script["join"], mapOf(
            "event" to e,
            "game" to e.game,
            "player" to e.player,
        ))
    }

    @SubscribeEvent
    fun e(e: PlayerQuitGameEvent) {
        eval(e.player, e.game.script["quit"], mapOf(
            "event" to e,
            "game" to e.game,
            "player" to e.player,
        ))
    }

    @SubscribeEvent
    fun e(e: GameStartEvent) {
        e.game.cache.keys.forEach {
            eval(it, e.game.script["start"], mapOf(
                "event" to e,
                "game" to e.game,
                "player" to it,
            ))
        }
    }

    @SubscribeEvent
    fun e(e: GameStopEvent) {
        e.game.cache.keys.forEach {
            eval(it, e.game.script["stop"], mapOf(
                "event" to e,
                "game" to e.game,
                "player" to it,
            ))
        }
    }

    @SubscribeEvent
    fun e(e: GameEndEvent) {
        e.game.cache.keys.forEach {
            eval(it, e.game.script["end"], mapOf(
                "event" to e,
                "game" to e.game,
                "player" to it,
            ))
        }
    }

    @SubscribeEvent
    fun e(e: PlayerKillMobEvent) {
        eval(e.player, e.game.script["kill"], mapOf(
            "event" to e,
            "game" to e.game,
            "player" to e.player,
            "entity" to e.entity,
        ))
    }

    @SubscribeEvent
    fun e(e: PlayerDeathEvent) {
        eval(e.player, e.game.script["death"], mapOf(
            "event" to e,
            "game" to e.game,
            "player" to e.player,
        ))
    }

    @SubscribeEvent
    fun e(e: GameVictoryEvent) {
        e.game.cache.keys.forEach {
            eval(it, e.game.script["victory"], mapOf(
                "event" to e,
                "game" to e.game,
                "player" to it,
            ))
        }
    }

    @SubscribeEvent
    fun e(e: GameDefeatedEvent) {
        e.game.cache.keys.forEach {
            eval(it, e.game.script["defeated"], mapOf(
                "event" to e,
                "game" to e.game,
                "player" to it,
            ))
        }
    }

    fun eval(player: Player, script: String?, data: Map<String, Any?> = mapOf()): CompletableFuture<Any?> {
        if (script == null || script.isEmpty()) return CompletableFuture.completedFuture(false)
        return try {
            KetherShell.eval(script, sender = adaptCommandSender(player), context= {
                data.forEach { (k, v) ->
                    if (v != null) set(k, v)
                }
            })
        } catch (e: Exception) {
            CompletableFuture.completedFuture(false)
        }
    }

}