package com.jishin.android.spamguard.blocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import timber.log.Timber

class IncomingCallBR : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("onReceive() called with: context = [$context], intent = [$intent]")

        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED &&
            intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                .equals(TelephonyManager.EXTRA_STATE_RINGING)
        ) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Timber.d("onReceive: incomingNumber: $incomingNumber")
        }
    }
}