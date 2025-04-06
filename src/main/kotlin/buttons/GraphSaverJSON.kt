package buttons

import AlertType
import GraphData
import GraphEdge
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val jsonFormatter = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}
class GraphSaverJSON (
    primaryStage: Stage,
    showAlert: (String, String, AlertType) -> Unit,
    private val getGraphInputAreaText:() -> String
) : GraphSaver(primaryStage, showAlert){
    override fun createSaveGraphButton(buttonName: String): Button {
        val saveGraph = Button(buttonName)
        saveGraph.setOnAction { handleSaveGraph() }
        saveGraph.setOnMouseEntered { saveGraph.style = "-fx-background-color: lightblue;" }
        saveGraph.setOnMouseExited { saveGraph.style = "" }
        saveGraph.setOnMouseClicked { saveGraph.style = "-fx-border-color: blue" }
        saveGraph.prefWidth = 170.0
        return saveGraph
    }
    private fun handleSaveGraph() {
        val fileChooser = FileChooser()
        fileChooser.title = "Save Graph as JSON"
        fileChooser.initialFileName = "graph.json"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("JSON Files", "*.json"))

        val file = fileChooser.showSaveDialog(primaryStage)
        if (file != null) {
            val graphData = extractGraphData()
            val jsonText = jsonFormatter.encodeToString(graphData)
            file.writeText(jsonText)
        } else {
            showAlert("Cancelled", "No file selected for saving JSON.", AlertType.ERROR)
        }
    }
    private fun extractGraphData(): GraphData {
        val inputText = getGraphInputAreaText().trim()
        val edgeLines = inputText.lines().filter{ it.contains("->")}

        val edges = edgeLines.map {line ->
            val parts = line.split("->", ":").map{ it.trim() }
            val from = parts[0]
            val to = parts[1]
            val label = if (parts.size == 3) parts[2] else null
            GraphEdge(from, to, label)
        }
        val vertices = edges.flatMap{listOf(it.from, it.to)}.toSet().toList()
        return GraphData(vertices, edges)
    }
}