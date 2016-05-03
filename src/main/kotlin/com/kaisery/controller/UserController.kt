package com.kaisery.controller

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.ServletException

data class User(val id: Int, val name: String, var password: String, var roles: Array<String> = arrayOf("user"))

data class LoginRequest(val name: String, val password: String)

data class LoginResponse(val token: String)

@RestController
class UserController {
    companion object {
        val userDB: Map<String, User> = hashMapOf(
                Pair("aa", User(1, "aa", "123456")),
                Pair("admin", User(0, "admin", "123456", arrayOf("user", "admin")))
        )
    }

    @RequestMapping(value = "login", method = arrayOf(RequestMethod.POST))
    fun login(@RequestBody request: LoginRequest?): LoginResponse {

        val userName: String = if (userDB.containsKey(request?.name)) {
            request?.name
        } else {
            throw ServletException("Invalid login")
        }

        return LoginResponse(Jwts.builder().setSubject(userName)
                .claim("roles", userDB[userName]?.roles).setIssuedAt(Date())
                .signWith(SignatureAlgorithm.HS256, "secretkey").compact())
    }
}
