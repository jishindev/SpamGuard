package dev.jishin.android.spamguard.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.jishin.android.spamguard.*
import dev.jishin.android.spamguard.core.db.entities.Contact
import dev.jishin.android.spamguard.custom.ui.adapters.SimpleRvAdapter
import dev.jishin.android.spamguard.databinding.ActivityMainBinding
import dev.jishin.android.spamguard.databinding.RvBlockedNumberBinding
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainVM by viewModels<MainVM>()

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var rvAdapter: SimpleRvAdapter<Contact, ContactVH>
    private val bsAddNew by lazy { AddNewBSFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fallback for Light status on Android versions < M(23) where it is not supported
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        // Set main content
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        initViews()

        // Config permission for call screening based on android versions
        // todo fine tune permissions structure based on support on different android versions
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ANSWER_PHONE_CALLS,
                        Manifest.permission.READ_CONTACTS
                    )
                ) {}
                requestRoleAsScreener()
            }
            else -> {
                val perms = arrayListOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    perms.add(Manifest.permission.ANSWER_PHONE_CALLS)
                }
                requestPermissions(perms.toTypedArray()) {
                    listenBlockedNumbers()
                }
            }
        }
    }

    private fun initViews() {

        with(mainBinding) {
            with(rvBlockedNumbers) {
                rvAdapter = SimpleRvAdapter(
                    layoutResId = R.layout.rv_blocked_number,
                    vhBuilder = { v -> ContactVH(RvBlockedNumberBinding.bind(v)) },
                    binder = { h, item -> h.bind(item) },
                    areContentsSame = { old, new -> old.number == new.number }
                )
                layoutManager = LinearLayoutManager(this@MainActivity)
                addDivider()
                adapter = rvAdapter
            }

            fabAdd.setOnClickListener {
                bsAddNew.show(supportFragmentManager, AddNewBSFragment.TAG)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestRoleAsScreener() {

        // todo needs to be instantiated before state STARTED, can be moved to onCreate()
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    listenBlockedNumbers()
                }
                Activity.RESULT_CANCELED -> {
                    // todo show rationale and request again or disable app
                }
            }
        }

        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        launcher.launch(intent)
    }

    private fun listenBlockedNumbers() {

        mainVM.getBlockedNumbers().observe(this) {

            rvAdapter.updateItems(it)

            with(mainBinding) {
                nestedScrollView.fullScroll(View.FOCUS_UP)
                tvEmpty.isVisible = it.isEmpty()
                tvTitle.isVisible = it.isNotEmpty()
            }
        }
    }

    private fun requestPermissions(perms: Array<String>, action: () -> Unit) {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grantedArray ->
            if (grantedArray.all { it.value }) {
                action()
            } else {
                // todo show rationale and request again or disable app
            }
        }

        launcher.launch(perms)
    }

    inner class ContactVH(private val binding: RvBlockedNumberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(contact: Contact) {
            with(binding) {

                val dateTime = contact.timestamp.getDateTime()
                tvNameAndNumber.text = contact.getNumberAndName()
                tvDateTime.text = dateTime

                chipUnblock.setOnClickListener {
                    mainVM.deleteBlocked(contact)
                }
            }
        }
    }
}