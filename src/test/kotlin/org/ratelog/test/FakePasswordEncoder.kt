package org.ratelog.test

import org.springframework.security.crypto.password.PasswordEncoder

class FakePasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: CharSequence): String = "encoded_$rawPassword"
    override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean =
        "encoded_$rawPassword" == encodedPassword
}
