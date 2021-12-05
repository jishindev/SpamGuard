package dev.jishin.android.spamguard.core.db.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "blocked_contacts")
data class Contact(
    @PrimaryKey @ColumnInfo(name = "number") val number: String,
    @ColumnInfo(name = "name") val name: String? = null,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
) {
    fun getNumberAndName() = "${if (!name.isNullOrEmpty()) "${getShortName()}, " else ""}$number"

    private fun getShortName() = name?.split(" ")?.run {
        subList(0, if (size > 2) 2 else size)
    }?.joinToString(" ")
}
