package top.lanscarlos.towerdefence.internal

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.warning
import taboolib.common.util.VariableReader
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XMaterial
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import top.lanscarlos.towerdefence.TowerDefence
import top.lanscarlos.towerdefence.database.Database
import top.lanscarlos.towerdefence.utils.*
import java.io.File

/**
 * TowerDefence
 * top.lanscarlos.towerdefence.internal
 *
 * @author Lanscarlos
 * @since 2022-07-05 14:44
 */
class Occupation(
    val id: String,
    config: ConfigurationSection
) {
    val display = config.getString("display") ?: "UNKNOWN-DISPLAY"
    val maxExp = config.getInt("max-exp", 100)
    val maxExpRatio = config.getDouble("max-exp-ratio", 1.5)
    val itemMaterial = config.getString("item.mat") ?: "STONE"
    val itemName = config.getString("item.name")
    val itemLore = config.getList("item.lore")?.mapNotNull { it?.toString() }
    val itemAmount = config.getInt("item.amount", 1)
    // 加载属性词条
    val attributes =
        config.getConfigurationSection("attributes")?.let { section ->
            section.getKeys(false).associateWith { key ->
                section.getConfigurationSection(key)?.let {
                    OccupationAttribute(it)
                }
            }
        } ?: mapOf()

    /**
     * 根据等级计算属性值
     * */
    fun calcAttribute(attribute: OccupationAttribute, level: Int): Int {
        return attribute.def + attribute.levelup * (level - 1)
    }

    fun buildItem(player: Player): ItemStack? {
        val material = XMaterial.matchXMaterial(itemMaterial)
        return taboolib.platform.util.buildItem(
            if (material.isPresent) material.get() else XMaterial.STONE
        ) {
            val data = Database.getData(player, this@Occupation) ?: let {
                if (this@Occupation.id == Context.Default_Occupation) {
                    Database.insertOccupation(player, this@Occupation)
                    return@let 1 to 0.0
                } else return null
            }

            // 调整玩家经验值
            val maxExp = maxExp * (maxExpRatio * (data.first - 1))
            player.level = data.first
            player.exp = (data.second / maxExp).toFloat()

            name = itemName
            amount = itemAmount
            val reader = VariableReader("{{", "}}")
            val sb = StringBuilder()
            itemLore?.forEach { line ->
                reader.readToFlatten(line).forEach {
                    if (it.isVariable) {
                        sb.append(when (it.text) {
                            "display" -> display
                            "level" -> data.first
                            "exp" -> data.second.toInt()
                            "max_exp" -> maxExp
                            in attributes -> attributes[it.text]?.let { it1 -> calcAttribute(it1, data.first) }
                            else -> it.text
                        })
                    } else sb.append(it.text)
                }
                lore += sb.toString()
                sb.clear()
            }
        }
    }

    companion object {

        val folder by lazy {
            File(TowerDefence.plugin.dataFolder, "occupations")
        }

        val cache = mutableMapOf<String, Occupation>()

        fun get(id: String): Occupation? = cache[id]

        fun load(): String {
            return try {
                val start = timing()
                folder.ifNotExists {
                    releaseResourceFile("occupations/def.yml", true)
                }.deepSections { _, key, section ->
                    cache[key] = Occupation(key, section)
                }
                info(cache)
                console().asLangText("Occupations-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                console().asLangText("Occupations-Load-Failed", e.localizedMessage).also {
                    console().sendMessage(it)
                }
            }
        }

    }

    class OccupationAttribute(config: ConfigurationSection) {
        val def: Int = config.getInt("def", 1)
        val levelup: Int = config.getInt("levelup", 1)
    }

}