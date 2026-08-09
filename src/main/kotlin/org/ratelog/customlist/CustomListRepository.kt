package org.ratelog.customlist

import org.ratelog.user.User

interface CustomListRepository {
    fun findById(id: CustomList.Id): CustomList?
    fun findByUserId(userId: User.Id): List<CustomListSummary>
    fun findPublicByUserId(userId: User.Id): List<CustomListSummary>
    fun save(list: CustomList): CustomList
    fun delete(id: CustomList.Id)
    fun addItem(item: CustomListItem): CustomListItem
    fun removeItem(itemId: CustomListItem.Id)
    fun findItemById(itemId: CustomListItem.Id): CustomListItem?
    fun findItemByListIdAndMediaIdAndMediaType(listId: CustomList.Id, mediaId: Long, mediaType: String): CustomListItem?
}
