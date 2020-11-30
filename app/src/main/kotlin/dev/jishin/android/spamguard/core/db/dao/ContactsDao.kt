package dev.jishin.android.spamguard.core.db.dao

import androidx.room.*
import dev.jishin.android.spamguard.core.db.entities.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM blocked_contacts ORDER BY timestamp DESC")
    fun getAllAsFlow(): Flow<List<Contact>>

    @Query("SELECT * FROM blocked_contacts ORDER BY timestamp DESC")
    suspend fun getAll(): List<Contact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun block(vararg contact: Contact):List<Long>

    @Delete
    suspend fun deleteBlocked(vararg contact: Contact)
}