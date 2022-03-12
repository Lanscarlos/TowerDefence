package top.lanscarlos.towerdefence.internal

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.*
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap
import taboolib.module.lang.asLangText
import taboolib.platform.util.sendLang
import top.lanscarlos.towerdefence.TowerDefence
import top.lanscarlos.towerdefence.event.*
import top.lanscarlos.towerdefence.nms.*
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
    val respawn = config.getInt("respawn")
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

        inGame[player] = this

        cache.keys.forEach {
            it.sendLang("Game-Join-Broadcast", player.name, display)
        }

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
        inGame.remove(player)
        cache.remove(player)?.let {
            player.gameMode = it.mode
            it.player.resetBorder()
            player.resetBorderWarning()
            player.teleport(it.loc)
            player.sendLang("Game-Quit-Success", display)
        }
        cache.keys.forEach {
            it.sendLang("Game-Quit-Broadcast", player.name, display)
        }
    }

    /**
     * 尝试开始倒计时
     * */
    fun tryStart() {
        if (isInCountdown() || isInGame()) return // 倒计时中 或 游戏中

        if (cache.size < minPlayer) {
            // 人数不足
            val require = minPlayer - cache.size
            cache.keys.forEach {
                it.sendLang("Game-Require-Player", require)
            }
            return
        } else {
            cache.keys.forEach {
                it.sendLang("Game-Ready-Start", display, countdown / 20)
            }
        }

        state = State.Countdown

        schedule = countdown
        maxPoint = min(maxPoint, cache.size)
        task = submit(period = 1) {
            schedule -= 1
            if (cache.size < minPlayer) {
                // 人数不足
                val require = minPlayer - cache.size
                cache.keys.forEach {
                    it.sendLang("Game-Require-Player", require)
                }
                task = null
                state = State.Idle
                cancel()
                return@submit
            }
            if (schedule <= 0) {
                // 倒计时完成、开始游戏
                task = null
                cancel()
                start()
                return@submit
            }
            if (schedule % 20 == 0) {
                val second = schedule / 20
                cache.keys.forEach {
                    it.sendLang("Game-PreStart-Countdown", second)
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

        state = State.InGame

        // 传送至出生点
        cache.values.forEach {
            it.player.teleport(region.playersSpawn[it.index])
            it.player.sendLang("Game-Start", display)
        }

        schedule = 0
        task = submit(period = 1) {
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
                cache.values.forEach {
                    if (it.respawn > 0) return@forEach
                    it.player.sendLang("Game-Process-Countdown", second)
                }
            }
        }
    }

    /**
     * 重开游戏
     * */
    fun restart(): String {
        if (!isInGame()) return console().asLangText("Game-Admin-Restart-Failed", display)
        task?.cancel()
        // 移除所有实体
        entities.forEach {
            if (it.isDead) return@forEach
            it.remove()
        }
        entities.clear()

        // 重置刷怪器延迟
        mobSpawners.forEach {
            it.wait = 0
        }

        // 重置数据
        cache.values.forEach {
            it.respawn = 0
            it.death = 0
            it.killed = 0
            // 模式转换
            it.player.gameMode = if (Context.Force_Adventure_Mode) {
                GameMode.ADVENTURE
            } else {
                it.mode
            }
            //传送到出生点
            it.player.teleport(region.playersSpawn[it.index])
            // 设置场地边界
            it.player.setBorder(region.center, region.size)
            it.player.resetBorderWarning()
        }
        state = State.Idle
        tryStart()

        return console().asLangText("Game-Admin-Restart-Success", display)
    }

    /**
     * 终止游戏
     * */
    fun stop(): String {

        if (!isInGame()) return console().asLangText("Game-Admin-Stop-Failed", display)

        GameStopEvent(this).call()

        state = State.Idle

        task?.cancel()

        // 移除所有实体
        entities.forEach {
            if (it.isDead) return@forEach
            it.remove()
        }
        entities.clear()

        // 恢复玩家的原属性
        cache.values.forEach {
            inGame.remove(it.player)
            it.player.gameMode = it.mode
            it.player.resetBorder()
            it.player.resetBorderWarning()
            it.player.teleport(it.loc)
        }
        maxPoint = min(region.mobSpawn.size, mobSpawners.size)

        end()

        cache.clear()

        return console().asLangText("Game-Admin-Stop-Success", display)
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
        var respawn: Int = 0, // 重生冷却时间
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
                folder.ifNotExists {
                    releaseResourceFile("games/#def.yml", true)
                }.getFiles().forEach { file ->
                    val config = file.toConfig()
                    val region = Region.get(config.getString("region") ?: "null") ?: let {
                        warning("Region \"${config.getString("region")}\" is undefined in Game \"${file.nameWithoutExtension}\"!")
                        return@forEach
                    }
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
            val game = e.entity.killer!!.inGame() ?: return
            // 禁止怪物掉落
            e.drops.clear()
            game.cache[e.entity.killer]!!.killed += 1
            PlayerKillMobEvent(game, e.entity.killer!!, e.entity).call()
            game.removeEntity(e.entity)
        }

        /**
         * 处理玩家死亡
         * */
        @SubscribeEvent
        fun e(e: EntityDamageEvent) {
            val player = e.entity as? Player ?: return
            val game = player.inGame() ?: return

            // 判断是否为玩家造成的攻击
            if (e is EntityDamageByEntityEvent) {
                val damager = (e.damager as? Player) ?: ((e.damager as? Projectile)?.shooter as? Player) ?: return
                if (damager.isInGame()) {
                    // 取消攻击
                    e.isCancelled = true
                    return
                }
            }


            // 阻止玩家死亡
            if (player.health - e.finalDamage <= 0.0) {
                e.isCancelled = true

                game.cache.keys.forEach {
                    // 模拟死亡
                    if (it == player) {
                        it.playSound(it.location, Sound.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.0f)
                    } else it.analogEntityDie(player)
                }

                player.gameMode = GameMode.SPECTATOR

                // 模拟血色警告框
                player.setBorderWarning()
                player.sendLang("Player-Death")
                // 数据处理
                val data = game.cache[e.entity]!!
                data.death += 1
                top.lanscarlos.towerdefence.event.PlayerDeathEvent(game, player).call()
                data.respawn += game.respawn
                // 恢复生命
                player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                player.foodLevel = 20
                submit(period = 1) {
                    if (!player.isInGame()) {
                        // 玩家已退出游戏
                        player.resetBorderWarning()
                        cancel()
                        return@submit
                    }
                    if (--data.respawn <= 0) {
                        // 重生
                        PlayerRespawnEvent(game, player).call()
                        player.resetBorderWarning()
                        player.teleport(game.region.playersSpawn[data.index])
                        // 模式转换
                        player.gameMode = if (Context.Force_Adventure_Mode) {
                            GameMode.ADVENTURE
                        } else {
                            data.mode
                        }
                        player.sendLang("Player-Respawn")
                        cancel()
                        return@submit
                    }
                    // 倒计时显示
                    if (data.respawn % 20 == 0) {
                        val second = data.respawn / 20
                        player.sendLang("Game-Respawn-Countdown", second)
                    }
                }
            }
        }

        /**
         * 禁止玩家之间互相伤害
         * */
        @SubscribeEvent
        fun e(e: EntityDamageByEntityEvent) {
            if ((e.entity as? Player)?.isInGame() != true) return // 非游戏玩家
            val damager = (e.damager as? Player) ?: ((e.damager as? Projectile)?.shooter as? Player) ?: return
            if (damager.isInGame()) {
                // 取消攻击
                e.isCancelled = true
            }
        }

        /**
         * 禁止场内实体丢弃物品
         * */
        @SubscribeEvent
        fun e(e: PlayerPickupItemEvent) {
            if (e.player.isInGame()) e.isCancelled = true
        }

        /**
         * 禁止场内玩家丢弃物品
         * */
        @SubscribeEvent
        fun e(e: PlayerDropItemEvent) {
            if (e.player.isInGame()) e.isCancelled = true
        }

        /**
         * 禁止场内玩家破坏方块
         * */
        @SubscribeEvent
        fun e(e: BlockBreakEvent) {
            if (e.player.isInGame()) e.isCancelled = true
        }

        /**
         * 禁止场内玩家防止方块
         * */
        @SubscribeEvent
        fun e(e: BlockPlaceEvent) {
            if (e.player.isInGame()) e.isCancelled = true
        }

        @Awake(LifeCycle.DISABLE)
        fun onDisable() {
            games.values.forEach {
                if (it.isInGame()) it.stop()
            }
        }

    }

}