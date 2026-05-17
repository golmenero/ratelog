package org.raterr.user.followed

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.raterr.UserId
import org.raterr.follow.InMemoryUserFollowRepository
import org.raterr.follow.UserFollow

class FollowedUsersHandlerTest : BehaviorSpec({

    val userFollowRepository = InMemoryUserFollowRepository()
    val handler = FollowedUsersHandler(userFollowRepository)

    beforeTest {
        userFollowRepository.clear()
    }

    Given("a user with followed users") {
        userFollowRepository.addUser(1, "alice")
        userFollowRepository.addUser(2, "bob")
        userFollowRepository.addUser(3, "charlie")

        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 3))

        When("querying followed users") {
            val result = handler.handle(FollowedUsersQuery(UserId(1)))

            Then("should return the list of followed users") {
                result.isRight() shouldBe true
                result.fold(
                    { },
                    {
                        it.size shouldBe 2
                        it[0].username shouldBe "bob"
                        it[1].username shouldBe "charlie"
                    }
                )
            }
        }
    }

    Given("a user with no followed users") {
        userFollowRepository.addUser(1, "alice")

        When("querying followed users") {
            val result = handler.handle(FollowedUsersQuery(UserId(1)))

            Then("should return an empty list") {
                result.isRight() shouldBe true
                result.fold(
                    { },
                    {
                        it.isEmpty() shouldBe true
                    }
                )
            }
        }
    }
})
