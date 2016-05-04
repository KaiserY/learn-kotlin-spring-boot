package com.kaisery

import com.kaisery.filter.JwtFilter
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.context.annotation.Bean

@SpringBootApplication
open class Application {

    @Bean
    open fun jwtFilter(): FilterRegistrationBean {
        val registrationBean = FilterRegistrationBean()
        registrationBean.setFilter(JwtFilter());
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}