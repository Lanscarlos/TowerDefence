package top.lanscarlos.towerdefence.internal

import org.bukkit.Bukkit
import org.bukkit.Location
import taboolib.common.io.newFile
import taboolib.common.platform.function.console
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.lang.asLangText
import top.lanscarlos.towerdefence.TowerDefence
import top.lanscarlos.towerdefence.utils.*
import java.io.File

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.internal
 *
 * @author Lanscarlos
 * @since 2022-03-09 15:45
 */
class Region(
    val id: String,
    var center: Location,
    var size: Double = 20.0,
    val playersSpawn: MutableList<Location> = mutableListOf(),
    val mobSpawn: MutableList<Location> = mutableListOf(),
) {

    var editing = false

    val file by lazy { File(folder, "$id.yml") }

    fun save() {
        val config = file.ifNotExists { newFile(it, create = true, folder = false) }.toConfig()
        config["world"] = center.world!!.name
        config["center.x"] = center.x
        config["center.y"] = center.y
        config["center.z"] = center.z
        config["center.yaw"] = center.yaw
        config["center.pitch"] = center.pitch
        config["size"] = size
        config["player-spawn"] = playersSpawn.map {
            mapOf(
                "x" to it.x,
                "y" to it.y,
                "z" to it.z,
                "yaw" to it.yaw,
                "pitch" to it.pitch,
            )
        }
        config["mob-spawn"] = mobSpawn.map {
            mapOf(
                "x" to it.x,
                "y" to it.y,
                "z" to it.z,
                "yaw" to it.yaw,
                "pitch" to it.pitch,
            )
        }
        config.saveToFile(file)
    }

    companion object {

        val folder by lazy {
            File(TowerDefence.plugin.dataFolder, "regions")
        }

        val regions = mutableMapOf<String, Region>()

        fun get(id: String): Region? {
            return regions[id]
        }

        fun create(id: String, center: Location, size: Double = 20.0): Region {
            return Region(id, center.clone(), size).also { regions[id] = it }
        }

        fun load(): String {
            return try {
                val start = timing()
                folder.ifNotExists {
                    releaseResourceFile("regions/#def.yml", true)
                }.getFiles().forEach { file ->
                    val config = file.toConfig()
                    val center = Location(
                        Bukkit.getWorld(config.getString("world") ?: "world")!!,
                        config.getDouble("center.x", 0.0),
                        config.getDouble("center.y", 0.0),
                        config.getDouble("center.z", 0.0),
                        config.getDouble("center.yaw", 0.0).toFloat(),
                        config.getDouble("center.pitch", 0.0).toFloat(),
                    )
                    val size: Double = config.getDouble("size", 0.0)
                    val playersSpawn = config.getMapList("player-spawn").map {
                        Location(
                            center.world,
                            it["x"].toDouble(0.0),
                            it["y"].toDouble(0.0),
                            it["z"].toDouble(0.0),
                            it["yaw"].toFloat(0.0f),
                            it["pitch"].toFloat(0.0f),
                        )
                    }.toMutableList()
                    val mobSpawn = config.getMapList("mob-spawn").map {
                        Location(
                            center.world,
                            it["x"].toDouble(0.0),
                            it["y"].toDouble(0.0),
                            it["z"].toDouble(0.0),
                            it["yaw"].toFloat(0.0f),
                            it["pitch"].toFloat(0.0f),
                        )
                    }.toMutableList()

                    regions[file.nameWithoutExtension] = Region(file.nameWithoutExtension, center, size, playersSpawn, mobSpawn)
                }
                console().asLangText("Regions-Load-Succeeded", regions.values.toSet().size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                console().asLangText("Regions-Load-Failed", e.localizedMessage).also {
                    console().sendMessage(it)
                }
            }
        }

    }
}