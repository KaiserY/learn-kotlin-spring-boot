package com.kaisery.controller

import com.kaisery.common.logging.Logging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicLong
import javax.servlet.http.HttpServletResponse

data class Greeting(val id: Long, val content: String)

@RestController
class GreetingController {

    val counter = AtomicLong()

    @RequestMapping(value = "/greeting", method = arrayOf(RequestMethod.GET))
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): Greeting {
        Logging.logger.info("greeting count = " + counter.incrementAndGet())

        return Greeting(counter.get(), "Hello, $name")
    }
}
