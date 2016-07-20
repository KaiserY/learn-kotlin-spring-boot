package com.kaisery.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kaisery.common.cache.Caching
import com.kaisery.common.token.Token
import com.kaisery.controller.LoginRequest
import com.kaisery.controller.LoginResponse
import com.kaisery.controller.User
import com.kaisery.repository.UserRepository
import io.jsonwebtoken.Claims
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HelloKotlinTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var template: TestRestTemplate

    val mapper = jacksonObjectMapper()

    @Test
    fun helloWorldTest() {
        val entity = template.getForEntity("/greeting", String::class.java)

        assert(entity.statusCode.is2xxSuccessful)
    }

    @Test
    fun loginTest() {
        val loginRequest = LoginRequest("aa", "123456")

        val entity = template.postForEntity("/login", loginRequest, String::class.java)

        assert(entity.statusCode.is2xxSuccessful)
        assert(mapper.readValue<LoginResponse>(entity.body).token == "Token is in the Cookie :)")

        val cookie = entity.headers.getFirst("Set-Cookie")

        val token = getTokenFromCookie(cookie)

        val claims: Claims = Token.parser.setSigningKey("secretkey").parseClaimsJws(token).body

        assert((claims["roles"] as List<*>).contains("user"))
    }

    @Test
    fun apiTest() {
        val loginRequest = LoginRequest("admin", "123456")

        var entity = template.postForEntity("/login", loginRequest, String::class.java)

        assert(entity.statusCode.is2xxSuccessful)

        val cookie = entity.headers.getFirst("Set-Cookie")

        val token = getTokenFromCookie(cookie)

        val headers = HttpHeaders()
        headers.add("Cookie", "token=$token")

        entity = template.exchange("/api/role/admin", HttpMethod.GET, HttpEntity<HttpHeaders>(headers), String::class.java)

        assert(entity.body.toBoolean())
    }

    @Test
    fun cacheTest() {
        val user = User(0, "test", "123456", arrayOf("user"))
        Caching.userCache.put(user.hashCode(), user)

        val cachedUser = Caching.userCache.get(user.hashCode())

        assert(user.id == cachedUser.id)
    }

    @Test
    fun userDBTest() {
        val user: com.kaisery.entity.User = userRepository.findByName("aa")[0]

        assert(user.password == "123456")
    }

    fun getTokenFromCookie(cookie: String): String {
        val items = cookie.split(";")

        return items.filter { it.startsWith("token=") }.map { it.substring(6) }[0]
    }
}
