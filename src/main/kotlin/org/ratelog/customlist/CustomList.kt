package org.ratelog.customlist

import org.ratelog.ListName
import org.ratelog.MediaType
import org.ratelog.toDateString
import org.ratelog.user.User

data class CustomList(
    val id: Id?,
    val userId: User.Id,
    val name: ListName,
    val isPublic: Boolean,
    val createdAtEpochMs: Long,
    val items: List<CustomListItem> = emptyList()
) {
    data class Id(val value: Long)
}

data class CustomListItem(
    val id: Id?,
    val listId: CustomList.Id,
    val mediaId: Long,
    val mediaType: MediaType,
    val position: Int,
    val addedAtEpochMs: Long
) {
    data class Id(val value: Long)
}
