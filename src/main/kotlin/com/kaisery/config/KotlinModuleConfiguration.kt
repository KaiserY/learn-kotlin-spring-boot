package com.kaisery.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KotlinModuleConfiguration {

    @Bean
    open fun kotlinModule(): KotlinModule {
        return KotlinModule()
    }
}
