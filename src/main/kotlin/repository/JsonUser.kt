package repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import domain.Client
import domain.ClientType
import domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JsonUser(
    private val dbFilePath: String = "src/main/kotlin/data/db.json",
    private val clientRepository: ClientRepository = JsonClient()
) : UserRepository {

    private val logger = LoggerFactory.getLogger(JsonUser::class.java)

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override suspend fun findById(id: String): User? {
        return findAll().find { it.id == id }
    }

    override suspend fun findByEmail(email: String): User? {
        return findAll().find { it.email == email }
    }

    override suspend fun findAll(): List<User> = withContext(Dispatchers.IO) {
        try {
            logger.debug("Loading users from {}", dbFilePath)
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) {
                logger.warn("Database file not found: {}", dbFilePath)
                return@withContext emptyList()
            }

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val users = root.get("users") as? ArrayNode ?: return@withContext emptyList()

            val result = users.mapNotNull { node -> mapToUser(node as ObjectNode) }
            logger.debug("Loaded {} users from database", result.size)
            result
        } catch (e: Exception) {
            logger.error("Failed to load users from {}: {}", dbFilePath, e.message)
            emptyList()
        }
    }

    override suspend fun save(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.debug("Saving user: id={}, email={}", user.id, user.email)
            val dbFile = File(dbFilePath)
            val root = if (dbFile.exists()) {
                objectMapper.readTree(dbFile) as ObjectNode
            } else {
                objectMapper.createObjectNode()
            }

            val users = root.get("users") as? ArrayNode
                ?: objectMapper.createArrayNode().also { root.set<ArrayNode>("users", it) }

            val userNode = createUserNode(user)
            users.add(userNode)

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, root)
            logger.info("User saved successfully: id={}", user.id)
            true
        } catch (e: Exception) {
            logger.error("Failed to save user {}: {}", user.id, e.message)
            false
        }
    }

    override suspend fun update(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.debug("Updating user: id={}", user.id)
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) {
                logger.warn("Cannot update user {}: database file not found", user.id)
                return@withContext false
            }

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val users = root.get("users") as? ArrayNode ?: return@withContext false

            var found = false
            for (i in 0 until users.size()) {
                val node = users[i] as ObjectNode
                if (node.get("id").asText() == user.id) {
                    users.set(i, createUserNode(user))
                    found = true
                    break
                }
            }

            if (found) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, root)
                logger.info("User updated successfully: id={}", user.id)
            } else {
                logger.warn("User not found for update: id={}", user.id)
            }
            found
        } catch (e: Exception) {
            logger.error("Failed to update user {}: {}", user.id, e.message)
            false
        }
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return findByEmail(email) != null
    }

    private suspend fun mapToUser(node: ObjectNode): User? {
        return try {
            // The JSON stores client as an embedded object, not just clientId
            val clientNode = node.get("client") as? ObjectNode
            val client = if (clientNode != null) {
                Client(
                    id = clientNode.get("id").asText(),
                    name = clientNode.get("name").asText(),
                    type = ClientType.valueOf(clientNode.get("type").asText())
                )
            } else {
                // Fallback: try clientId field and lookup
                val clientId = node.get("clientId")?.asText() ?: return null
                clientRepository.findById(clientId) ?: return null
            }

            val dateOfBirthStr = node.get("dateOfBirth")?.asText()
            val dateOfBirth = dateOfBirthStr?.let {
                try {
                    // Handle ISO format with time component
                    LocalDate.parse(it.substringBefore("T"))
                } catch (e: Exception) {
                    null
                }
            }

            User(
                id = node.get("id").asText(),
                client = client,
                dateOfBirth = dateOfBirth,
                email = node.get("email").asText(),
                firstname = node.get("firstname").asText(),
                surname = node.get("surname").asText(),
                hasCreditLimit = node.get("hasCreditLimit")?.asBoolean() ?: true,
                creditLimit = node.get("creditLimit")?.asDouble() ?: 0.0
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun createUserNode(user: User): ObjectNode {
        return objectMapper.createObjectNode().apply {
            put("id", user.id)
            put("clientId", user.client.id)
            put("dateOfBirth", user.dateOfBirth?.format(DateTimeFormatter.ISO_LOCAL_DATE))
            put("email", user.email)
            put("firstname", user.firstname)
            put("surname", user.surname)
            put("hasCreditLimit", user.hasCreditLimit)
            put("creditLimit", user.creditLimit)
        }
    }
}
