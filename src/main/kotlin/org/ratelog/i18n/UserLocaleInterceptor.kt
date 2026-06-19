package org.ratelog.i18n

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.ratelog.Lang
import org.ratelog.user.BrowserLangResolver
import org.ratelog.user.UserDetailsService
import org.springframework.web.servlet.LocaleResolver
import java.util.Locale

class UserAwareLocaleResolver : LocaleResolver {

    override fun resolveLocale(request: HttpServletRequest): Locale {
        val user = UserDetailsService.getCurrentUser()
        return if (user != null) {
            when (user.lang) {
                Lang.es -> Locale.of("es")
                else -> Locale.of("en")
            }
        } else BrowserLangResolver.resolve(request)
    }

    override fun setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: Locale?) {
    }
}
