package behavior

import com.speechify.domain.ClientType
import dsl.UserTestDSL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import java.time.LocalDate

@DisplayName("Credit Limit Behavior")
class CreditLimitBehaviorTest {

    private lateinit var dsl: UserTestDSL

    @BeforeEach
    fun setup() {
        dsl = UserTestDSL()
        dsl.withDefaultUserService()
    }

    @Test
    @DisplayName("VeryImportantClient user has no credit limit")
    fun `VeryImportantClient user has no credit limit`() {
        // Given
        dsl.givenVeryImportantClient("vip-client")
        dsl.givenUserRequest(
            firstname = "VIP",
            surname = "User",
            email = "vip@example.com",
            dateOfBirth = LocalDate.now().minusYears(30),
            clientId = "vip-client"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsCreatedSuccessfully()
        dsl.thenCreatedUserHasNoCreditLimit()
    }

    @Test
    @DisplayName("ImportantClient user gets 20000 credit limit")
    fun `ImportantClient user gets 20000 credit limit`() {
        // Given
        dsl.givenImportantClient("important-client")
        dsl.givenUserRequest(
            firstname = "Important",
            surname = "User",
            email = "important@example.com",
            dateOfBirth = LocalDate.now().minusYears(30),
            clientId = "important-client"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsCreatedSuccessfully()
        dsl.thenCreatedUserHasCreditLimit(20000.0)
    }

    @Test
    @DisplayName("Regular client user gets 10000 credit limit")
    fun `Regular client user gets 10000 credit limit`() {
        // Given
        dsl.givenRegularClient("regular-client")
        dsl.givenUserRequest(
            firstname = "Regular",
            surname = "User",
            email = "regular@example.com",
            dateOfBirth = LocalDate.now().minusYears(30),
            clientId = "regular-client"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsCreatedSuccessfully()
        dsl.thenCreatedUserHasCreditLimit(10000.0)
    }
}
