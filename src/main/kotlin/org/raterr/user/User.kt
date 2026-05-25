package org.raterr.user

import org.raterr.Email
import org.raterr.Username

data class User(
    val id: Id?,
    val username: Username,
    val email: Email,
    val passwordHash: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {
    data class Id(val value: Long)
}
