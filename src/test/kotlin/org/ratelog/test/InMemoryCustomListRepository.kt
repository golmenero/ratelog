package org.ratelog.test

import org.ratelog.customlist.*
import org.ratelog.user.User
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryCustomListRepository : CustomListRepository {
    private val lists = ConcurrentHashMap<CustomList.Id, CustomList>()
    private val items = ConcurrentHashMap<CustomListItem.Id, CustomListItem>()
    private val listIdGenerator = AtomicLong(1)
    private val itemIdGenerator = AtomicLong(1)

    override fun findById(id: CustomList.Id): CustomList? =
        lists[id]?.let { list ->
            val listItems = items.values.filter { it.listId == id }.sortedBy { it.position }
            list.copy(items = listItems)
        }

    override fun findByUserId(userId: User.Id): List<CustomList> =
        lists.values
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAtEpochMs }
            .map { list ->
                val listItems = items.values.filter { it.listId == list.id }.sortedBy { it.position }
                list.copy(items = listItems)
            }

    override fun findPublicByUserId(userId: User.Id): List<CustomList> =
        lists.values
            .filter { it.userId == userId && it.isPublic }
            .sortedByDescending { it.createdAtEpochMs }
            .map { list ->
                val listItems = items.values.filter { it.listId == list.id }.sortedBy { it.position }
                list.copy(items = listItems)
            }

    override fun save(list: CustomList): CustomList {
        val savedList = if (list.id == null) {
            list.copy(id = CustomList.Id(listIdGenerator.getAndIncrement()))
        } else {
            list
        }
        lists[savedList.id!!] = savedList
        return savedList
    }

    override fun delete(id: CustomList.Id) {
        lists.remove(id)
        items.entries.removeAll { it.value.listId == id }
    }

    override fun addItem(item: CustomListItem): CustomListItem {
        val savedItem = if (item.id == null) {
            item.copy(id = CustomListItem.Id(itemIdGenerator.getAndIncrement()))
        } else {
            item
        }
        items[savedItem.id!!] = savedItem
        return savedItem
    }

    override fun removeItem(itemId: CustomListItem.Id) {
        items.remove(itemId)
    }

    override fun findItemById(itemId: CustomListItem.Id): CustomListItem? =
        items[itemId]

    override fun findItemByListIdAndMediaIdAndMediaType(listId: CustomList.Id, mediaId: Long, mediaType: String): CustomListItem? =
        items.values.find { it.listId == listId && it.mediaId == mediaId && it.mediaType.name == mediaType }
}
