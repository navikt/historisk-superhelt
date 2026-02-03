package no.nav.historisk.superhelt.infrastruktur.mdc

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class MdcFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain) {

        try {
            val callId = request.getHeader(MdcHelper.CALL_ID_HEADER) ?: UUID.randomUUID().toString()
            MdcHelper.callId = callId

            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated) {
                val username = authentication.name
                MdcHelper.userIdent= username
            }

            filterChain.doFilter(request, response)

        } finally {
            MdcHelper.clear()
        }
    }


}