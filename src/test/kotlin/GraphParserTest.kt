import GraphUtils.fromTextToGraphData
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GraphParserTest {
    @Test
    fun `parse edges correctly`() {
        val input = "A -> B : 2\nB -> C : 3\nC -> A"
        val expectedEdges = listOf("A -> B : 2", "B -> C : 3", "C -> A")
        val edges = input.lines().filter { it.contains("->") }.map { it.trim() }

        assertEquals(expectedEdges, edges)
    }
    @Test
    fun testExtractGraphData_emptyInput() {
        val input = ""
        val result = fromTextToGraphData(input)

        assertEquals(emptyList<GraphEdge>(), result.edges)
        assertEquals(emptyList<String>(), result.vertices)
    }


}