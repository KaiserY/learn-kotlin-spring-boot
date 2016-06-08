package com.kaisery.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class Error(val errorCode: Int, val attr: Map<String, Any>)

@RestController
class CustomErrorController : ErrorController {
    companion object {
        const val PATH: String = "/error"
    }

    @Value("\${debug}")
    val debug: Boolean = false;

    @Autowired
    lateinit var errorAttributes: ErrorAttributes

    override fun getErrorPath(): String? {
        return PATH
    }

    @RequestMapping(value = PATH)
    fun error(request: HttpServletRequest, response: HttpServletResponse): Error {
        return Error(
            response.status,
            errorAttributes.getErrorAttributes(ServletRequestAttributes(request), debug)
        )
    }
}
