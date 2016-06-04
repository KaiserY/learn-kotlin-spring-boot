package com.kaisery.controller

import io.jsonwebtoken.Claims
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class ApiController {

    @RequestMapping(value = "role/{role}", method = arrayOf(RequestMethod.GET))
    fun hasRole(@PathVariable role: String, request: HttpServletRequest): Boolean {

        val claims: Claims? = request.getAttribute("claims") as Claims

        return if ((claims?.get("roles") as? List<*>)?.contains(role) == true) true else false
    }

    @RequestMapping(value = "audio", method = arrayOf(RequestMethod.GET))
    fun getAudio(request: HttpServletRequest) {

    }
}
