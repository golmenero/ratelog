package org.raterr.user

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController {

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }
}
