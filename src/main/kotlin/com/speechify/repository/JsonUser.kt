package com.speechify.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.speechify.domain.Client
import com.speechify.domain.ClientType
import com.speechify.domain.User
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JsonUser(
    private val dbFilePath: String = "src/main/kotlin/com/speechify/data/db.json",
    private val clientRepository: ClientRepository = JsonClient()
) : UserRepository {

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun findById(id: String): User? {
        return findAll().find { it.id == id }
    }

    override fun findByEmail(email: String): User? {
        return findAll().find { it.email == email }
    }

    override fun findAll(): List<User> {
        return try {
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) return emptyList()

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val users = root.get("users") as? ArrayNode ?: return emptyList()

            users.mapNotNull { node -> mapToUser(node as ObjectNode) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun save(user: User): Boolean {
        return try {
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
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun update(user: User): Boolean {
        return try {
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) return false

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val users = root.get("users") as? ArrayNode ?: return false

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
            }
            found
        } catch (e: Exception) {
            false
        }
    }

    override fun existsByEmail(email: String): Boolean {
        return findByEmail(email) != null
    }

    private fun mapToUser(node: ObjectNode): User? {
        return try {
            // The JSON stores client as an embedded object, not just clientId
            val clientNode = node.get("client") as? ObjectNode
            val client = if (clientNode != null) {
                val clientName = clientNode.get("name").asText()
                Client(
                    id = clientNode.get("id").asText(),
                    name = clientName,
                    type = ClientType.fromName(clientName)
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
