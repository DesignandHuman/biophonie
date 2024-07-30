package fr.labomg.biophonie.core.network.di

import javax.inject.Qualifier

@Qualifier annotation class LocalDataSource

@Qualifier annotation class RemoteDataSource

@Qualifier annotation class Authenticated

@Qualifier annotation class Unauthenticated
