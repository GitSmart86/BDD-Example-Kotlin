package behavior

import domain.Client
import domain.ClientType
import drivers.InMemoryClientRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*

@DisplayName("Client Repository Behavior")
class ClientRepositoryBehaviorTest {

    private lateinit var repository: InMemoryClientRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryClientRepository()
    }

    @Test
    @DisplayName("finds client by ID")
    fun `finds client by ID`() {
        // Given
        val client = Client(id = "client-1", name = "Test Client", type = ClientType.REGULAR)
        repository.addClient(client)

        // When
        val found = repository.findById("client-1")

        // Then
        assertNotNull(found)
        assertEquals("client-1", found?.id)
        assertEquals("Test Client", found?.name)
        assertEquals(ClientType.REGULAR, found?.type)
    }

    @Test
    @DisplayName("returns null for non-existent client")
    fun `returns null for non-existent client`() {
        // Given - empty repository

        // When
        val found = repository.findById("non-existent")

        // Then
        assertNull(found)
    }

    @Test
    @DisplayName("returns all clients")
    fun `returns all clients`() {
        // Given
        repository.addClient(Client("c1", "Client 1", ClientType.REGULAR))
        repository.addClient(Client("c2", "Client 2", ClientType.IMPORTANT))
        repository.addClient(Client("c3", "Client 3", ClientType.VERY_IMPORTANT))

        // When
        val all = repository.findAll()

        // Then
        assertEquals(3, all.size)
        assertTrue(all.any { it.id == "c1" })
        assertTrue(all.any { it.id == "c2" })
        assertTrue(all.any { it.id == "c3" })
    }

    @Test
    @DisplayName("returns empty list when no clients exist")
    fun `returns empty list when no clients exist`() {
        // Given - empty repository

        // When
        val all = repository.findAll()

        // Then
        assertTrue(all.isEmpty())
    }

    @Test
    @DisplayName("tracks find call count")
    fun `tracks find call count`() {
        // Given
        repository.addClient(Client("c1", "Client 1", ClientType.REGULAR))

        // When
        repository.findById("c1")
        repository.findById("c1")
        repository.findAll()

        // Then
        assertEquals(3, repository.findCallCount)
    }
}
