package org.ratelog.user

import jakarta.servlet.http.HttpServletRequest
import java.util.Locale

class BrowserLangResolver {
    companion object {
        fun resolve(request: HttpServletRequest): Locale {
            val browserLocales = request.locales.toList().map { it.language }
            return when(browserLocales.first()) {
                "es" -> Locale.of("es")
                else -> Locale.of("en")
            }
        }
    }
}