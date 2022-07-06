package top.lanscarlos.towerdefence.database

import taboolib.module.database.Host
import taboolib.module.database.Table

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.database
 *
 * @author Lanscarlos
 * @since 2022-07-05 20:12
 */
interface Type {
    fun host(): Host<*>
    fun table(): Table<*, *>
}