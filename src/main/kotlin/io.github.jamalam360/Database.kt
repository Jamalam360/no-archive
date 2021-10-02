package io.github.jamalam360

import dev.kord.common.entity.Snowflake
import org.litote.kmongo.*

/**
 * @author  Jamalam360
 */
class Database {
    private val client = KMongo.createClient("mongodb://localhost:27017")
    private val db = client.getDatabase("NoArchive")
    private val threads = db.getCollection<NoArchiveThread>()
    private val serverConfigs = db.getCollection<ServerConfig>()

    fun addThread(id: Snowflake) {
        threads.insertOne(NoArchiveThread(id.value))
    }

    fun removeThread(id: Snowflake) {
        threads.deleteOne(idFilter(id))
    }

    fun hasThread(id: Snowflake): Boolean {
        return threads.findOne(idFilter(id)) != null
    }

    fun getServerConfig(id: Snowflake): ServerConfig? {
        return serverConfigs.findOne(idFilter(id))
    }

    fun createServerConfig(id: Snowflake) {
        serverConfigs.insertOne(ServerConfig(id.value, false))
    }

    fun updateServerConfig(id: Snowflake, autoNoArchive: Boolean) {
        serverConfigs.replaceOne(idFilter(id), ServerConfig(id.value, autoNoArchive))
    }

    fun hasServerConfig(id: Snowflake): Boolean {
        return serverConfigs.findOne(idFilter(id)) != null
    }

    private fun idFilter(id: Snowflake): String {
        return "{\"id\" : ${id.value}}"
    }
}

data class NoArchiveThread(val id: Long)

data class ServerConfig(val id: Long, val autoPrevent: Boolean)