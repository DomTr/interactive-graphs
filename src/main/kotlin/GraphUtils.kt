import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

private val jsonFormatter = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

object GraphUtils {
    // Copy-pasted from
    fun fromTextToGraphData(inputText: String): GraphData {
        val edgeLines = inputText.lines().filter { it.contains("->") }

        val edges = edgeLines.map { line ->
            val parts = line.split("->", ":").map { it.trim() }
            val from = parts[0]
            val to = parts[1]
            val label = if (parts.size == 3) parts[2] else null
            GraphEdge(from, to, label)
        }

        val vertices = edges.flatMap { listOf(it.from, it.to) }.toSet().toList()
        return GraphData(vertices, edges)
    }

    // From GraphLoaderJSON
    fun fromJSONToGraphData(file: File): GraphData {
        val jsonFormatter = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
        val jsonContent = file.readText()
        return jsonFormatter.decodeFromString<GraphData>(jsonContent) // decodeFromString is the main part of reading from JSON
    }

    // Copy-pasted from GraphLoaderGraphML
    fun fromGraphMLToGraphData(file: File): GraphData {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("node")
        val edgeList = doc.getElementsByTagName("edge")

        val vertices = mutableListOf<String>()
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i) as Element
            vertices.add(node.getAttribute("id"))
        }
        val edges = mutableListOf<GraphEdge>()
        for (i in 0 until edgeList.length) {
            val edge = edgeList.item(i) as Element
            val from = edge.getAttribute("source")
            val to = edge.getAttribute("target")

            val dataList = edge.getElementsByTagName("data")
            var label: String? = null
            for (j in 0 until dataList.length) {
                val data = dataList.item(j) as Element
                if (data.getAttribute("key") == "label") {
                    label = data.textContent.trim()
                    break
                }
            }
            edges.add(GraphEdge(from, to, label))
        }
        return GraphData(vertices, edges)
    }

    fun fromGraphMLToText(file: File): String {
        val graphData = fromGraphMLToGraphData(file)

        // Converting back to plain text edge list format
        val edgeLines = graphData.edges.map {edge ->
            if (edge.label != null)
                "${edge.from} -> ${edge.to} : ${edge.label}"
            else
                "${edge.from} -> ${edge.to}"
        }
        return edgeLines.joinToString("\n")
    }

    // From GraphLoaderJSON
    fun fromJSONToText(file: File): String {
        val jsonContent = file.readText()
        val graphData = jsonFormatter.decodeFromString<GraphData>(jsonContent)

        // Converting back to plain text edge list format
        val edgeLines = graphData.edges.map {edge ->
            if (edge.label != null)
                "${edge.from} -> ${edge.to} : ${edge.label}"
            else
                "${edge.from} -> ${edge.to}"
        }
        return edgeLines.joinToString("\n")
    }

}
