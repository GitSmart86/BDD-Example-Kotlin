package repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import domain.Client
import domain.ClientType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class JsonClient(
    private val dbFilePath: String = "src/main/kotlin/data/db.json"
) : ClientRepository {

    private val objectMapper = ObjectMapper()

    override suspend fun findById(id: String): Client? = withContext(Dispatchers.IO) {
        try {
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) return@withContext null

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val clients = root.get("clients") as? ArrayNode ?: return@withContext null

            for (node in clients) {
                val clientNode = node as ObjectNode
                if (clientNode.get("id").asText() == id) {
                    return@withContext mapToClient(clientNode)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun findAll(): List<Client> = withContext(Dispatchers.IO) {
        try {
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) return@withContext emptyList()

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val clients = root.get("clients") as? ArrayNode ?: return@withContext emptyList()

            clients.map { node -> mapToClient(node as ObjectNode) }
        } catch (e: Exception) {
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
