package com.jishin.android.spamguard.core.di

import android.content.Context
import com.jishin.android.spamguard.core.db.BlockedContactsDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DbModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context) =
        BlockedContactsDb.getDb(appContext)

    @Provides
    @Singleton
    fun provideContactsDao(database: BlockedContactsDb) = database.contactsDao()
}