package org.raterr.user

import org.raterr.rating.RatingRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class RegisterController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val ratingRepository: RatingRepository
) {

    @GetMapping("/register")
    fun registerPage(model: Model): String {
        return "register"
    }

    @PostMapping("/register")
    fun register(
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("password") password: String,
        redirectAttributes: RedirectAttributes
    ): String {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "All fields are required")
            return "redirect:/register"
        }

        if (username.length < 3 || username.length > 50) {
            redirectAttributes.addFlashAttribute("error", "Username must be between 3 and 50 characters")
            return "redirect:/register"
        }

        if (password.length < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters")
            return "redirect:/register"
        }

        if (userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username already exists")
            return "redirect:/register"
        }

        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already exists")
            return "redirect:/register"
        }

        val hashedPassword = passwordEncoder.encode(password)

        val user = User(
            username = username,
            email = email,
            passwordHash = hashedPassword
        )

        val savedUser = userRepository.save(user)

        val ratingsWithoutUser = ratingRepository.findAllWithoutUser()
        ratingsWithoutUser.forEach { rating ->
            ratingRepository.save(rating.copy(userId = savedUser.id!!))
        }

        return "redirect:/login"
    }
}
