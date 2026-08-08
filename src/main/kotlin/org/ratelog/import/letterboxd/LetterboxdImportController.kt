package org.ratelog.import.letterboxd

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class LetterboxdImportController(
    private val handler: LetterboxdImportHandler,
) {

    @PostMapping("/import/letterboxd")
    fun importCsv(
        @CurrentUser user: User,
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("error", "error.import.empty")
            return "redirect:/profile"
        }

        return ImportLetterboxdCommand(
            userId = user.id!!,
            csvContent = file.bytes,
        ).let(handler::handle)
            .mapLeft { LetterboxdImportError.ParseError }
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", "error.import.parse")
                    "redirect:/profile"
                },
                { result ->
                    redirectAttributes.addFlashAttribute("importResult", result)
                    "redirect:/profile"
                }
            )
    }
}
