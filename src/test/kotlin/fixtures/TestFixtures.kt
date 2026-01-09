package fixtures

import com.speechify.domain.Client
import com.speechify.domain.ClientType
import com.speechify.domain.User
import com.speechify.service.AddUserRequest
import java.time.LocalDate
import java.util.UUID

object TestFixtures {

    fun aUser(
        id: String = UUID.randomUUID().toString(),
        firstname: String = "John",
        surname: String = "Doe",
        email: String = "john.doe@example.com",
        client: Client = aClient(),
        dateOfBirth: LocalDate = LocalDate.now().minusYears(25),
        hasCreditLimit: Boolean = true,
        creditLimit: Double = 10000.0
    ): User = User(
        id = id,
        firstname = firstname,
        surname = surname,
        email = email,
        client = client,
        dateOfBirth = dateOfBirth,
        hasCreditLimit = hasCreditLimit,
        creditLimit = creditLimit
    )

    fun aClient(
        id: String = UUID.randomUUID().toString(),
        name: String = "TestClient",
        type: ClientType = ClientType.REGULAR
    ): Client = Client(id = id, name = name, type = type)

    fun anAddUserRequest(
        firstname: String = "John",
        surname: String = "Doe",
        email: String = "john.doe@example.com",
        dateOfBirth: LocalDate? = LocalDate.now().minusYears(25),
        clientId: String = "client-1"
    ): AddUserRequest = AddUserRequest(
        firstname = firstname,
        surname = surname,
        email = email,
        dateOfBirth = dateOfBirth,
        clientId = clientId
    )

    fun userBornYearsAgo(years: Long): LocalDate = LocalDate.now().minusYears(years)

    fun userBornExactlyYearsAgo(years: Long): LocalDate = LocalDate.now().minusYears(years)
}
