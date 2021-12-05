package dev.jishin.android.spamguard.main

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jishin.android.spamguard.core.db.dao.ContactsDao
import dev.jishin.android.spamguard.core.db.entities.Contact
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject internal constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactsDao: ContactsDao
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