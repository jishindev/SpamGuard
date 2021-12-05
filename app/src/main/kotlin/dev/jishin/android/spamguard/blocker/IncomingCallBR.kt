package dev.jishin.android.spamguard.blocker

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.core.app.NotificationManagerCompat
import dev.jishin.android.spamguard.core.db.dao.ContactsDao
import dev.jishin.android.spamguard.core.db.entities.Contact
import dev.jishin.android.spamguard.sendGrantPermsNotification
import dev.jishin.android.spamguard.sendRejectedNotification
import dagger.hilt.android.AndroidEntryPoint
import dev.jishin.android.spamguard.getMatchingContact
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.reflect.Method
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class IncomingCallBR : HiltBroadcastReceiver(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            Timber.i("CoroutineExceptionHandler called with: coroutineContext = [$coroutineContext], throwable = [$throwable]")
        }

    @Inject
    lateinit var contactsDao: ContactsDao

    @Inject
    lateinit var notificationManagerCompat: NotificationManagerCompat

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Timber.i("onReceive() called with: context = [$context], intent = [$intent]")

        // Abort and send out notification if permissions are not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        ) {
            notificationManagerCompat.sendGrantPermsNotification(context)
            return
        }

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED &&
            intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                .equals(TelephonyManager.EXTRA_STATE_RINGING)
        ) {
            // parse incoming phone number, skip and return if number is null
            val incomingNumber =
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: return

            // fetch blocked numbers from db and match with incoming number
            launch {
                contactsDao.getAll().let { blockedList ->
                    Timber.i("onReceive called with: uri: $incomingNumber, blocked = [${blockedList.map { it.number }}]")
                    val contactToBeBlocked = blockedList.getMatchingContact(incomingNumber)
                    if (contactToBeBlocked != null) {
                        rejectCall(context, contactToBeBlocked)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "PrivateApi")
    private fun rejectCall(context: Context, contact: Contact) {
        Timber.i("rejectCall() called")
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        when {

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                try {
                    telecomManager.endCall()
                    Timber.d("endCall() invoked on TelecomManager")

                    notificationManagerCompat.sendRejectedNotification(context, contact)
                } catch (e: Exception) {
                    Timber.e(e, "Error ending call with telecomManager")
                }
            }

            else -> {

                // Using reflection for API <= 27
                try {
                    val telephonyClass = Class.forName("com.android.internal.telephony.ITelephony")
                    val telephonyStubClass = telephonyClass.classes[0]
                    val serviceManagerClass = Class.forName("android.os.ServiceManager")
                    val serviceManagerNativeClass = Class.forName("android.os.ServiceManagerNative")
                    val getService: Method =
                        serviceManagerClass.getMethod("getService", String::class.java)
                    val tempInterfaceMethod: Method = serviceManagerNativeClass.getMethod(
                        "asInterface",
                        IBinder::class.java
                    )
                    val tmpBinder = Binder()
                    tmpBinder.attachInterface(null, "fake")
                    val serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder)!!
                    val serviceMethod: Method =
                        telephonyStubClass.getMethod("asInterface", IBinder::class.java)
                    val telephonyObject = serviceMethod.invoke(
                        null,
                        getService.invoke(serviceManagerObject, "phone")
                    )!!
                    val telephonyEndCall = telephonyClass.getMethod("endCall")
                    telephonyEndCall.invoke(telephonyObject)

                    notificationManagerCompat.sendRejectedNotification(context, contact)
                } catch (e: Exception) {
                    Timber.e(e, "Error ending call with ITelecomService")
                }
            }
        }
    }
}

abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
}