package com.kaisery.config

import com.kaisery.entity.User
import com.kaisery.repository.UserRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SetupConfiguration {

    @Bean
    open fun setupUser(userRepository: UserRepository) = ApplicationRunner {
        userRepository.save(User(0, "aa", "123456"))
        userRepository.save(User(0, "bb", "123456"))
    }
}
