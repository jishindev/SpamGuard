package com.jishin.android.spamguard.core.db.entities

import android.os.SystemClock
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_contacts")
data class Contact(
    @PrimaryKey @ColumnInfo(name = "number") val number: String,
    @ColumnInfo(name = "name") val name: String? = null,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)