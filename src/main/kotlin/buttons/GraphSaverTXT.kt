package buttons

import AlertType
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class GraphSaverTXT(
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
        return saveGraph
    }
    private fun handleSaveGraph() {
        val fileChooser = FileChooser()
        fileChooser.title = "Save Graph As"
        fileChooser.initialFileName = "graph.txt"  // Initial name

        // Set file extension filter
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("TXT File", "*.txt"))

        val selectedFile = fileChooser.showSaveDialog(primaryStage)
        if (selectedFile != null) {
            println("Saving to: ${selectedFile.absolutePath}")// For debugging
            // Write the image to this file
            saveGraphToFile(selectedFile)
        } else {
            showAlert("Cancelled", "No file was selected.", AlertType.ERROR) // For debugging
        }
    }
    private fun saveGraphToFile(file: File) {
        file.writeText(getGraphInputAreaText())
    }
}