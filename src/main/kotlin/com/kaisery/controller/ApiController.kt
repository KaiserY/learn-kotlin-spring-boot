package com.kaisery.controller

import io.jsonwebtoken.Claims
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.InputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api")
class ApiController {

    @Autowired
    lateinit var context: ApplicationContext

    @RequestMapping(value = "role/{role}", method = arrayOf(RequestMethod.GET))
    fun hasRole(@PathVariable role: String, request: HttpServletRequest): Boolean {

        val claims: Claims? = request.getAttribute("claims") as Claims

        return if ((claims?.get("roles") as? List<*>)?.contains(role) == true) true else false
    }

    @RequestMapping(value = "audio", method = arrayOf(RequestMethod.GET))
    fun getAudio(request: HttpServletRequest, response: HttpServletResponse) {
        val resource: Resource = context.getResource("classpath:public/assets/test.mp3")
        val inputStream: InputStream = resource.inputStream
        response.contentType = "audio/mpeg"
        IOUtils.copy(inputStream, response.outputStream);
        response.outputStream.flush()
    }
}
