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
import taboolib.platform.util.giveItem
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

    var maxPoint = min(region.mobSpawn.size, mobSpawners.size) // ?????????????????????
    var state = State.Idle // ????????????
    var schedule = 0
    var task: PlatformExecutor.PlatformTask? = null
    val cache = mutableMapOf<Player, PlayerCache>() // player to ???????????????

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
     * ??????????????????
     * */
    fun join(player: Player) {
        if (isInGame()) {
            // ??????????????????
            player.sendLang("Game-Join-In-Game", display)
            return
        }
        if (cache.size >= region.playersSpawn.size) {
            // ??????????????????
            player.sendLang("Game-Join-Full", display)
            return
        }

        if (!PlayerJoinGameEvent(this, player).call()) return

        // ???????????????????????????
        val loc = region.playersSpawn[cache.size]
        cache[player] = PlayerCache(cache.size, player)

        // ????????????
        if (Context.Force_Adventure_Mode) {
            player.gameMode = GameMode.ADVENTURE
        }

        //??????????????????
        player.teleport(loc)
        // ??????????????????
        player.setBorder(region.center, region.size)

        inGame[player] = this

        cache.keys.forEach {
            it.sendLang("Game-Join-Broadcast", player.name, display)
        }

        player.sendLang("Game-Join-Success", display)

        tryStart()
    }

    /**
     * ??????????????????
     * */
    fun quit(player: Player) {
        if (player !in cache) return
        if (isInGame()) {
            // ?????????
            player.sendLang("Game-Quit-Failed", display)
            return
        }
        forceQuit(player)
    }

    /**
     * ????????????
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
     * ?????????????????????
     * */
    fun tryStart() {
        if (isInCountdown() || isInGame()) return // ???????????? ??? ?????????

        if (cache.size < minPlayer) {
            // ????????????
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
                // ????????????
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
                // ??????????????????????????????
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
     * ????????????
     * */
    fun start() {
        if (cache.size < minPlayer) return // ????????????

        if (!GameStartEvent(this).call()) return

        state = State.InGame

        // ??????????????????
        cache.values.forEach {

            if (it.occupation == null) {
                it.occupation = Occupation.get(Context.Default_Occupation)
            }
            // ??????????????????
            val item = it.occupation?.buildItem(it.player)
            item?.also { itemStack -> it.player.giveItem(itemStack) } ?: warning("Default_Occupation is null!")

            // ?????????????????????


            // ??????????????????
            it.player.teleport(region.playersSpawn[it.index])
            it.player.sendLang("Game-Start", display)
        }

        schedule = 0
        task = submit(period = 1) {
            schedule += 1
            if (cache.isEmpty()) {
                // ???????????????
                stop()
                cancel()
                return@submit
            }
            if (schedule >= time) {
                // ????????????
                victory()
                cancel()
                return@submit
            }
            mobSpawners.forEach {
                it.tick(schedule, maxPoint)
            }
            if (schedule % 20 == 0) {
                if (entities.filter { !it.isDead }.size > threshold) {
                    // ?????????????????????????????????????????????
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
     * ????????????
     * */
    fun restart(): String {
        if (!isInGame()) return console().asLangText("Game-Admin-Restart-Failed", display)
        task?.cancel()
        // ??????????????????
        entities.forEach {
            if (it.isDead) return@forEach
            it.remove()
        }
        entities.clear()

        // ?????????????????????
        mobSpawners.forEach {
            it.wait = 0
        }

        // ????????????
        cache.values.forEach {
            it.respawn = 0
            it.death = 0
            it.killed = 0
            // ????????????
            it.player.gameMode = if (Context.Force_Adventure_Mode) {
                GameMode.ADVENTURE
            } else {
                it.mode
            }
            //??????????????????
            it.player.teleport(region.playersSpawn[it.index])
            // ??????????????????
            it.player.setBorder(region.center, region.size)
            it.player.resetBorderWarning()
        }
        state = State.Idle
        tryStart()

        return console().asLangText("Game-Admin-Restart-Success", display)
    }

    /**
     * ????????????
     * */
    fun stop(): String {

        if (!isInGame()) return console().asLangText("Game-Admin-Stop-Failed", display)

        GameStopEvent(this).call()

        state = State.Idle

        task?.cancel()

        // ??????????????????
        entities.forEach {
            if (it.isDead) return@forEach
            it.remove()
        }
        entities.clear()

        // ????????????????????????
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
     * ????????????
     * */
    fun victory() {
        GameVictoryEvent(this).call()
        cache.keys.forEach {
            it.sendLang("Game-Victory", display)
        }
        stop()
    }

    /**
     * ????????????
     * */
    fun defeated() {
        GameDefeatedEvent(this).call()
        cache.keys.forEach {
            it.sendLang("Game-Defeated", display)
        }
        stop()
    }

    /**
     * ????????????
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
     * ??????????????????
     * */
    inner class PlayerCache(
        val index: Int,
        val player: Player,
        var occupation: Occupation? = null, // ?????????????????????
        val loc: Location = player.location, // ???????????????
        val mode: GameMode = player.gameMode, // ???????????????
        var respawn: Int = 0, // ??????????????????
        var killed: Int = 0, // ???????????????
        var death: Int = 0, // ????????????
    )

    enum class State {
        InGame, // ?????????
        Idle, // ?????????
        Countdown, // ??????????????????
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
         * ???????????????????????????
         * */
        @SubscribeEvent
        fun e(e: PlayerQuitEvent) {
            val game = e.player.inGame() ?: return
            game.forceQuit(e.player)
        }

        /**
         * ??????????????????????????????
         * */
        @SubscribeEvent
        fun e(e: EntityDeathEvent) {
            // ????????????????????????????????????
            if (e.entity is Player || e.entity !in entities || e.entity.killer?.isInGame() == false) return
            val game = e.entity.killer!!.inGame() ?: return
            // ??????????????????
            e.drops.clear()
            game.cache[e.entity.killer]!!.killed += 1
            PlayerKillMobEvent(game, e.entity.killer!!, e.entity).call()
            game.removeEntity(e.entity)
        }

        /**
         * ??????????????????
         * */
        @SubscribeEvent
        fun e(e: EntityDamageEvent) {
            val player = e.entity as? Player ?: return
            val game = player.inGame() ?: return

            // ????????????????????????????????????
            if (e is EntityDamageByEntityEvent) {
                val damager = (e.damager as? Player) ?: ((e.damager as? Projectile)?.shooter as? Player)
                if (damager?.isInGame() == true) {
                    // ????????????
                    e.isCancelled = true
                    return
                }
            }

            // ??????????????????
            if (player.health - e.finalDamage <= 0.0) {
                e.isCancelled = true

                game.cache.keys.forEach {
                    // ????????????
                    if (it == player) {
                        it.playSound(it.location, Sound.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.0f)
                    } else it.analogEntityDie(player)
                }

                player.gameMode = GameMode.SPECTATOR

                // ?????????????????????
                player.setBorderWarning()
                player.sendLang("Player-Death")
                // ????????????
                val data = game.cache[e.entity]!!
                data.death += 1
                top.lanscarlos.towerdefence.event.PlayerDeathEvent(game, player).call()
                data.respawn += game.respawn
                // ????????????
                player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                player.foodLevel = 20
                submit(period = 1) {
                    if (!player.isInGame()) {
                        // ?????????????????????
                        player.resetBorderWarning()
                        cancel()
                        return@submit
                    }
                    if (--data.respawn <= 0) {
                        // ??????
                        PlayerRespawnEvent(game, player).call()
                        player.resetBorderWarning()
                        player.teleport(game.region.playersSpawn[data.index])
                        // ????????????
                        player.gameMode = if (Context.Force_Adventure_Mode) {
                            GameMode.ADVENTURE
                        } else {
                            data.mode
                        }
                        player.sendLang("Player-Respawn")
                        cancel()
                        return@submit
                    }
                    // ???????????????
                    if (data.respawn % 20 == 0) {
                        val second = data.respawn / 20
                        player.sendLang("Game-Respawn-Countdown", second)
                    }
                }
            }
        }

        /**
         * ??????????????????????????????
         * */
        @SubscribeEvent
        fun e(e: EntityDamageByEntityEvent) {
            if ((e.entity as? Player)?.isInGame() != true) return // ???????????????
            val damager = (e.damager as? Player) ?: ((e.damager as? Projectile)?.shooter as? Player) ?: return
            if (damager.isInGame()) {
                // ????????????
                e.isCancelled = true
            }
        }

        /**
         * ??????????????????????????????
         * */
        @SubscribeEvent
        fun e(e: PlayerPickupItemEvent) {
            if (e.player.isInGame()) e.isCancelled = true
        }

        /**
         * ??????????????????????????????
         * */
        @SubscribeEvent
        fun e(e: PlayerDropItemEvent) {
            if (e.player.isInGame()) e.isCancelled = true
        }

        /**
         * ??????????????????????????????
         * */
        @SubscribeEvent
        fun e(e: BlockBreakEvent) {
            if (e.player.isInGame()) e.isCancelled = true
        }

        /**
         * ??????????????????????????????
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