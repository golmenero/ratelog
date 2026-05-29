package org.ratelog.user.changelang

import org.ratelog.Lang
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.AppUserDetails
import org.ratelog.user.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ChangeLangController(
    private val handler: ChangeLangHandler,
) {

    @PostMapping("/lang")
    fun changeLang(
        @CurrentUser user: User,
        @RequestParam("lang") lang: String,
        redirectAttributes: RedirectAttributes
    ): String {
        val newLang = Lang(lang)
        return ChangeLangCommand(
            userId = user.id!!,
            lang = newLang,
        ).let(handler::handle)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", "Could not update language.")
                    "redirect:/profile"
                },
                {
                    refreshUserLang(newLang)
                    "redirect:/profile"
                }
            )
    }

    private fun refreshUserLang(newLang: Lang) {
        val auth = SecurityContextHolder.getContext().authentication
        val currentDetails = auth.principal as? AppUserDetails ?: return

        val newAuth = UsernamePasswordAuthenticationToken(
            AppUserDetails(
                id = currentDetails.id,
                username = currentDetails.username,
                email = currentDetails.email,
                password = currentDetails.password,
                lang = newLang,
            ),
            auth.credentials,
            auth.authorities
        )
        SecurityContextHolder.getContext().authentication = newAuth
    }
}
