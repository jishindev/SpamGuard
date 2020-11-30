package com.jishin.android.spamguard

import android.telephony.PhoneNumberUtils
import android.util.Patterns
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

fun EditText.hasValidNumber(pattern:String = /*Patterns.PHONE.pattern()*/"^[+]?[0-9]{10,13}\$") = Regex(pattern).matches(text.toString())
   /* PhoneNumberUtils.isGlobalPhoneNumber(text.toString())*/

fun RecyclerView.addDivider() {
    addItemDecoration(
        DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        ).apply {
            ContextCompat.getDrawable(context, R.drawable.divider_grey)?.let {
                setDrawable(it)
            }
        }
    )
}
fun Long.getDateTime() = runCatching {
    SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
        .format(
            Date(this)
        ).toLowerCase(Locale.getDefault())
}.getOrNull()

object Constants {
    const val DATE_TIME_FORMAT = "d MMM yyyy 'at' hh:mm a"
}