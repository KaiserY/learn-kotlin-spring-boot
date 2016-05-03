package com.kaisery.controller

import com.kaisery.common.logging.Logging
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicLong

data class Greeting(val id: Long, val content: String)

@RestController
class GreetingController {

    val counter = AtomicLong()

    @RequestMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): Greeting {
        Logging.logger.info("greeting count = " + counter.incrementAndGet())
        return Greeting(counter.get(), "Hello, $name")
    }
}
