package dev.jishin.android.spamguard.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.jishin.android.spamguard.R
import dev.jishin.android.spamguard.core.db.entities.Contact
import dev.jishin.android.spamguard.databinding.BsAddBlockedNumberBinding
import dev.jishin.android.spamguard.hasValidNumber
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class AddNewBSFragment : BottomSheetDialogFragment() {

    private val mainVM by activityViewModels<MainVM>()
    private var _binding: BsAddBlockedNumberBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BsAddBlockedNumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        parsePickedContact(it)?.let { contact ->
                            mainVM.block(contact)
                            dismissAllowingStateLoss()
                        }
                    }
                    Activity.RESULT_CANCELED -> {

                    }
                }
            }
            btnPickFromContacts.setOnClickListener {
                val contactPickerIntent = Intent(
                    Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                )

                launcher.launch(contactPickerIntent)
            }

            // shrunk by default
            fabConfirm.shrink()
            etNumber.addTextChangedListener {
                // clear error when text is modified
                tilNumber.error = null
                // animate and extend when valid number
                if (etNumber.hasValidNumber()) fabConfirm.extend() else fabConfirm.shrink()
            }

            fabConfirm.setOnClickListener {
                // block if it is a valid number, show error otherwise
                if (etNumber.hasValidNumber()) {
                    mainVM.block(Contact(etNumber.text.toString()))
                    etNumber.text = null
                    dismissAllowingStateLoss()
                } else
                    tilNumber.error = getString(R.string.not_valid_number)
            }
        }
    }

    private fun parsePickedContact(activityResult: ActivityResult): Contact? {

        try {
            val uri: Uri = activityResult.data?.data!!
            context?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                val number =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                Timber.d("Name and Contact number is $name,$number")
                if (name != null && number != null) {
                    return Contact(number, name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BSAddNewFragment"
    }
}