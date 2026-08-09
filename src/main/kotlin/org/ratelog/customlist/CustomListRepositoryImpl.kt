package org.ratelog.customlist

import org.ratelog.ListName
import org.ratelog.MediaType
import org.ratelog.toDateString
import org.ratelog.user.User
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class CustomListRepositoryImpl(
    private val customListDAO: CustomListDAO,
    private val customListItemDAO: CustomListItemDAO
) : CustomListRepository {

    override fun findById(id: CustomList.Id): CustomList? =
        customListDAO.findById(id.value).getOrNull()?.let { entity ->
            val items = customListItemDAO.findByListId(entity.id!!).map { it.toDomain() }
            entity.toDomain(items)
        }

    override fun findByUserId(userId: User.Id): List<CustomListSummary> =
        customListDAO.findByUserId(userId.value).map { it.toSummary() }

    override fun findPublicByUserId(userId: User.Id): List<CustomListSummary> =
        customListDAO.findPublicByUserId(userId.value).map { it.toSummary() }

    override fun save(list: CustomList): CustomList {
        val savedEntity = customListDAO.save(list.toEntity())
        return savedEntity.toDomain(emptyList())
    }

    override fun delete(id: CustomList.Id) {
        customListItemDAO.deleteByListId(id.value)
        customListDAO.deleteById(id.value)
    }

    override fun addItem(item: CustomListItem): CustomListItem {
        val savedEntity = customListItemDAO.save(item.toEntity())
        return savedEntity.toDomain()
    }

    override fun removeItem(itemId: CustomListItem.Id) {
        customListItemDAO.deleteById(itemId.value)
    }

    override fun findItemById(itemId: CustomListItem.Id): CustomListItem? =
        customListItemDAO.findById(itemId.value).getOrNull()?.toDomain()

    override fun findItemByListIdAndMediaIdAndMediaType(listId: CustomList.Id, mediaId: Long, mediaType: String): CustomListItem? =
        customListItemDAO.findByListIdAndMediaIdAndMediaType(listId.value, mediaId, mediaType).getOrNull()?.toDomain()

    private fun CustomListEntity.toDomain(items: List<CustomListItem>): CustomList =
        CustomList(
            id = id!!.let { CustomList.Id(it) },
            userId = User.Id(userId),
            name = ListName(name),
            isPublic = isPublic,
            createdAtEpochMs = createdAtEpochMs,
            items = items
        )

    private fun CustomListEntity.toSummary(): CustomListSummary =
        CustomListSummary(
            id = CustomList.Id(id!!),
            name = ListName(name),
            isPublic = isPublic,
            createdAt = createdAtEpochMs.toDateString()
        )

    private fun CustomList.toEntity(): CustomListEntity =
        CustomListEntity(
            id = id?.value,
            userId = userId.value,
            name = name.value,
            isPublic = isPublic,
            createdAtEpochMs = createdAtEpochMs
        )

    private fun CustomListItemEntity.toDomain(): CustomListItem =
        CustomListItem(
            id = id!!.let { CustomListItem.Id(it) },
            listId = CustomList.Id(listId),
            mediaId = mediaId,
            mediaType = MediaType.valueOf(mediaType),
            position = position,
            addedAtEpochMs = addedAtEpochMs
        )

    private fun CustomListItem.toEntity(): CustomListItemEntity =
        CustomListItemEntity(
            id = id?.value,
            listId = listId.value,
            mediaId = mediaId,
            mediaType = mediaType.name,
            position = position,
            addedAtEpochMs = addedAtEpochMs
        )
}
