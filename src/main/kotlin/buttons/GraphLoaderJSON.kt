package buttons

import AlertType
import GraphData
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import java.io.File

private val jsonFormatter = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

class GraphLoaderJSON (
    primaryStage: Stage,
    updateGraph: () -> Unit,
    showAlert: (String, String, AlertType) -> Unit,
    changeGraphInputArea: (String) -> Unit
) : GraphLoader(primaryStage, updateGraph, showAlert, changeGraphInputArea) {
    override fun createLoadGraphButton(buttonName: String, fileName:String ): Button {
        val loadGraph = Button(buttonName)
        loadGraph.setOnAction { handleLoadGraph() }
        loadGraph.setOnMouseEntered { loadGraph.style = "-fx-background-color: lightblue;" }
        loadGraph.setOnMouseExited { loadGraph.style = "" }
        loadGraph.setOnMouseClicked { loadGraph.style = "-fx-border-color: blue" }
        return loadGraph
    }
    private fun handleLoadGraph() { // handleLoadGraphFromFile
        val fileChooser = FileChooser()
        val initialDirectory = File(System.getProperty("user.home"))
        fileChooser.initialDirectory = initialDirectory
        fileChooser.title = "Select JSON file of graph to be displayed"
        val ex1 = FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
        fileChooser.extensionFilters.addAll(ex1)
        val selectedFile = fileChooser.showOpenDialog(primaryStage)

        if (selectedFile != null) {
            println("Open file")
            try {
                val jsonContent = selectedFile.readText()
                val graphData = jsonFormatter.decodeFromString<GraphData>(jsonContent)

                // Converting back to plain text edge list format
                val edgeLines = graphData.edges.map {edge ->
                    if (edge.label != null)
                        "${edge.from} -> ${edge.to} : ${edge.label}"
                    else
                        "${edge.from} -> ${edge.to}"
                }
                changeGraphInputArea(edgeLines.joinToString("\n"))
                updateGraph()
                println(edgeLines.joinToString("\n"))
            } catch (e: Exception){
                showAlert("Error", "Failed to load JSON: ${e.message}", AlertType.ERROR)
            }
            println(selectedFile.path)
        } else {
            showAlert("Error", "No file selected", AlertType.ERROR)
        }
    }
}