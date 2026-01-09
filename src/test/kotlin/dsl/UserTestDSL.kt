package dsl

import com.speechify.domain.Client
import com.speechify.domain.ClientType
import com.speechify.domain.User
import com.speechify.policy.CreditLimit
import com.speechify.policy.UserCredits
import com.speechify.policy.UserCreditsDefault
import com.speechify.service.AddUserRequest
import com.speechify.service.AddUserResult
import com.speechify.service.UserDefault
import com.speechify.service.UserService
import com.speechify.service.UserValidator
import drivers.InMemoryClientRepository
import drivers.InMemoryUserRepository
import fixtures.TestFixtures
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class UserTestDSL {
    val userRepository = InMemoryUserRepository()
    val clientRepository = InMemoryClientRepository()

    private var currentRequest: AddUserRequest? = null
    private var currentResult: AddUserResult? = null
    private var currentUser: User? = null
    private var userService: UserService? = null

    fun withUserService(service: UserService) {
        this.userService = service
    }

    fun withUserDefault() {
        val creditPolicy = UserCreditsDefault()
        val validator = UserValidator()
        this.userService = UserDefault(
            userRepository = userRepository,
            clientRepository = clientRepository,
            creditPolicy = creditPolicy,
            validator = validator
        )
    }

    // GIVEN methods
    fun givenClient(id: String, name: String, type: ClientType) {
        clientRepository.addClient(Client(id = id, name = name, type = type))
    }

    fun givenVeryImportantClient(id: String = "vip-client") {
        givenClient(id, "VeryImportantClient", ClientType.VERY_IMPORTANT)
    }

    fun givenImportantClient(id: String = "important-client") {
        givenClient(id, "ImportantClient", ClientType.IMPORTANT)
    }

    fun givenRegularClient(id: String = "regular-client") {
        givenClient(id, "RegularClient", ClientType.REGULAR)
    }

    fun givenExistingUser(email: String, firstname: String = "Existing", surname: String = "User") {
        val client = clientRepository.findAll().firstOrNull() ?: run {
            givenRegularClient()
            clientRepository.findById("regular-client")!!
        }
        userRepository.addUser(TestFixtures.aUser(
            email = email,
            firstname = firstname,
            surname = surname,
            client = client
        ))
    }

    fun givenUserRequest(
        firstname: String = "John",
        surname: String = "Doe",
        email: String = "john@example.com",
        dateOfBirth: LocalDate? = LocalDate.now().minusYears(25),
        clientId: String = "regular-client"
    ) {
        currentRequest = AddUserRequest(
            firstname = firstname,
            surname = surname,
            email = email,
            dateOfBirth = dateOfBirth,
            clientId = clientId
        )
    }

    // WHEN methods
    fun whenAddingUser() {
        requireNotNull(userService) { "UserService not configured. Call withUserService() first." }
        requireNotNull(currentRequest) { "No request configured. Call givenUserRequest() first." }
        currentResult = userService!!.addUser(currentRequest!!)
    }

    fun whenAddingUser(request: AddUserRequest) {
        requireNotNull(userService) { "UserService not configured. Call withUserService() first." }
        currentRequest = request
        currentResult = userService!!.addUser(request)
    }

    fun whenUpdatingUser(user: User) {
        requireNotNull(userService) { "UserService not configured. Call withUserService() first." }
        currentUser = user
        userService!!.updateUser(user)
    }

    fun whenFetchingUserByEmail(email: String) {
        requireNotNull(userService) { "UserService not configured. Call withUserService() first." }
        currentUser = userService!!.getUserByEmail(email)
    }

    // THEN methods
    fun thenUserIsCreatedSuccessfully() {
        assertNotNull(currentResult, "Result should not be null")
        assertTrue(currentResult is AddUserResult.Success,
            "Expected Success but got: $currentResult")
    }

    fun thenUserIsRejectedWithValidationError(expectedReason: String? = null) {
        assertNotNull(currentResult, "Result should not be null")
        assertTrue(currentResult is AddUserResult.ValidationError,
            "Expected ValidationError but got: $currentResult")
        if (expectedReason != null) {
            val error = currentResult as AddUserResult.ValidationError
            assertTrue(error.reason.contains(expectedReason, ignoreCase = true),
                "Expected reason to contain '$expectedReason' but was '${error.reason}'")
        }
    }

    fun thenUserIsRejectedDueToDuplicateEmail() {
        assertNotNull(currentResult, "Result should not be null")
        assertTrue(currentResult is AddUserResult.DuplicateEmail,
            "Expected DuplicateEmail but got: $currentResult")
    }

    fun thenUserIsRejectedDueToClientNotFound() {
        assertNotNull(currentResult, "Result should not be null")
        assertTrue(currentResult is AddUserResult.ClientNotFound,
            "Expected ClientNotFound but got: $currentResult")
    }

    fun thenCreatedUserHasCreditLimit(expectedLimit: Double) {
        assertTrue(currentResult is AddUserResult.Success, "Expected Success result")
        val user = (currentResult as AddUserResult.Success).user
        assertTrue(user.hasCreditLimit, "User should have credit limit")
        assertEquals(expectedLimit, user.creditLimit, "Credit limit mismatch")
    }

    fun thenCreatedUserHasNoCreditLimit() {
        assertTrue(currentResult is AddUserResult.Success, "Expected Success result")
        val user = (currentResult as AddUserResult.Success).user
        assertFalse(user.hasCreditLimit, "User should not have credit limit")
    }

    fun thenDatabaseSaveWasCalled(times: Int = 1) {
        assertEquals(times, userRepository.saveCallCount, "Expected $times save calls")
    }

    fun thenDatabaseFindWasNotCalled() {
        assertEquals(0, userRepository.findCallCount, "Expected no find calls to database")
    }

    fun reset() {
        userRepository.reset()
        clientRepository.reset()
        currentRequest = null
        currentResult = null
        currentUser = null
    }
}
