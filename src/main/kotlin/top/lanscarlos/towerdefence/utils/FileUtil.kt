package top.lanscarlos.towerdefence.utils

import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import java.io.File

inline fun File.ifExists(func: ((file: File) -> Unit)): File {
    if (exists()) func(this)
    return this
}

inline fun File.ifNotExists(func: ((file: File) -> Unit)): File {
    if (!exists()) func(this)
    return this
}

/**
 * 过滤有效文件
 * */
fun File.getFiles(file : File = this, filter : String = "#", suffix: String = "yml") : List<File> {
    if (!file.exists()) return listOf()
    return mutableListOf<File>().apply {
        if(file.isDirectory) {
            file.listFiles()?.forEach {
                addAll(getFiles(it))
            }
        } else if (!file.name.startsWith(filter) && file.extension == suffix) {
            add(file)
        }
    }
}

fun File.toConfig(): ConfigFile {
    return Configuration.loadFromFile(this)
}

/**
 * 加载 Config 下所有 Section 内容
 * */
inline fun Configuration.forEachSections(func: ((key: String, section: ConfigurationSection) -> Unit)) {
    getKeys(false).forEach { key ->
        getConfigurationSection(key)?.let { section ->
            func(key, section)
        }
    }
}

/**
 * 加载该目录下所有子文件的 Section 内容
 * */
inline fun File.deepSections(crossinline func: ((file: File, key: String, section: ConfigurationSection) -> Unit)) {
    this.getFiles().forEach { it.toConfig().forEachSections { key, section -> func(it, key, section) } }
}

inline fun File.addWatcher(runFirst: Boolean = false, crossinline func: (File.() -> Unit)): File {
    if (FileWatcher.INSTANCE.hasListener(this)) return this
    FileWatcher.INSTANCE.addSimpleListener(this, { func(this) }, runFirst)
    return this
}