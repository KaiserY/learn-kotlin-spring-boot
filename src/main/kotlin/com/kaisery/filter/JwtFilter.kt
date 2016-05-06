package com.kaisery.filter

import com.kaisery.common.token.Token
import io.jsonwebtoken.Claims
import io.jsonwebtoken.SignatureException
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class JwtFilter : GenericFilterBean() {

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {

        val token: String = (request as HttpServletRequest?)?.getHeader("Cookie")?.substring(6)
                ?: throw ServletException("Missing or invalid Cookie.")

        try {
            val claims: Claims = Token.parser.setSigningKey("secretkey").parseClaimsJws(token).body

            request?.setAttribute("claims", claims) ?: throw ServletException("Request is null ???")
        } catch (e: SignatureException) {
            throw ServletException("Invalid token.")
        }

        chain?.doFilter(request, response)
    }
}
