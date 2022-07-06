package top.lanscarlos.towerdefence.database

import taboolib.common.io.newFile
import taboolib.module.database.*
import java.io.File

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.database
 *
 * @author Lanscarlos
 * @since 2022-07-05 20:21
 */
class TypeSQLite(file: File, prefix: String) : Type {
    private val host = newFile(file).getHost()
    private val table = Table("${prefix}_data", host) {
        add { id() }
        add("user") {
            type(ColumnTypeSQLite.TEXT, 36) {
                options(ColumnOptionSQLite.PRIMARY_KEY)
            }
        }
        add("occupation") {
            type(ColumnTypeSQLite.TEXT, 36)
        }
        add("level") {
            type(ColumnTypeSQLite.INTEGER, 16)
        }
        add("exp") {
            type(ColumnTypeSQLite.REAL, 16, 12)
        }
    }

    override fun host(): Host<*> = host
    override fun table(): Table<*, *> = table
}