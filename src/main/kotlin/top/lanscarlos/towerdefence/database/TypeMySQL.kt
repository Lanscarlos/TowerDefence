package top.lanscarlos.towerdefence.database

import taboolib.module.database.*

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.database
 *
 * @author Lanscarlos
 * @since 2022-07-05 20:12
 */
class TypeMySQL(private val host: HostSQL, prefix: String) : Type {

    private val table = Table("${prefix}_data", host) {
        add { id() }
        add("user") {
            type(ColumnTypeSQL.VARCHAR, 36) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("occupation") {
            type(ColumnTypeSQL.VARCHAR, 36)
        }
        add("level") {
            type(ColumnTypeSQL.INT, 16)
        }
        add("exp") {
            type(ColumnTypeSQL.DOUBLE, 16, 12)
        }

    }

    override fun host(): Host<*> = host
    override fun table(): Table<*, *> = table
}