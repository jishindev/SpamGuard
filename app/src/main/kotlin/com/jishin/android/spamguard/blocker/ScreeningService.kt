package com.jishin.android.spamguard.blocker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.android.internal.telephony.ITelephony
import com.jishin.android.spamguard.core.db.dao.ContactsDao
import dagger.hilt.android.AndroidEntryPoint
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
        }


    @Inject
    lateinit var contactsDao: ContactsDao

    override fun onScreenCall(callDetails: Call.Details) {
        Timber.i("onScreenCall() called with: callDetails = [$callDetails]")

        val uri = callDetails.handle.toString()

        launch {
            contactsDao.getAllBlocked().collect { blocked ->
                Timber.i("onScreenCall() called with: uri: $uri, blocked = [${blocked.map { it.number }}]")
                if (blocked.any { uri.contains(it.number) }) {
                    rejectCall(callDetails)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun rejectCall(callDetails: Call.Details) {
        Timber.i("rejectCall() called")
        val failed = false
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                val response = CallResponse.Builder()
                response.setRejectCall(true)
                response.setDisallowCall(true)
                response.setSkipCallLog(false)
                response.setSkipNotification(true)
                respondToCall(callDetails, response.build())
            }
            /*Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {

                try {
                    telecomManager.endCall()
                    Timber.d("Invoked 'endCall' on TelecomManager");
                } catch (e: Exception) {
                    Timber.e(e, "Couldn't end call with TelecomManager")
                }
            }*/
            else -> {

                // Using reflection for API<28
                try {
                    val m = telecomManager.javaClass.getDeclaredMethod("getITelephony");
                    m.isAccessible = true;

                    val telephony = m.invoke(telecomManager) as ITelephony

                    telephony.endCall();
                } catch (e: Exception) {
                    Timber.e(e, "Couldn't end call with TelephonyManager")
                }
            }
        }
    }
}