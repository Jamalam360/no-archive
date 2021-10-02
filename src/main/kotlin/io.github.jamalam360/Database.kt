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

    fun addThread(id: Snowflake) {
        threads.insertOne(NoArchiveThread(id.value))
    }

    fun removeThread(id: Snowflake) {
        threads.deleteOne("{\"id\" : ${id.value}}")
    }

    fun hasThread(id: Snowflake): Boolean {
        return threads.findOne("{\"id\" : ${id.value}}") != null
    }
}

data class NoArchiveThread(val id: Long)