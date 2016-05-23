package com.kaisery.controller

import com.kaisery.common.token.Token
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.Serializable
import java.util.*
import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

data class User(val id: Int, val name: String, var password: String, var roles: Array<String> = arrayOf("user")) : Serializable

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
    fun login(@RequestBody body: LoginRequest?, response: HttpServletResponse): LoginResponse {

        val userName: String = if (userDB.containsKey(body?.name) &&
                userDB[body?.name]?.password == body?.password) {
            body!!.name
        } else {
            throw ServletException("Invalid login")
        }

        val token: String = Token.builder.setSubject(userName)
                .claim("roles", userDB[userName]?.roles)
                .setIssuedAt(Date())
                .signWith(SignatureAlgorithm.HS256, "secretkey")
                .compact()

        val cookie = Cookie("token", token)
        cookie.isHttpOnly = true

        response.addCookie(cookie)

        return LoginResponse("Token is in the Cookie :)")
    }
}
