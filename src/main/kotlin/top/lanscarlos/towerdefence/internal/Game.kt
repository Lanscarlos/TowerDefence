package top.lanscarlos.towerdefence.internal

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap
import taboolib.module.lang.asLangText
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.TowerDefence
import top.lanscarlos.towerdefence.event.*
import top.lanscarlos.towerdefence.nms.resetBorder
import top.lanscarlos.towerdefence.nms.setBorder
import top.lanscarlos.towerdefence.utils.*
import java.io.File
import kotlin.math.min

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.internal
 *
 * @author Lanscarlos
 * @since 2022-03-09 11:36
 */
class Game(
    val id: String,
    val config: Configuration,
    val region: Region
) {

    val display = config.getString("display") ?: id
    val time = config.getInt("time")
    val minPlayer = config.getInt("min-player")
    val countdown = config.getInt("countdown")
    val threshold = config.getInt("threshold")
    val period = config.getInt("schedule.period")
    val mobSpawners: List<MobSpawner> = config.getMapList("mobs-spawn").map {
        MobSpawner(
            this,
            it["start-time"].toInt(0),
            it["end-time"].toInt(1200),
            it["interval"].toInt(200),
            it["mob"]?.toString() ?: "def",
            it["amount"].toInt(1),
        )
    }
    val script: Map<String, String> = config.getMap("script")
    private val entities = mutableSetOf<Entity>()

    var maxPoint = min(region.mobSpawn.size, mobSpawners.size) // 最大刷怪点数量
    var state = State.Idle // 游戏状态
    var schedule = 0
    var task: PlatformExecutor.PlatformTask? = null
    val cache = mutableMapOf<Player, PlayerCache>() // player to 游戏前位置

    fun isInGame(): Boolean {
        return state == State.InGame
    }

    fun isInCountdown(): Boolean {
        return state == State.Countdown
    }

    fun isInIdle(): Boolean {
        return state == State.Idle
    }

    /**
     * 玩家加入游戏
     * */
    fun join(player: Player) {
        if (isInGame()) {
            // 游戏已经开始
            player.sendLang("Game-Join-In-Game", display)
            return
        }
        if (cache.size >= region.playersSpawn.size) {
            // 玩家人数已满
            player.sendLang("Game-Join-Full", display)
            return
        }

        if (!PlayerJoinGameEvent(this, player).call()) return

        // 获取玩家游戏出生点
        val loc = region.playersSpawn[cache.size]
        cache[player] = PlayerCache(cache.size, player)

        // 模式转换
        if (Context.Force_Adventure_Mode) {
            player.gameMode = GameMode.ADVENTURE
        }

        //传送到出生点
        player.teleport(loc)
        // 设置场地边界
        player.setBorder(region.center, region.size)
        player.sendLang("Game-Join-Success", display)

        tryStart()
    }

    /**
     * 玩家离开游戏
     * */
    fun quit(player: Player) {
        if (player !in cache) return
        if (isInGame()) {
            // 游戏中
            player.sendLang("Game-Quit-Failed", display)
            return
        }
        forceQuit(player)
    }

    /**
     * 强制退出
     * */
    fun forceQuit(player: Player) {
        PlayerQuitGameEvent(this, player).call()

        cache.remove(player)!!.let {
            player.gameMode = it.mode
            player.teleport(it.loc)
        }
        player.sendLang("Game-Quit-Success", display)
    }

    /**
     * 尝试开始倒计时
     * */
    fun tryStart() {
        if (isInCountdown() || cache.size < minPlayer) return // 倒计时中 或 人数不足
        schedule = countdown
        maxPoint = min(maxPoint, cache.size)
        task = submit(period = 1) {
            schedule -= 1
            if (cache.size < minPlayer) {
                // 人数不足
                task = null
                cancel()
                return@submit
            }
            if (schedule <= 0) {
                // 倒计时完成、开始游戏
                task = null
                start()
                cancel()
                return@submit
            }
            if (schedule % 20 == 0) {
                val second = schedule / 20
                cache.keys.forEach {
                    it.sendLang("Game-PreStart-Count-Down", second)
                }
            }
        }
    }

    /**
     * 开始游戏
     * */
    fun start() {
        if (cache.size < minPlayer) return // 人数不足

        if (!GameStartEvent(this).call()) return

        cache.keys.forEach {
            it.sendLang("Game-Start")
        }

        schedule = 0
        val task = submit(period = 1) {
            schedule += 1
            if (cache.isEmpty()) {
                // 场上无玩家
                stop()
                cancel()
                return@submit
            }
            if (schedule >= time) {
                // 游戏结束
                victory()
                cancel()
                return@submit
            }
            mobSpawners.forEach {
                it.tick(schedule, maxPoint)
            }
            if (schedule % 20 == 0) {
                if (entities.filter { !it.isDead }.size > threshold) {
                    // 场上敌人数量超过阈值，游戏结束
                    defeated()
                    cancel()
                    return@submit
                }
                val second = (time - schedule) / 20
                cache.keys.forEach {
                    it.sendLang("Game-Process-Count-Down", second)
                }
            }
        }
    }

    /**
     * 终止游戏
     * */
    fun stop() {
        task?.cancel()

        // 移除所有实体
        entities.forEach {
            if (it.isDead) return@forEach
            it.remove()
        }
        entities.clear()

        // 恢复玩家的原属性
        cache.values.forEach {
            it.player.gameMode = it.mode
            it.player.resetBorder()
            it.player.teleport(it.loc)
        }
        maxPoint = min(region.mobSpawn.size, mobSpawners.size)

        end()
    }

    /**
     * 游戏胜利
     * */
    fun victory() {
        GameVictoryEvent(this).call()
        cache.keys.forEach {
            it.sendLang("Game-Victory", display)
        }
        stop()
    }

    /**
     * 游戏失败
     * */
    fun defeated() {
        GameDefeatedEvent(this).call()
        cache.keys.forEach {
            it.sendLang("Game-Defeated", display)
        }
        stop()
    }

    /**
     * 游戏结束
     * */
    fun end() {
        GameEndEvent(this).call()
        cache.keys.forEach {
            it.sendLang("Game-End", display)
        }
    }

    fun addEntity(entity: Entity) {
        entities.add(entity)
        Game.entities[entity] = this
    }

    fun removeEntity(entity: Entity) {
        entities.remove(entity)
        Game.entities.remove(entity)
    }

    /**
     * 玩家信息缓存
     * */
    inner class PlayerCache(
        val index: Int,
        val player: Player,
        val loc: Location = player.location, // 游戏前位置
        val mode: GameMode = player.gameMode, // 游戏前模式
        var killed: Int = 0, // 怪物击杀数
        var death: Int = 0, // 死亡次数
    )

    enum class State {
        InGame, // 游戏中
        Idle, // 空闲中
        Countdown, // 游戏倒计时中
    }

    companion object {

        val folder by lazy {
            File(TowerDefence.plugin.dataFolder, "games")
        }

        val games = mutableMapOf<String, Game>()
        val inGame = mutableMapOf<Player, Game>()
        val entities = mutableMapOf<Entity, Game>()

        fun get(id: String): Game? {
            return games[id]
        }

        fun Player.isInGame(): Boolean {
            return this in inGame
        }

        fun Player.inGame(): Game? {
            return inGame[this]
        }

        fun Player.joinGame(id: String) {
            if (isInGame()) {
                sendLang("Game-Join-Failed", inGame()!!.display)
            } else {
                get(id)?.join(this) ?: sendLang("Game-Not-Found", id)
            }
        }

        fun Player.quitGame() {
            val game = inGame() ?: let {
                sendLang("Game-Quit-Not-In-Game")
                return
            }
            game.quit(this)
        }

        fun Player.forceQuitGame() {
            val game = inGame() ?: let {
                sendLang("Game-Quit-Not-In-Game")
                return
            }
            game.forceQuit(this)
        }

        fun load(): String {
            return try {
                games.values.forEach {
                    if (it.isInGame()) it.stop()
                }

                val start = timing()
                folder.getFiles().forEach { file ->
                    val config = file.toConfig()
                    val region = Region.get(config.getString("region") ?: "def") ?: error("Cannot Found Region \"${config.getString("region") ?: "def"}\"!")
                    games[file.nameWithoutExtension] = Game(file.nameWithoutExtension, config, region)
                }
                console().asLangText("Games-Load-Succeeded", games.values.toSet().size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                console().asLangText("Games-Load-Failed", e.localizedMessage).also {
                    console().sendMessage(it)
                }
            }
        }

        /**
         * 处理退出游戏的玩家
         * */
        @SubscribeEvent
        fun e(e: PlayerQuitEvent) {
            val game = e.player.inGame() ?: return
            game.forceQuit(e.player)
        }

        /**
         * 监听玩家击杀生物事件
         * */
        @SubscribeEvent
        fun e(e: EntityDeathEvent) {
            // 此处仅处理非玩家死亡事件
            if (e.entity is Player || e.entity !in entities || e.entity.killer?.isInGame() == false) return
            val game = e.entity.killer!!.inGame()!!
            // 禁止怪物掉落
            e.drops.clear()
            PlayerKillMobEvent(game, e.entity.killer!!, e.entity).call()
            game.removeEntity(e.entity)
        }

        /**
         * 监听玩家死亡事件
         * */
        @SubscribeEvent
        fun e(e: org.bukkit.event.entity.PlayerDeathEvent) {
            if (!e.entity.isInGame()) return
            top.lanscarlos.towerdefence.event.PlayerDeathEvent(e.entity.inGame()!!, e.entity).call()
        }

        /**
         * 禁止玩家之间互相伤害
         * */
        @SubscribeEvent
        fun e(e: EntityDamageByEntityEvent) {
            if (e.entity is Player && (e.damager is Player || (e.damager as? Projectile)?.shooter is Player) ) {
                e.isCancelled = true
            }
        }

        /**
         * 禁止场内实体捡起物品
         * */
        @SubscribeEvent
        fun e(e: EntityPickupItemEvent) {
            if ((e.entity is Player && (e.entity as Player).isInGame()) || e.entity in entities) e.isCancelled = true
        }

        /**
         * 禁止场内实体丢弃物品
         * */
        @SubscribeEvent
        fun e(e: EntityDropItemEvent) {
            if ((e.entity is Player && (e.entity as Player).isInGame()) || e.entity in entities) e.isCancelled = true
        }

        @Awake(LifeCycle.DISABLE)
        fun onDisable() {
            games.values.forEach {
                if (it.isInGame()) it.stop()
            }
        }

    }

}