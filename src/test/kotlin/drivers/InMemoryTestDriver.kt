package drivers

import domain.Client
import domain.User
import repository.ClientRepository
import repository.UserRepository

class InMemoryUserRepository : UserRepository {
    private val users = mutableMapOf<String, User>()
    var saveCallCount = 0
        private set
    var findCallCount = 0
        private set

    override suspend fun findById(id: String): User? {
        findCallCount++
        return users[id]
    }

    override suspend fun findByEmail(email: String): User? {
        findCallCount++
        return users.values.find { it.email == email }
    }

    override suspend fun findAll(): List<User> {
        findCallCount++
        return users.values.toList()
    }

    override suspend fun save(user: User): Boolean {
        saveCallCount++
        users[user.id] = user
        return true
    }

    override suspend fun update(user: User): Boolean {
        saveCallCount++
        if (users.containsKey(user.id)) {
            users[user.id] = user
            return true
        }
        return false
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return users.values.any { it.email == email }
    }

    fun reset() {
        users.clear()
        saveCallCount = 0
        findCallCount = 0
    }

    fun addUser(user: User) {
        users[user.id] = user
    }
}

class InMemoryClientRepository : ClientRepository {
    private val clients = mutableMapOf<String, Client>()
    var findCallCount = 0
        private set

    override suspend fun findById(id: String): Client? {
        findCallCount++
        return clients[id]
    }

    override suspend fun findAll(): List<Client> {
        findCallCount++
        return clients.values.toList()
    }

    fun reset() {
        clients.clear()
        findCallCount = 0
    }

    fun addClient(client: Client) {
        clients[client.id] = client
    }
}
