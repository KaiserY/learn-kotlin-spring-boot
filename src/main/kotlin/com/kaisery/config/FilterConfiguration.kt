package com.kaisery.config

import com.kaisery.filter.JwtFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FilterConfiguration {

    @Bean
    open fun jwtFilter(): FilterRegistrationBean {
        val registrationBean = FilterRegistrationBean()
        registrationBean.filter = JwtFilter()
        registrationBean.addUrlPatterns("/api/*")

        return registrationBean
    }
}
