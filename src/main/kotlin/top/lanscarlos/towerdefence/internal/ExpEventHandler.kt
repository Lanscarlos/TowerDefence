package top.lanscarlos.towerdefence.internal

import org.bukkit.event.player.PlayerExpChangeEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import top.lanscarlos.towerdefence.database.Database
import top.lanscarlos.towerdefence.event.PlayerUpgradeEvent
import top.lanscarlos.towerdefence.internal.Game.Companion.inGame

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.internal
 *
 * @author Lanscarlos
 * @since 2022-07-06 10:42
 */
object ExpEventHandler {
    @SubscribeEvent
    fun e(e: PlayerExpChangeEvent) {
        info("PlayerExpChangeEvent x1")
        val efficiency = Context.Exp_Conversion_Efficiency
        val game = e.player.inGame() ?: return
        info("PlayerExpChangeEvent x2")
        val occupation = game.cache[e.player]?.occupation ?: return
        info("PlayerExpChangeEvent x3")
        val data = Database.getData(e.player, occupation) ?: return
        info("PlayerExpChangeEvent x4")
        var exp = data.second + e.amount * efficiency
        var maxExp = occupation.maxExp * (occupation.maxExpRatio * (data.first - 1))
        var level = data.first

        // 是否达到升级条件
        if (exp >= maxExp) {

            exp -= maxExp
            maxExp *= occupation.maxExpRatio

            level += 1
            // 触发事件
            if (!PlayerUpgradeEvent(game, e.player, occupation, level).call()) return

            // 保存等级数据
            Database.updateLevel(e.player, occupation, level)
        }

        // 保存经验数据
        Database.updateExp(e.player, occupation, exp)

        // 更新玩家经验条
        info("exp -> $exp")
        info("maxExp -> $maxExp")
        info("(exp / maxExp) -> ${(exp / maxExp)}")
        e.player.level = level
        e.player.exp = (exp / maxExp).toFloat()

        e.amount = 0
    }
}