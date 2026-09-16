package org.ratelog.admin.updaterole

sealed interface AdminUpdateRoleHandlerError {
    data object Forbidden : AdminUpdateRoleHandlerError
    data object UserNotFound : AdminUpdateRoleHandlerError
    data object CannotPromoteToSuperadmin : AdminUpdateRoleHandlerError
    data object CannotChangeSuperadminRole : AdminUpdateRoleHandlerError
    data object CannotDemoteYourself : AdminUpdateRoleHandlerError
}
