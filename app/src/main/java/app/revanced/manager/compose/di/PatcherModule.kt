package app.revanced.manager.compose.di

import app.revanced.manager.compose.patcher.PatcherState
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val patcherModule = module {
    singleOf(::PatcherState)
}