package org.ratelog.customlist

import org.ratelog.user.User

interface CustomListRepository {
    fun findById(id: CustomList.Id): CustomList?
    fun findByUserId(userId: User.Id): List<CustomListSummary>
    fun findPublicByUserId(userId: User.Id): List<CustomListSummary>
    fun findPublicLists(): List<CustomListSummary>
    fun save(list: CustomList): CustomList
    fun update(list: CustomList)
    fun delete(id: CustomList.Id)
    fun countByUserId(userId: User.Id): Int
    fun addItem(item: CustomListItem): CustomListItem
    fun removeItem(itemId: CustomListItem.Id)
    fun findItemById(itemId: CustomListItem.Id): CustomListItem?
    fun findItemByListIdAndTmdbIdAndMediaType(listId: CustomList.Id, tmdbId: Int, mediaType: String): CustomListItem?
}
