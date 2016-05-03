package com.kaisery.test

import com.kaisery.Application
import com.kaisery.controller.LoginRequest
import com.kaisery.controller.LoginResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.TestRestTemplate
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(Application::class))
@WebIntegrationTest(randomPort = true)
class HelloKotlinTest {

    @Value("\${local.server.port}")
    val port: Int = 0;

    val addr: String = "http://localhost"

    val template: TestRestTemplate = TestRestTemplate()

    @Test
    fun helloWorldTest() {
        val entity = template.getForEntity("${addr}:${port}/greeting", String::class.java)

        assert(entity.statusCode.is2xxSuccessful)
    }

    @Test
    fun loginTest() {
        val loginRequest = LoginRequest("aa", "123456")

        val entity = template.postForEntity("${addr}:${port}/login", loginRequest, String::class.java)

        assert(entity.statusCode.is2xxSuccessful)
    }
}
