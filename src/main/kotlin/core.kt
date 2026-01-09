/**
 * BDD Example - Kotlin
 *
 * Main entry point providing easy access to the core API.
 * This facade simplifies imports for consumers of the library.
 *
 * Usage:
 *   val service = BDD.createUserService()
 *   val result = service.addUser(request)
 */
object BDD {

    /**
     * Creates a fully configured UserService with:
     * - JSON file persistence (JsonUser, JsonClient)
     * - LRU caching layer (CachedUser)
     * - Default credit policy (UserCreditsDefault)
     */
    fun createUserService(
        cacheSize: Int = 100,
        dbPath: String = "src/main/kotlin/data/db.json",
        minimumAge: Int = 21
    ): service.UserService {
        val clientRepo = repository.JsonClient(dbPath)
        val jsonUserRepo = repository.JsonUser(dbPath, clientRepo)
        val cache = cache.Provider.createLRUCache<domain.User>(cache.Config(cacheSize))
        val cachedUserRepo = repository.CachedUser(jsonUserRepo, cache)
        val creditPolicy = policy.UserCreditsDefault()
        val validator = service.UserValidator(minimumAge)

        return service.UserDefault(
            userRepository = cachedUserRepo,
            clientRepository = clientRepo,
            creditPolicy = creditPolicy,
            validator = validator
        )
    }

    /**
     * Creates a simple in-memory cache for custom use.
     */
    fun <T> createCache(maxItems: Int = 100): cache.Interface<T> {
        return cache.Provider.createLRUCache(cache.Config(maxItems))
    }

    /**
     * Creates a request to add a new user.
     */
    fun addUserRequest(
        firstname: String,
        surname: String,
        email: String,
        dateOfBirth: java.time.LocalDate?,
        clientId: String
    ): service.AddUserRequest {
        return service.AddUserRequest(
            firstname = firstname,
            surname = surname,
            email = email,
            dateOfBirth = dateOfBirth,
            clientId = clientId
        )
    }
}

// Re-export key types for convenient access
typealias User = domain.User
typealias Client = domain.Client
typealias ClientType = domain.ClientType
typealias UserService = service.UserService
typealias AddUserRequest = service.AddUserRequest
typealias AddUserResult = service.AddUserResult
typealias CacheInterface<T> = cache.Interface<T>
