package repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import domain.Client
import domain.ClientType
import java.io.File

class JsonClient(
    private val dbFilePath: String = "src/main/kotlin/data/db.json"
) : ClientRepository {

    private val objectMapper = ObjectMapper()

    override fun findById(id: String): Client? {
        return try {
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) return null

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val clients = root.get("clients") as? ArrayNode ?: return null

            for (node in clients) {
                val clientNode = node as ObjectNode
                if (clientNode.get("id").asText() == id) {
                    return mapToClient(clientNode)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    override fun findAll(): List<Client> {
        return try {
            val dbFile = File(dbFilePath)
            if (!dbFile.exists()) return emptyList()

            val root = objectMapper.readTree(dbFile) as ObjectNode
            val clients = root.get("clients") as? ArrayNode ?: return emptyList()

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
