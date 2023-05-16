package app.revanced.manager.compose.di

import app.revanced.manager.compose.patcher.ReVancedWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    worker { ReVancedWorker(androidContext(), get()) }
}