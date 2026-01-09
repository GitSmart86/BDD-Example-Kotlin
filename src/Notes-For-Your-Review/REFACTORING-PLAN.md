# Refactoring Plan: BDD/TDD Approach

## Overview

This plan transforms the codebase using **Behavior-Driven Development** principles, testing against interfaces rather than implementations. The goal is to create stable tests that survive refactoring while ensuring the system meets its behavioral requirements.

---

## Architecture Vision

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            TEST LAYER                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐           │
│  │ UserBehaviorTest │  │ CacheBehaviorTest│  │ ClientBehaviorTest│          │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘           │
│           │                     │                     │                      │
│           └─────────────────────┼─────────────────────┘                      │
│                                 ▼                                            │
│                    ┌────────────────────────┐                                │
│                    │    Test DSL Layer      │                                │
│                    │  (UserTestDSL, etc.)   │                                │
│                    └───────────┬────────────┘                                │
│                                │                                             │
└────────────────────────────────┼─────────────────────────────────────────────┘
                                 │
┌────────────────────────────────┼─────────────────────────────────────────────┐
│                         INTERFACE LAYER                                      │
├────────────────────────────────┼─────────────────────────────────────────────┤
│                                ▼                                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐           │
│  │ UserRepository   │  │ ClientRepository │  │   LRUCache<T>    │           │
│  │   (interface)    │  │   (interface)    │  │   (interface)    │           │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘           │
│                                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                                 │
│  │  UserService     │  │  CreditPolicy    │  (Business Logic Interfaces)    │
│  │   (interface)    │  │   (interface)    │                                 │
│  └──────────────────┘  └──────────────────┘                                 │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                 │
┌────────────────────────────────┼─────────────────────────────────────────────┐
│                      IMPLEMENTATION LAYER                                    │
├────────────────────────────────┼─────────────────────────────────────────────┤
│                                ▼                                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐           │
│  │JsonUserRepository│  │JsonClientRepo    │  │ LRUCacheImpl<T>  │           │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘           │
│                                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                                 │
│  │CachedUserRepo    │  │DefaultCreditPol. │  (Decorators & Implementations) │
│  │  (decorator)     │  │                  │                                 │
│  └──────────────────┘  └──────────────────┘                                 │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │       db.json          │
                    │   (Data Storage)       │
                    └────────────────────────┘
```

---

## Phase 1: Define Interfaces (Contracts)

### 1.1 Domain Models (Immutable)

**File:** `src/main/kotlin/com/speechify/domain/Models.kt`

```kotlin
// Immutable data classes
data class User(
    val id: String,
    val client: Client,
    val dateOfBirth: LocalDate?,
    val email: String,
    val firstname: String,
    val surname: String,
    val hasCreditLimit: Boolean,
    val creditLimit: Double
)

data class Client(
    val id: String,
    val name: String,
    val type: ClientType
)

enum class ClientType {
    VERY_IMPORTANT,
    IMPORTANT,
    REGULAR;

    companion object {
        fun fromName(name: String): ClientType = when (name) {
            "VeryImportantClient" -> VERY_IMPORTANT
            "ImportantClient" -> IMPORTANT
            else -> REGULAR
        }
    }
}
```

### 1.2 Repository Interfaces

**File:** `src/main/kotlin/com/speechify/repository/Repositories.kt`

```kotlin
interface UserRepository {
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun save(user: User): Boolean
    fun update(user: User): Boolean
    fun existsByEmail(email: String): Boolean
}

interface ClientRepository {
    fun findById(id: String): Client?
    fun findAll(): List<Client>
}
```

### 1.3 Service Interfaces

**File:** `src/main/kotlin/com/speechify/service/Services.kt`

```kotlin
interface UserService {
    fun addUser(request: AddUserRequest): AddUserResult
    fun updateUser(user: User): Boolean
    fun getUserByEmail(email: String): User?
    fun getAllUsers(): List<User>
}

data class AddUserRequest(
    val firstname: String,
    val surname: String,
    val email: String,
    val dateOfBirth: LocalDate?,
    val clientId: String
)

sealed class AddUserResult {
    data class Success(val user: User) : AddUserResult()
    data class ValidationError(val reason: String) : AddUserResult()
    object ClientNotFound : AddUserResult()
    object DuplicateEmail : AddUserResult()
}
```

### 1.4 Credit Policy Interface

**File:** `src/main/kotlin/com/speechify/policy/CreditPolicy.kt`

```kotlin
interface CreditPolicy {
    fun calculateCreditLimit(client: Client): CreditLimit
}

data class CreditLimit(
    val hasLimit: Boolean,
    val amount: Double
)
```

### 1.5 Cache Interface (Already Exists)

**File:** `src/main/kotlin/com/speechify/LRUCache.kt` (existing)

```kotlin
interface LRUCache<T> {
    fun get(key: String): T?
    fun set(key: String, value: T)
}
```

---

## Phase 2: Test DSL Design

### 2.1 User Test DSL

**File:** `src/test/kotlin/dsl/UserTestDSL.kt`

```kotlin
class UserTestDSL {
    private var currentUser: User? = null
    private var currentRequest: AddUserRequest? = null
    private var result: AddUserResult? = null
    private val driver: UserTestDriver = InMemoryUserTestDriver()

    // GIVEN
    fun givenClient(id: String, name: String, type: ClientType)
    fun givenExistingUser(email: String, firstname: String)
    fun givenCacheWithCapacity(size: Int)

    // WHEN
    fun whenAddingUser(request: AddUserRequest)
    fun whenUpdatingUser(user: User)
    fun whenFetchingUserByEmail(email: String)
    fun whenFetchingAllUsers()

    // THEN
    fun thenUserIsCreated()
    fun thenUserIsRejected(reason: String)
    fun thenCreditLimitIs(expected: Double)
    fun thenHasNoCreditLimit()
    fun thenUserIsInCache()
    fun thenDatabaseWasNotCalled()
}
```

### 2.2 Cache Test DSL

**File:** `src/test/kotlin/dsl/CacheTestDSL.kt`

```kotlin
class CacheTestDSL<T> {
    private val driver: CacheTestDriver<T>

    // GIVEN
    fun givenCacheWithCapacity(size: Int)
    fun givenCachedItem(key: String, value: T)
    fun givenCacheIsFull()

    // WHEN
    fun whenItemIsStored(key: String, value: T)
    fun whenItemIsAccessed(key: String)
    fun whenNewItemExceedsCapacity(key: String, value: T)

    // THEN
    fun thenItemCanBeRetrieved(key: String, expected: T)
    fun thenItemIsEvicted(key: String)
    fun thenCacheContainsOnly(vararg keys: String)
    fun thenMostRecentlyUsedIs(key: String)
}
```

---

## Phase 3: Test Cases Specification

### 3.1 User Validation Tests

**File:** `src/test/kotlin/behavior/UserValidationBehaviorTest.kt`

| Test Case | Given | When | Then |
|-----------|-------|------|------|
| `rejects user under 21` | User born < 21 years ago | Adding user | ValidationError("under 21") |
| `accepts user exactly 21` | User born exactly 21 years ago | Adding user | Success |
| `accepts user over 21` | User born > 21 years ago | Adding user | Success |
| `rejects duplicate email` | User with email exists | Adding user with same email | DuplicateEmail |
| `rejects empty firstname` | Request with empty firstname | Adding user | ValidationError |
| `rejects empty surname` | Request with empty surname | Adding user | ValidationError |
| `rejects empty email` | Request with empty email | Adding user | ValidationError |
| `rejects invalid client` | No client with given ID | Adding user | ClientNotFound |

### 3.2 Credit Limit Tests

**File:** `src/test/kotlin/behavior/CreditLimitBehaviorTest.kt`

| Test Case | Given | When | Then |
|-----------|-------|------|------|
| `VeryImportantClient has no limit` | Client type = VERY_IMPORTANT | Adding user | hasCreditLimit = false |
| `ImportantClient gets 20000` | Client type = IMPORTANT | Adding user | creditLimit = 20000 |
| `Regular client gets 10000` | Client type = REGULAR | Adding user | creditLimit = 10000 |

### 3.3 Cache Behavior Tests

**File:** `src/test/kotlin/behavior/CacheBehaviorTest.kt`

| Test Case | Given | When | Then |
|-----------|-------|------|------|
| `stores and retrieves` | Empty cache | Store item | Item retrievable |
| `returns null for missing` | Empty cache | Get non-existent | Returns null |
| `evicts LRU at capacity` | Cache full (A, B), A accessed | Store C | B evicted, A and C remain |
| `update refreshes access` | Cache with item | Update same key | No eviction, value updated |
| `get refreshes access` | Cache full | Get oldest item | Item not evicted next |

### 3.4 Cache Integration Tests

**File:** `src/test/kotlin/behavior/CacheIntegrationBehaviorTest.kt`

| Test Case | Given | When | Then |
|-----------|-------|------|------|
| `cache miss fetches from DB` | User in DB, not in cache | Get user | DB called, user cached |
| `cache hit skips DB` | User in cache | Get user | DB not called |
| `save updates cache` | User saved | Get user | Served from cache |
| `update propagates to cache` | User in cache | Update user | Cache has new value |

### 3.5 Client Repository Tests

**File:** `src/test/kotlin/behavior/ClientRepositoryBehaviorTest.kt`

| Test Case | Given | When | Then |
|-----------|-------|------|------|
| `finds client by ID` | Client exists | Find by ID | Returns client |
| `returns null for missing` | Client doesn't exist | Find by ID | Returns null |
| `returns all clients` | Multiple clients | Find all | Returns all |

---

## Phase 4: Implementation Tasks

### Task 4.1: LRU Cache Implementation

**File:** `src/main/kotlin/com/speechify/cache/LRUCacheImpl.kt`

```kotlin
class LRUCacheImpl<T>(private val capacity: Int) : LRUCache<T> {
    private val cache = LinkedHashMap<String, T>(capacity, 0.75f, true)

    override fun get(key: String): T? = cache[key]

    override fun set(key: String, value: T) {
        cache[key] = value
        if (cache.size > capacity) {
            val oldest = cache.keys.first()
            cache.remove(oldest)
        }
    }
}
```

### Task 4.2: Credit Policy Implementation

**File:** `src/main/kotlin/com/speechify/policy/DefaultCreditPolicy.kt`

```kotlin
class DefaultCreditPolicy : CreditPolicy {
    override fun calculateCreditLimit(client: Client): CreditLimit {
        return when (client.type) {
            ClientType.VERY_IMPORTANT -> CreditLimit(hasLimit = false, amount = 0.0)
            ClientType.IMPORTANT -> CreditLimit(hasLimit = true, amount = 20_000.0)
            ClientType.REGULAR -> CreditLimit(hasLimit = true, amount = 10_000.0)
        }
    }
}
```

### Task 4.3: JSON Repository Implementations

**File:** `src/main/kotlin/com/speechify/repository/JsonUserRepository.kt`
**File:** `src/main/kotlin/com/speechify/repository/JsonClientRepository.kt`

- Extract JSON parsing logic
- Implement repository interfaces
- Handle errors properly (no silent swallowing)

### Task 4.4: Cached Repository Decorator

**File:** `src/main/kotlin/com/speechify/repository/CachedUserRepository.kt`

```kotlin
class CachedUserRepository(
    private val delegate: UserRepository,
    private val cache: LRUCache<User>
) : UserRepository {

    override fun findById(id: String): User? {
        return cache.get(id) ?: delegate.findById(id)?.also {
            cache.set(id, it)
        }
    }

    override fun save(user: User): Boolean {
        val result = delegate.save(user)
        if (result) cache.set(user.id, user)
        return result
    }

    // ... other methods with cache-aside pattern
}
```

### Task 4.5: User Service Implementation

**File:** `src/main/kotlin/com/speechify/service/DefaultUserService.kt`

```kotlin
class DefaultUserService(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val creditPolicy: CreditPolicy,
    private val validator: UserValidator
) : UserService {

    override fun addUser(request: AddUserRequest): AddUserResult {
        // 1. Validate request
        val validationResult = validator.validate(request)
        if (validationResult != null) return validationResult

        // 2. Check email uniqueness
        if (userRepository.existsByEmail(request.email)) {
            return AddUserResult.DuplicateEmail
        }

        // 3. Find client
        val client = clientRepository.findById(request.clientId)
            ?: return AddUserResult.ClientNotFound

        // 4. Calculate credit limit
        val creditLimit = creditPolicy.calculateCreditLimit(client)

        // 5. Create and save user
        val user = User(
            id = generateId(),
            client = client,
            dateOfBirth = request.dateOfBirth,
            email = request.email,
            firstname = request.firstname,
            surname = request.surname,
            hasCreditLimit = creditLimit.hasLimit,
            creditLimit = creditLimit.amount
        )

        userRepository.save(user)
        return AddUserResult.Success(user)
    }
}
```

---

## Phase 5: Test Drivers

### 5.1 In-Memory Test Driver

**File:** `src/test/kotlin/drivers/InMemoryUserTestDriver.kt`

```kotlin
class InMemoryUserTestDriver : UserTestDriver {
    private val users = mutableMapOf<String, User>()
    private val clients = mutableMapOf<String, Client>()
    private var dbCallCount = 0

    override fun storeClient(client: Client) { clients[client.id] = client }
    override fun storeUser(user: User) { users[user.id] = user }
    override fun getUser(id: String): User? { dbCallCount++; return users[id] }
    override fun getDatabaseCallCount(): Int = dbCallCount
    override fun reset() { users.clear(); clients.clear(); dbCallCount = 0 }
}
```

### 5.2 Test Fixtures

**File:** `src/test/kotlin/fixtures/TestFixtures.kt`

```kotlin
object TestFixtures {
    fun aUser(
        id: String = UUID.randomUUID().toString(),
        firstname: String = "John",
        surname: String = "Doe",
        email: String = "john@example.com",
        clientType: ClientType = ClientType.REGULAR,
        age: Int = 25
    ): User { /* ... */ }

    fun aClient(
        id: String = UUID.randomUUID().toString(),
        name: String = "TestClient",
        type: ClientType = ClientType.REGULAR
    ): Client { /* ... */ }

    fun anAddUserRequest(/* ... */): AddUserRequest { /* ... */ }
}
```

---

## Execution Order

### Sub-Agent Assignments

| Agent | Task | Files | Dependencies |
|-------|------|-------|--------------|
| **Agent 1** | Define interfaces & models | `domain/Models.kt`, `repository/Repositories.kt`, `service/Services.kt`, `policy/CreditPolicy.kt` | None |
| **Agent 2** | Implement Test DSL | `test/dsl/UserTestDSL.kt`, `test/dsl/CacheTestDSL.kt` | Agent 1 |
| **Agent 3** | Write behavior tests | `test/behavior/*.kt` | Agent 1, Agent 2 |
| **Agent 4** | Implement LRU Cache | `cache/LRUCacheImpl.kt`, update `LRUCacheProvider.kt` | Agent 1 |
| **Agent 5** | Implement repositories | `repository/JsonUserRepository.kt`, `repository/JsonClientRepository.kt`, `repository/CachedUserRepository.kt` | Agent 1, Agent 4 |
| **Agent 6** | Implement services | `service/DefaultUserService.kt`, `policy/DefaultCreditPolicy.kt`, `service/UserValidator.kt` | Agent 1, Agent 5 |

### Dependency Graph

```
Agent 1 (Interfaces)
    │
    ├──────────┬──────────┐
    ▼          ▼          ▼
Agent 2    Agent 4    Agent 3 (partial)
(DSL)      (Cache)    (Tests - can write, won't pass)
    │          │
    ▼          ▼
Agent 3    Agent 5
(Tests)    (Repos)
               │
               ▼
           Agent 6
           (Services)
               │
               ▼
         All Tests Pass
```

---

## File Structure After Refactoring

```
src/
├── main/kotlin/com/speechify/
│   ├── domain/
│   │   └── Models.kt              # User, Client, ClientType
│   ├── repository/
│   │   ├── Repositories.kt        # Interfaces
│   │   ├── JsonUserRepository.kt  # Implementation
│   │   ├── JsonClientRepository.kt
│   │   └── CachedUserRepository.kt # Decorator
│   ├── service/
│   │   ├── Services.kt            # Interfaces
│   │   ├── DefaultUserService.kt  # Implementation
│   │   └── UserValidator.kt       # Validation logic
│   ├── policy/
│   │   ├── CreditPolicy.kt        # Interface
│   │   └── DefaultCreditPolicy.kt # Implementation
│   ├── cache/
│   │   ├── LRUCache.kt            # Interface (existing)
│   │   ├── LRUCacheImpl.kt        # Implementation
│   │   └── LRUCacheProvider.kt    # Factory (updated)
│   └── CacheLimits.kt             # Config (existing)
│
└── test/kotlin/
    ├── dsl/
    │   ├── UserTestDSL.kt
    │   └── CacheTestDSL.kt
    ├── drivers/
    │   └── InMemoryUserTestDriver.kt
    ├── fixtures/
    │   └── TestFixtures.kt
    ├── behavior/
    │   ├── UserValidationBehaviorTest.kt
    │   ├── CreditLimitBehaviorTest.kt
    │   ├── CacheBehaviorTest.kt
    │   ├── CacheIntegrationBehaviorTest.kt
    │   └── ClientRepositoryBehaviorTest.kt
    ├── LruCacheTest.kt            # Existing (keep)
    ├── BDD-principles.md          # Reference doc
    └── REFACTORING-PLAN.md        # This file
```

---

## Success Criteria

1. **All existing LRU cache tests pass**
2. **All new behavior tests pass**
3. **No direct dependencies on implementations in tests** (only interfaces/DSL)
4. **Cache integration reduces "database" calls** (verifiable via test driver)
5. **Code follows SOLID principles:**
   - S: Each class has single responsibility
   - O: New credit policies can be added without modifying existing code
   - L: All repository implementations are substitutable
   - I: Interfaces are focused and minimal
   - D: High-level modules depend on abstractions

---

## Notes for Implementation

1. **Keep existing behavior** - The refactored code must produce identical results
2. **No external libraries** - Per README constraints
3. **Synchronous first** - Remove fake async (CompletableFuture) unless truly needed
4. **Immutable models** - Use `val` and `data class` throughout
5. **Fail fast** - Validation errors should be explicit, not silent nulls
