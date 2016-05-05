package com.kaisery.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kaisery.Application
import com.kaisery.cache.Caching
import com.kaisery.controller.LoginRequest
import com.kaisery.controller.LoginResponse
import com.kaisery.controller.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.TestRestTemplate
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(Application::class))
@WebIntegrationTest(randomPort = true)
class HelloKotlinTest {

    @Value("\${local.server.port}")
    val port: Int = 0;

    val addr = "http://localhost"

    val template = TestRestTemplate()

    val mapper = jacksonObjectMapper()

    @Test
    fun helloWorldTest() {
        val entity = template.getForEntity("$addr:$port/greeting", String::class.java)

        assert(entity.statusCode.is2xxSuccessful)
    }

    @Test
    fun loginTest() {
        val loginRequest = LoginRequest("aa", "123456")

        val entity = template.postForEntity("$addr:$port/login", loginRequest, String::class.java)

        assert(entity.statusCode.is2xxSuccessful)
        assert(mapper.readValue<LoginResponse>(entity.body).token == "Token is in the Cookie :)")

        val cookie = entity.headers.getFirst("Set-Cookie")

        val token = getTokenFromCookie(cookie)

        val claims: Claims = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token).body

        assert((claims["roles"] as List<*>).contains("user"))
    }

    @Test
    fun apiTest() {
        val loginRequest = LoginRequest("admin", "123456")

        var entity = template.postForEntity("$addr:$port/login", loginRequest, String::class.java)

        assert(entity.statusCode.is2xxSuccessful)

        val cookie = entity.headers.getFirst("Set-Cookie")

        val token = getTokenFromCookie(cookie)

        val headers = HttpHeaders()
        headers.add("Cookie", "token=$token")

        entity = template.exchange("$addr:$port/api/role/admin", HttpMethod.GET, HttpEntity<HttpHeaders>(headers), String::class.java)

        assert(entity.body.toBoolean())
    }

    @Test
    fun cacheTest() {
        val user = User(0, "test", "123456", arrayOf("user"))
        Caching.userCache.put(user.hashCode(), user)

        val cachedUser = Caching.userCache.get(user.hashCode())

        assert(user.id == cachedUser.id)
    }

    fun getTokenFromCookie(cookie: String): String {
        val items = cookie.split(";")

        return items.filter { it.startsWith("token=") }.map { it.substring(6) }[0]
    }
}
