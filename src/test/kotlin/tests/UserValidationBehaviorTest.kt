package behavior

import com.speechify.domain.ClientType
import com.speechify.policy.CreditLimit
import com.speechify.policy.UserCredits
import com.speechify.service.AddUserRequest
import dsl.UserTestDSL
import fixtures.TestFixtures
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import java.time.LocalDate

@DisplayName("User Validation Behavior")
class UserValidationBehaviorTest {

    private lateinit var dsl: UserTestDSL

    @BeforeEach
    fun setup() {
        dsl = UserTestDSL()
        dsl.withDefaultUserService()
    }

    @Test
    @DisplayName("rejects user under 21 years old")
    fun `rejects user under 21 years old`() {
        // Given
        dsl.givenRegularClient("client-1")
        dsl.givenUserRequest(
            firstname = "Young",
            surname = "User",
            email = "young@example.com",
            dateOfBirth = LocalDate.now().minusYears(20), // 20 years old
            clientId = "client-1"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsRejectedWithValidationError("21")
    }

    @Test
    @DisplayName("accepts user exactly 21 years old")
    fun `accepts user exactly 21 years old`() {
        // Given
        dsl.givenRegularClient("client-1")
        dsl.givenUserRequest(
            firstname = "Legal",
            surname = "User",
            email = "legal@example.com",
            dateOfBirth = LocalDate.now().minusYears(21),
            clientId = "client-1"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsCreatedSuccessfully()
    }

    @Test
    @DisplayName("accepts user over 21 years old")
    fun `accepts user over 21 years old`() {
        // Given
        dsl.givenRegularClient("client-1")
        dsl.givenUserRequest(
            firstname = "Adult",
            surname = "User",
            email = "adult@example.com",
            dateOfBirth = LocalDate.now().minusYears(30),
            clientId = "client-1"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsCreatedSuccessfully()
    }

    @Test
    @DisplayName("rejects duplicate email")
    fun `rejects duplicate email`() {
        // Given
        dsl.givenRegularClient("client-1")
        dsl.givenExistingUser(email = "existing@example.com")
        dsl.givenUserRequest(
            email = "existing@example.com",
            clientId = "client-1"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsRejectedDueToDuplicateEmail()
    }

    @Test
    @DisplayName("rejects request with non-existent client")
    fun `rejects request with non-existent client`() {
        // Given - no client setup
        dsl.givenUserRequest(
            clientId = "non-existent-client"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsRejectedDueToClientNotFound()
    }

    @Test
    @DisplayName("rejects empty firstname")
    fun `rejects empty firstname`() {
        // Given
        dsl.givenRegularClient("client-1")
        dsl.givenUserRequest(
            firstname = "",
            clientId = "client-1"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsRejectedWithValidationError("firstname")
    }

    @Test
    @DisplayName("rejects empty surname")
    fun `rejects empty surname`() {
        // Given
        dsl.givenRegularClient("client-1")
        dsl.givenUserRequest(
            surname = "",
            clientId = "client-1"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsRejectedWithValidationError("surname")
    }

    @Test
    @DisplayName("rejects empty email")
    fun `rejects empty email`() {
        // Given
        dsl.givenRegularClient("client-1")
        dsl.givenUserRequest(
            email = "",
            clientId = "client-1"
        )

        // When
        dsl.whenAddingUser()

        // Then
        dsl.thenUserIsRejectedWithValidationError("email")
    }
}
