package dev.jishin.android.spamguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import dev.jishin.android.spamguard.core.db.entities.Contact
import dev.jishin.android.spamguard.main.MainActivity
import timber.log.Timber
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*


fun EditText.hasValidNumber(pattern: String = "^[+]?[0-9]{10,13}\$") =
    Regex(
        pattern
    ).matches(text.toString())

fun RecyclerView.addDivider(orientation: Int = DividerItemDecoration.VERTICAL) {
    addItemDecoration(
        DividerItemDecoration(
            context,
            orientation
        )/*.apply {
            ContextCompat.getDrawable(context, R.drawable.divider_grey)?.let {
                setDrawable(it)
            }
        }*/
    )
}

fun Long.getDateTime() = runCatching {
    SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
        .format(
            Date(this)
        ).lowercase()
}.getOrNull()


fun Uri.toPhoneNumber() = runCatching {
    URLDecoder.decode(
        toString().replace("tel:", ""), "UTF-8"
    )
}.getOrNull()

fun List<Contact>.getMatchingContact(incomingNumber: String) = firstOrNull {

    val incoming = incomingNumber.replace(" ", "")
    val blocked = it.number.replace(" ", "")

    // todo better number matching, may be by using libphonenumber
    incoming.contains(blocked) || blocked.contains(incoming)
}


fun NotificationManagerCompat.sendRejectedNotification(context: Context, contact: Contact) {
    Timber.i("sendRejectedNotification() called with: context = [$context], contact = [$contact]")

    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(
            "default", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Default"
        createNotificationChannel(channel)
    }

    val notify: Notification = NotificationCompat.Builder(context, "default")
        .setSmallIcon(R.drawable.ic_baseline_phone_disabled_12_red)
        .setContentTitle("Call blocked")
        .setContentText(contact.name ?: contact.number)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setShowWhen(true)
        .setAutoCancel(true)
        .setColor(getColor(context, R.color.lt_red))
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .addPerson(Person.Builder().setUri("tel:${contact.number}").build())
        .setGroup("rejected")
        .setChannelId("default")
        .setGroupSummary(true)
        .build()

    val tag = contact.number
    notify(tag, 1, notify)
}

fun NotificationManagerCompat.sendGrantPermsNotification(context: Context) {
    Timber.i("sendGrantPermsNotification() called with: context = [$context]")

    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(
            "default", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Default"
        createNotificationChannel(channel)
    }

    val notify: Notification = NotificationCompat.Builder(context, "default")
        .setSmallIcon(R.drawable.ic_baseline_phone_disabled_12_red)
        .setContentTitle("Call blocking is disabled")
        .setContentText("Click here to reauthorize SpamGuard with necessary permissions")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setShowWhen(true)
        .setAutoCancel(true)
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .setChannelId("default")
        .setGroupSummary(true)
        .build()

    notify("perms", 1, notify)
}


object Constants {
    const val DATE_TIME_FORMAT = "d MMM yyyy 'at' hh:mm a"
}