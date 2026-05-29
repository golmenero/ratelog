package org.ratelog.i18n

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.ratelog.user.UserDetailsService
import org.springframework.web.servlet.LocaleResolver
import java.util.Locale

class UserAwareLocaleResolver : LocaleResolver {

    override fun resolveLocale(request: HttpServletRequest): Locale {
        val user = UserDetailsService.getCurrentUser()
        return if (user != null) {
            when (user.lang.value) {
                "es" -> Locale("es")
                else -> Locale("en")
            }
        } else {
            val browserLocales = request.locales.toList().map { it.language }
            when(browserLocales.first()) {
                "es" -> Locale("es")
                else -> Locale("en")
            }
        }
    }

    override fun setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: Locale?) {
    }
}
