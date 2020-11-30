package com.jishin.android.spamguard.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jishin.android.spamguard.core.db.dao.ContactsDao
import com.jishin.android.spamguard.core.db.entities.Contact

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class BlockedContactsDb : RoomDatabase() {

    abstract fun contactsDao(): ContactsDao

    companion object{
        fun getDb(appContext: Context) = Room.databaseBuilder(
            appContext,
            BlockedContactsDb::class.java,
            "blocked_numbers.db"
        ).build()
    }
}