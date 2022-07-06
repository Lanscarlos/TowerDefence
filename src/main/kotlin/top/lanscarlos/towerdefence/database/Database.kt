package top.lanscarlos.towerdefence.database

import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.module.configuration.Configuration
import taboolib.module.database.getHost
import top.lanscarlos.towerdefence.TowerDefence
import top.lanscarlos.towerdefence.internal.Occupation
import java.io.File
import javax.sql.DataSource

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.database
 *
 * @author Lanscarlos
 * @since 2022-07-05 20:16
 */
object Database {
    private lateinit var type: Type
    private lateinit var dataSource: DataSource

    /**
     * 获取玩家某职业的等级信息
     * @return <level, exp>，若找不到则返回 null
     * */
    fun getData(player: Player, occupation: Occupation): Pair<Int, Double>? {
        return type.table().select(dataSource) {
            rows("level", "exp")
            where(("user" eq player.uniqueId.toString()) and ("occupation" eq occupation.id))
        }.firstOrNull { getInt("level") to getDouble("exp") }
    }

    fun getOccupations(player: Player): List<Occupation> {
        return type.table().select(dataSource) {
            rows("occupation")
            where("user" eq player.uniqueId.toString())
        }.map { getString("occupation") }.mapNotNull { Occupation.get(it) }
    }

    fun updateExp(player: Player, occupation: Occupation, exp: Double) {
        type.table().update(dataSource) {
            set("exp", exp)
            where(("user" eq player.uniqueId.toString()) and ("occupation" eq occupation.id))
        }
    }

    fun updateLevel(player: Player, occupation: Occupation, level: Int) {
        type.table().update(dataSource) {
            set("level", level)
            where(("user" eq player.uniqueId.toString()) and ("occupation" eq occupation.id))
        }
    }

    fun insertOccupation(player: Player, occupation: Occupation) {
        type.table().insert(dataSource, "user", "occupation", "level", "exp") {
            value(player.uniqueId.toString(), occupation.id, 1, 0.0)
        }
    }

    fun deleteOccupation(player: Player, occupation: Occupation) {
        type.table().delete(dataSource) {
            where(("user" eq player.uniqueId.toString()) and ("occupation" eq occupation.id))
        }
    }

    fun setup(config: Configuration) {
        type = if (config.getBoolean("database-setting.enable"))
            TypeMySQL(config.getHost("database-setting"), config.getString("database-setting.table-prefix") ?: "tower_defence")
        else
            TypeSQLite(File(TowerDefence.plugin.dataFolder, "data.db"), config.getString("database-setting.table-prefix") ?: "tower_defence")
        dataSource = type.host().createDataSource()
        type.table().createTable(dataSource)
        info("数据库启动完毕...")
    }
}