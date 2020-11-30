package com.jishin.android.spamguard.main

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
import com.jishin.android.spamguard.*
import com.jishin.android.spamguard.core.db.entities.Contact
import com.jishin.android.spamguard.custom.ui.adapters.SimpleRvAdapter
import com.jishin.android.spamguard.databinding.ActivityMainBinding
import com.jishin.android.spamguard.databinding.RvBlockedNumberBinding
import dagger.hilt.android.AndroidEntryPoint
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

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                requestPermissions(arrayOf(Manifest.permission.ANSWER_PHONE_CALLS)){}
                requestRoleAsScreener()
            }
            else -> {
                val perms = arrayListOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG
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

        //lifecycle.addObserver(mainVM)

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
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    listenBlockedNumbers()
                }
                Activity.RESULT_CANCELED -> {

                }
            }
        }

        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        launcher.launch(intent)
    }

    private fun listenBlockedNumbers() {

        mainVM.getBlockedNumbers().observe(this, {

            rvAdapter.updateItems(it)

            with(mainBinding) {
                nestedScrollView.fullScroll(View.FOCUS_UP)
                tvEmpty.isVisible = it.isEmpty()
                tvTitle.isVisible = it.isNotEmpty()
            }
        })
    }

    private fun requestPermissions(perms: Array<out String>, action: () -> Unit) {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grantedArray ->
            if (grantedArray.all { it.value }) {
                action()
            } else {
                // request again or disable app
            }
        }

        launcher.launch(perms)
    }

    inner class ContactVH(private val binding: RvBlockedNumberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(contact: Contact) {
            with(binding) {
                val name = contact.name?.split(" ")?.run {
                    subList(0, if (size > 2) 2 else size)
                }?.joinToString(" ")
                val number = contact.number
                val dateTime = contact.timestamp.getDateTime()

                tvNameAndNumber.text =
                    "${if (!name.isNullOrEmpty()) "$name, " else ""}$number"
                tvDateTime.text = dateTime

                chipUnblock.setOnClickListener {
                    mainVM.deleteBlocked(contact)
                }
            }
        }
    }

}