import kotlinx.serialization.Serializable
@Serializable
data class GraphEdge(val from: String, val to: String, val label: String? = null)

@Serializable
data class GraphData(
    val vertices: List<String>,
    val edges: List<GraphEdge>
)
