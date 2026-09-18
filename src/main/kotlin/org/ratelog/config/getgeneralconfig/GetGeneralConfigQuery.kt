package org.ratelog.config.getgeneralconfig

import org.ratelog.config.GeneralConfig
import org.ratelog.user.User

data class GetGeneralConfigQuery(
    val currentUser: User,
)
