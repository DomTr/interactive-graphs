package buttons
import AlertType
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class GraphLoaderTXT (
    primaryStage: Stage,
    updateGraph: () -> Unit,
    showAlert: (String, String, AlertType) -> Unit,
    changeGraphInputArea: (String) -> Unit
): GraphLoader(primaryStage, updateGraph, showAlert, changeGraphInputArea){
    private lateinit var fileName: String
    override fun createLoadGraphButton(buttonName: String, fileName:String): Button {
        val loadGraph = Button(buttonName)
        this.fileName = fileName
        loadGraph.setOnAction { handleLoadGraph(fileName) }
        loadGraph.setOnMouseEntered { loadGraph.style = "-fx-background-color: lightblue;" }
        loadGraph.setOnMouseExited { loadGraph.style = "" }
        loadGraph.setOnMouseClicked { loadGraph.style = "-fx-border-color: blue" }
        loadGraph.prefWidth = 170.0
        return loadGraph
    }
    private fun handleLoadGraph(fileName:String) {
        println(fileName)
        if (fileName != "") {
            println(fileName)
            loadGraphFromFile(fileName)
        } else {
            handleLoadGraphFromFile()
        }
        updateGraph()
    }
    private fun loadGraphFromFile(fileName: String) {
        println(fileName)
        val graph = readGraphPrimitive(fileName)
        changeGraphInputArea(graph)
    }
    private fun handleLoadGraphFromFile() {
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.title = "Select file of graph to be displayed"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"))

        val selectedFile = fileChooser.showOpenDialog(primaryStage)
        if (selectedFile != null) {
            println("Open file")
            println(selectedFile.path)
            loadGraphFromFile(selectedFile.path)
            updateGraph()
        } else {
            showAlert("Error", "No file selected", AlertType.ERROR)
        }
    }
    private fun readGraphPrimitive(fileName: String): String {
        val graphString = File(fileName).bufferedReader().readLines()
        val edges = graphString.filter { it.contains("->") }.map { it.trim() }
        return buildString { edges.forEach { e -> append(e + "\n") } }
    }

}