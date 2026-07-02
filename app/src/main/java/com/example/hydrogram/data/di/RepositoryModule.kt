package com.example.hydrogram.data.di

import com.example.hydrogram.domain.repository.AuthRepository
import com.example.hydrogram.domain.repository.ChatRepository
import com.example.hydrogram.domain.repository.InboxRepository
import com.example.hydrogram.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepository: ChatRepository,
    ) : ChatRepository

    @Binds
    @Singleton
    abstract fun bindInboxRepository(
        inboxRepository: InboxRepository,
    ) : InboxRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepository: UserRepository,
    ) : UserRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: AuthRepository,
    ) : AuthRepository

}