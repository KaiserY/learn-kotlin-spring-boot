package com.kaisery.controller

import com.kaisery.common.multipart.MultipartFileSender
import io.jsonwebtoken.Claims
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class FileUploadResponse(val success: Boolean, val error: String? = null, val preventRetry: Boolean? = false, val reset: Boolean? = false)

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

    @RequestMapping(value = "video", method = arrayOf(RequestMethod.GET))
    fun getVideo(request: HttpServletRequest, response: HttpServletResponse) {
//        val resource: Resource = context.getResource("classpath:public/assets/test.mp4")
//        val inputStream: InputStream = resource.inputStream
//        response.contentType = "video/mp4"
//        IOUtils.copy(inputStream, response.outputStream);
//        response.outputStream.flush()
        MultipartFileSender.fromFile(File("/home/yueyang/Downloads/Taylor Swift - Shake It Off.mp4"))
            .with(request)
            .with(response)
            .serveResource()
    }

    @RequestMapping(value = "upload", method = arrayOf(RequestMethod.POST))
    fun upload(
        @RequestParam("filename") fileName: String,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("partindex") partindex: Int,
        @RequestParam("partbyteoffset") partByteOffset: Int,
        @RequestParam("chunksize") chunkSize: Int,
        @RequestParam("totalparts") totalParts: Int,
        @RequestParam("totalfilesize") totalFileSize: Int
    ): FileUploadResponse {

        val append: Boolean = if (partindex > 0) true else false

        val outputStream: BufferedOutputStream = BufferedOutputStream(FileOutputStream(File("/tmp/" + fileName), append))

        outputStream.write(file.inputStream.readBytes(chunkSize))

        outputStream.close()

        return FileUploadResponse(true)
    }
}
