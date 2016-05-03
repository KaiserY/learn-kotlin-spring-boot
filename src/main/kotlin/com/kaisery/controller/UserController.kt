package com.kaisery.controller

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class LoginRequest(val name: String, val password: String)

data class LoginResponse(val token: String)

@RestController
class UserController {
    fun login(@RequestBody request: LoginRequest): LoginResponse {

        return LoginResponse("")
    }
}
