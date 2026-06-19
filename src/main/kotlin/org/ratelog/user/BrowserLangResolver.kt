package org.ratelog.user

import jakarta.servlet.http.HttpServletRequest
import org.ratelog.Lang

class BrowserLangResolver {
    companion object {
        fun resolve(request: HttpServletRequest): Lang {
            val browserLocales = request.locales.toList().map { it.language }
            return when(browserLocales.first()) {
                "es" -> Lang.es
                else -> Lang.en
            }
        }
    }
}