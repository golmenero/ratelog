package org.ratelog.i18n

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.ratelog.Lang
import org.ratelog.user.BrowserLangResolver
import org.ratelog.user.UserDetailsService
import org.springframework.web.servlet.LocaleResolver
import java.util.Locale

class UserAwareLocaleResolver : LocaleResolver {

    override fun resolveLocale(request: HttpServletRequest): Locale =
        UserDetailsService
            .getCurrentUser()
            ?.lang
            ?.locale
            ?: BrowserLangResolver.resolve(request).locale

    override fun setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: Locale?) {
    }
}
