package dev.jishin.android.spamguard.main

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import dev.jishin.android.spamguard.core.db.dao.ContactsDao
import dev.jishin.android.spamguard.core.db.entities.Contact
import kotlinx.coroutines.launch

class MainVM @ViewModelInject internal constructor(
    private val contactsDao: ContactsDao,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel(), LifecycleObserver {

    fun getBlockedNumbers() =
        contactsDao.getAllAsFlow().asLiveData()

    fun block(contact: Contact) = viewModelScope.launch {
        contactsDao.block(contact)
    }

    fun deleteBlocked(contact: Contact) = viewModelScope.launch {
        contactsDao.deleteBlocked(contact)
    }
}