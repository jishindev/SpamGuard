package dev.jishin.android.spamguard.blocker

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.jishin.android.spamguard.core.db.dao.ContactsDao
import dev.jishin.android.spamguard.core.db.entities.Contact
import dev.jishin.android.spamguard.getMatchingContact
import dev.jishin.android.spamguard.sendGrantPermsNotification
import dev.jishin.android.spamguard.sendRejectedNotification
import dev.jishin.android.spamguard.toPhoneNumber
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.N)
class ScreeningService : CallScreeningService(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            Timber.i("CoroutineExceptionHandler called with: coroutineContext = [$coroutineContext], throwable = [$throwable]")
            // todo handle exceptions
        }

    @Inject
    lateinit var contactsDao: ContactsDao

    @Inject
    lateinit var notificationManagerCompat: NotificationManagerCompat


    /* Not called for when the number is already in the users contacts but we need it to be.
     Might need to update logic based on the resolution of this - https://issuetracker.google.com/issues/130081372 */
    override fun onScreenCall(callDetails: Call.Details) {
        Timber.i("onScreenCall() called with: callDetails = [$callDetails]")

        // Abort and send out notification if permissions are not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationManagerCompat.sendGrantPermsNotification(this)
            return
        }


        // Skip all calls except Incoming
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && callDetails.callDirection != Call.Details.DIRECTION_INCOMING
        ) return

        // parse incoming phone number from callDetails
        val incomingNumber = callDetails.handle.toPhoneNumber() ?: return

        // fetch blocked numbers from db and match with incoming number
        launch {
            contactsDao.getAll().let { blockedList ->
                Timber.i("onScreenCall() called with: uri: $incomingNumber, blocked = [${blockedList.map { it.number }}]")

                val contactToBeBlocked = blockedList.getMatchingContact(incomingNumber)
                if (contactToBeBlocked != null) {
                    rejectCall(contactToBeBlocked, callDetails)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun rejectCall(contact: Contact, callDetails: Call.Details) {
        Timber.i("rejectCall() called")
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {

                val response = CallResponse.Builder()
                response.setRejectCall(true)
                response.setDisallowCall(true)
                response.setSkipCallLog(false)
                response.setSkipNotification(true)

                // request to reject call
                respondToCall(callDetails, response.build())

                // send notification about the blocked call
                notificationManagerCompat.sendRejectedNotification(this, contact)
            }
        }
    }
}