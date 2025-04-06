import GraphUtils.fromGraphMLToGraphData
import GraphUtils.fromGraphMLToText
import GraphUtils.fromJSONToGraphData
import GraphUtils.fromTextToGraphData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GraphUtilsTest {

    @Test
    fun testExtractGraphData_withLabels() {
        val input = """
            a -> b : 10
            b -> c
        """.trimIndent()

        val expectedVertices = listOf("a", "b", "c")
        val expectedEdges = listOf(
            GraphEdge("a", "b", "10"),
            GraphEdge("b", "c", null)
        )

        val result = fromTextToGraphData(input)

        assertEquals(expectedVertices.sorted(), result.vertices.sorted())
        assertEquals(expectedEdges, result.edges)
    }
    @Test
    fun testExtractGraphDataJSON() {
        val fileName = "src/test/resources/testParseGraphJSON.txt"
        val file = File(fileName)
        val expectedVertices = listOf("A", "B", "C", "D", "G")
        val expectedEdges = listOf(
            GraphEdge("A", "B", "2"),
            GraphEdge("B", "C", "3"),
            GraphEdge("C", "D", "3"),
            GraphEdge("D", "A", "5"),
            GraphEdge("G", "G", null),
        )
        val result = fromJSONToGraphData(file)
        assertEquals(expectedVertices.sorted(), result.vertices.sorted())
        assertEquals(expectedEdges, result.edges)
    }

    @Test
    fun testExtractGraphDataGraphML() {
        val fileName = "src/test/resources/testParseGraphML.txt"
        val file = File(fileName)
        val expectedVertices = listOf("A", "B", "G", "H", "J")
        val expectedEdges = listOf(
            GraphEdge("A", "B", "2"),
            GraphEdge("G", "G", null),
            GraphEdge("H", "B", null),
            GraphEdge("H", "G", "19"),
            GraphEdge("J", "A", null),
        )
        val result = fromGraphMLToGraphData(file)
        assertEquals(expectedVertices.sorted(), result.vertices.sorted())
        assertEquals(expectedEdges, result.edges)
    }
    @Test
    fun `fromJSONToText converts JSON to plain text format correctly`(@TempDir tempDir: File) {
        // Sample GraphData JSON
        val json = """
            {
              "vertices": ["A", "B", "C"],
              "edges": [
                {"from": "A", "to": "B", "label": "10"},
                {"from": "B", "to": "C", "label": null}
              ]
            }
        """.trimIndent()

        val file = File(tempDir, "graph.json")
        file.writeText(json)

        val expected = """
            A -> B : 10
            B -> C
        """.trimIndent()

        val actual = GraphUtils.fromJSONToText(file)

        assertEquals(expected, actual)
    }
    @Test fun `fromGraphMLToText converts graphML to plain text format correctly`(@TempDir tempDir: File){
        val actual = fromGraphMLToText(File("src/test/resources/testParseGraphML.txt"))
        val file = File(tempDir, "graphML.txt")
        file.writeText(actual)
        val expected =
            """
            A -> B : 2
            G -> G
            H -> B
            H -> G : 19
            J -> A
        """.trimIndent()

        assertEquals(expected, actual)
    }


}
