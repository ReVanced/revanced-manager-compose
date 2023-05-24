package app.revanced.manager.compose.di

import app.revanced.manager.compose.domain.repository.ReVancedRepository
import app.revanced.manager.compose.network.api.ManagerAPI
import app.revanced.manager.compose.domain.repository.BundleRepository
import app.revanced.manager.compose.domain.repository.SourceConfigRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::ReVancedRepository)
    singleOf(::ManagerAPI)
    singleOf(::BundleRepository)
    singleOf(::SourceConfigRepository)
}