package org.ratelog.user.changemetadataLang

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
class ChangeMetadataLangController(
    private val handler: ChangeMetadataLangHandler,
) {

    @PostMapping("/metadata-lang")
    fun changeMetadataLang(
        @CurrentUser user: User,
        @RequestParam("metadataLang") metadataLang: String,
        redirectAttributes: RedirectAttributes
    ): String {
        val newMetadataLang = Lang.valueOf(metadataLang)
        return ChangeMetadataLangCommand(
            userId = user.id!!,
            metadataLang = newMetadataLang,
        ).let(handler::handle)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", "Could not update metadata language.")
                    "redirect:/profile"
                },
                {
                    refreshUserMetadataLang(newMetadataLang)
                    "redirect:/profile"
                }
            )
    }

    private fun refreshUserMetadataLang(newMetadataLang: Lang) {
        val auth = SecurityContextHolder.getContext().authentication
        val currentDetails = auth.principal as? AppUserDetails ?: return

        val newAuth = UsernamePasswordAuthenticationToken(
            AppUserDetails(
                id = currentDetails.id,
                username = currentDetails.username,
                email = currentDetails.email,
                password = currentDetails.password,
                lang = currentDetails.lang,
                metadataLang = newMetadataLang,
            ),
            auth.credentials,
            auth.authorities
        )
        SecurityContextHolder.getContext().authentication = newAuth
    }
}
