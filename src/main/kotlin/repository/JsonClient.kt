package repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import domain.Client
import domain.ClientType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File

/**
 * JSON file-based implementation of [ClientRepository].
 *
 * Reads client data from a JSON file with non-blocking I/O.
 *
 * @param dbFilePath Path to the JSON database file
 */
class JsonClient(
    private val dbFilePath: String = "src/main/kotlin/data/db.json"
) : ClientRepository {

    private val logger = LoggerFactory.getLogger(JsonClient::class.java)
    private val objectMapper = ObjectMapper()

    override suspend fun findById(id: String): Client? = withContext(Dispatchers.IO) {
        try {
            logger.debug("Looking up client: id={}", id)
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) {
                logger.warn("Database file not found: {}", dbFilePath)
                return@withContext null
            }

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val clients = root.get("clients") as? ArrayNode ?: return@withContext null

            for (node in clients) {
                val clientNode = node as ObjectNode
                if (clientNode.get("id").asText() == id) {
                    logger.debug("Client found: id={}", id)
                    return@withContext mapToClient(clientNode)
                }
            }
            logger.debug("Client not found: id={}", id)
            null
        } catch (e: Exception) {
            logger.error("Failed to find client {}: {}", id, e.message)
            null
        }
    }

    override suspend fun findAll(): List<Client> = withContext(Dispatchers.IO) {
        try {
            logger.debug("Loading all clients from {}", dbFilePath)
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) {
                logger.warn("Database file not found: {}", dbFilePath)
                return@withContext emptyList()
            }

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val clients = root.get("clients") as? ArrayNode ?: return@withContext emptyList()

            val result = clients.map { node -> mapToClient(node as ObjectNode) }
            logger.debug("Loaded {} clients", result.size)
            result
        } catch (e: Exception) {
            logger.error("Failed to load clients from {}: {}", dbFilePath, e.message)
            emptyList()
        }
    }

    private fun mapToClient(node: ObjectNode): Client {
        return Client(
            id = node.get("id").asText(),
            name = node.get("name").asText(),
            type = ClientType.valueOf(node.get("type").asText())
        )
    }
}
