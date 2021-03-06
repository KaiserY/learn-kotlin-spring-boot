package com.kaisery.common.multipart

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by kevin on 10/02/15.
 * See full code here : https://github.com/davinkevin/Podcast-Server/blob/d927d9b8cb9ea1268af74316cd20b7192ca92da7/src/main/java/lan/dk/podcastserver/utils/multipart/MultipartFileSender.java
 */
class MultipartFileSender {

    internal val logger = LoggerFactory.getLogger(this.javaClass)

    internal var filepath: Path? = null
    internal var request: HttpServletRequest? = null
    internal var response: HttpServletResponse? = null

    //** internal setter **//
    private fun setFilepath(filepath: Path): MultipartFileSender {
        this.filepath = filepath
        return this
    }

    fun with(httpRequest: HttpServletRequest): MultipartFileSender {
        request = httpRequest
        return this
    }

    fun with(httpResponse: HttpServletResponse): MultipartFileSender {
        response = httpResponse
        return this
    }

    @Throws(Exception::class)
    fun serveResource() {
        if (response == null || request == null) {
            return
        }

        if (!Files.exists(filepath)) {
            logger.error("File doesn't exist at URI : {}", filepath!!.toAbsolutePath().toString())
            response!!.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        val length = Files.size(filepath)
        val fileName = filepath!!.fileName.toString()
        val lastModifiedObj = Files.getLastModifiedTime(filepath)

        if (StringUtils.isEmpty(fileName) || lastModifiedObj == null) {
            response!!.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            return
        }
        val lastModified = LocalDateTime.ofInstant(lastModifiedObj.toInstant(), ZoneId.of(ZoneOffset.systemDefault().id)).toEpochSecond(ZoneOffset.UTC)
        var contentType: String? = "video/mp4"

        // Validate request headers for caching ---------------------------------------------------

        // If-None-Match header should contain "*" or ETag. If so, then return 304.
        val ifNoneMatch = request!!.getHeader("If-None-Match")
        if (ifNoneMatch != null && HttpUtils.matches(ifNoneMatch, fileName)) {
            response!!.setHeader("ETag", fileName) // Required in 304.
            response!!.sendError(HttpServletResponse.SC_NOT_MODIFIED)
            return
        }

        // If-Modified-Since header should be greater than LastModified. If so, then return 304.
        // This header is ignored if any If-None-Match header is specified.
        val ifModifiedSince = request!!.getDateHeader("If-Modified-Since")
        if (ifNoneMatch == null && ifModifiedSince != -1L && ifModifiedSince + 1000 > lastModified) {
            response!!.setHeader("ETag", fileName) // Required in 304.
            response!!.sendError(HttpServletResponse.SC_NOT_MODIFIED)
            return
        }

        // Validate request headers for resume ----------------------------------------------------

        // If-Match header should contain "*" or ETag. If not, then return 412.
        val ifMatch = request!!.getHeader("If-Match")
        if (ifMatch != null && !HttpUtils.matches(ifMatch, fileName)) {
            response!!.sendError(HttpServletResponse.SC_PRECONDITION_FAILED)
            return
        }

        // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
        val ifUnmodifiedSince = request!!.getDateHeader("If-Unmodified-Since")
        if (ifUnmodifiedSince != -1L && ifUnmodifiedSince + 1000 <= lastModified) {
            response!!.sendError(HttpServletResponse.SC_PRECONDITION_FAILED)
            return
        }

        // Validate and process range -------------------------------------------------------------

        // Prepare some variables. The full Range represents the complete file.
        val full = Range(0, length - 1, length)
        val ranges = ArrayList<Range>()

        // Validate and process Range and If-Range headers.
        val range = request!!.getHeader("Range")
        if (range != null) {

            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$".toRegex())) {
                response!!.setHeader("Content-Range", "bytes */" + length) // Required in 416.
                response!!.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                return
            }

            val ifRange = request!!.getHeader("If-Range")
            if (ifRange != null && ifRange != fileName) {
                try {
                    val ifRangeTime = request!!.getDateHeader("If-Range") // Throws IAE if invalid.
                    if (ifRangeTime != -1.toLong()) {
                        ranges.add(full)
                    }
                } catch (ignore: IllegalArgumentException) {
                    ranges.add(full)
                }

            }

            // If any valid If-Range header, then process each part of byte range.
            if (ranges.isEmpty()) {
                for (part in range.substring(6).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                    var start = Range.sublong(part, 0, part.indexOf("-"))
                    var end = Range.sublong(part, part.indexOf("-") + 1, part.length)

                    if (start == -1L) {
                        start = length - end
                        end = length - 1
                    } else if (end == -1L || end > length - 1) {
                        end = length - 1
                    }

                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        response!!.setHeader("Content-Range", "bytes */" + length) // Required in 416.
                        response!!.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                        return
                    }

                    // Add range.
                    ranges.add(Range(start, end, length))
                }
            }
        }

        // Prepare and initialize response --------------------------------------------------------

        // Get content type by file name and set content disposition.
        var disposition = "inline"

        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if (contentType == null) {
            contentType = "application/octet-stream"
        } else if (!contentType.startsWith("image")) {
            // Else, expect for images, determine content disposition. If content type is supported by
            // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
            val accept = request!!.getHeader("Accept")
            disposition = if (accept != null && HttpUtils.accepts(accept, contentType)) "inline" else "attachment"
        }
        logger.debug("Content-Type : {}", contentType)
        // Initialize response.
        response!!.reset()
        response!!.bufferSize = DEFAULT_BUFFER_SIZE
        response!!.setHeader("Content-Type", contentType)
        response!!.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"")
        logger.debug("Content-Disposition : {}", disposition)
        response!!.setHeader("Accept-Ranges", "bytes")
        response!!.setHeader("ETag", fileName)
        response!!.setDateHeader("Last-Modified", lastModified)
        response!!.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME)

        // Send requested file (part(s)) to client ------------------------------------------------

        // Prepare streams.
        BufferedInputStream(Files.newInputStream(filepath)).use { input ->
            response!!.outputStream.use { output ->

                if (ranges.isEmpty() || ranges[0] === full) {

                    // Return full file.
                    logger.info("Return full file")
                    response!!.contentType = contentType
                    response!!.setHeader("Content-Range", "bytes " + full.start + "-" + full.end + "/" + full.total)
                    response!!.setHeader("Content-Length", full.length.toString())
                    Range.copy(input, output, length, full.start, full.length)

                } else if (ranges.size == 1) {

                    // Return single part of file.
                    val r = ranges[0]
                    logger.info("Return 1 part of file : from ({}) to ({})", r.start, r.end)
                    response!!.contentType = contentType
                    response!!.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total)
                    response!!.setHeader("Content-Length", r.length.toString())
                    response!!.status = HttpServletResponse.SC_PARTIAL_CONTENT // 206.

                    // Copy single part range.
                    Range.copy(input, output, length, r.start, r.length)

                } else {

                    // Return multiple parts of file.
                    response!!.contentType = "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY
                    response!!.status = HttpServletResponse.SC_PARTIAL_CONTENT // 206.

                    // Cast back to ServletOutputStream to get the easy println methods.

                    // Copy multi part range.
                    for (r in ranges) {
                        logger.info("Return multi part of file : from ({}) to ({})", r.start, r.end)
                        // Add multipart boundary and header fields for every range.
                        output.println()
                        output.println("--" + MULTIPART_BOUNDARY)
                        output.println("Content-Type: " + contentType!!)
                        output.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total)

                        // Copy single part range of multi part range.
                        Range.copy(input, output, length, r.start, r.length)
                    }

                    // End with multipart boundary.
                    output.println()
                    output.println("--$MULTIPART_BOUNDARY--")
                }
            }
        }

    }

    private class Range
    /**
     * Construct a byte range.

     * @param start Start of the byte range.
     * *
     * @param end   End of the byte range.
     * *
     * @param total Total length of the byte source.
     */
    (internal var start: Long, internal var end: Long, internal var total: Long) {
        internal var length: Long = 0

        init {
            this.length = end - start + 1
        }

        companion object {

            fun sublong(value: String, beginIndex: Int, endIndex: Int): Long {
                val substring = value.substring(beginIndex, endIndex)
                return if (substring.length > 0) java.lang.Long.parseLong(substring) else -1
            }

            @Throws(IOException::class)
            fun copy(input: InputStream, output: OutputStream, inputSize: Long, start: Long, length: Long) {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read: Int = -1

                if (inputSize == length) {
                    // Write full range.
                    while (input.read(buffer).let { read = it; read > 0 }) {
                        output.write(buffer, 0, read)
                        output.flush()
                    }
                } else {
                    input.skip(start)
                    var toRead = length

                    while (input.read(buffer).let { read = it; it > 0 }) {
                        if (read.toLong().let { toRead -= it; toRead > 0 }) {
                            output.write(buffer, 0, read)
                            output.flush()
                        } else {
                            output.write(buffer, 0, toRead.toInt() + read)
                            output.flush()
                            break
                        }
                    }
                }
            }
        }
    }

    private object HttpUtils {

        /**
         * Returns true if the given accept header accepts the given value.

         * @param acceptHeader The accept header.
         * *
         * @param toAccept     The value to be accepted.
         * *
         * @return True if the given accept header accepts the given value.
         */
        fun accepts(acceptHeader: String, toAccept: String): Boolean {
            val acceptValues = acceptHeader.split("\\s*(,|;)\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            Arrays.sort(acceptValues)

            return Arrays.binarySearch(acceptValues, toAccept) > -1
                || Arrays.binarySearch(acceptValues, toAccept.replace("/.*$".toRegex(), "/*")) > -1
                || Arrays.binarySearch(acceptValues, "*/*") > -1
        }

        /**
         * Returns true if the given match header matches the given value.

         * @param matchHeader The match header.
         * *
         * @param toMatch     The value to be matched.
         * *
         * @return True if the given match header matches the given value.
         */
        fun matches(matchHeader: String, toMatch: String): Boolean {
            val matchValues = matchHeader.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            Arrays.sort(matchValues)
            return Arrays.binarySearch(matchValues, toMatch) > -1 || Arrays.binarySearch(matchValues, "*") > -1
        }
    }

    companion object {

        private val DEFAULT_BUFFER_SIZE = 20480 // ..bytes = 20KB.
        private val DEFAULT_EXPIRE_TIME = 604800000L // ..ms = 1 week.
        private val MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES"

        fun fromPath(path: Path): MultipartFileSender {
            return MultipartFileSender().setFilepath(path)
        }

        fun fromFile(file: File): MultipartFileSender {
            return MultipartFileSender().setFilepath(file.toPath())
        }

        fun fromURIString(uri: String): MultipartFileSender {
            return MultipartFileSender().setFilepath(Paths.get(uri))
        }
    }
}
