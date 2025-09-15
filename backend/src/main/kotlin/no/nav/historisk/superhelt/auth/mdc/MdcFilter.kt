package no.nav.historisk.superhelt.auth.mdc

import io.micrometer.core.instrument.Tag
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.util.UUID

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class MdcFilter() : GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        try {

                if (request is HttpServletRequest ) {
                    val callId = request.getHeader(MdcHelper.CALL_ID_HEADER) ?: UUID.randomUUID().toString()
                    MdcHelper.callId = callId
                }

             chain.doFilter(request, response)

        } finally {
            MdcHelper.clear()
        }
    }


}