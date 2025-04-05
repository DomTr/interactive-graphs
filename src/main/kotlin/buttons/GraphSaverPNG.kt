package buttons

import AlertType
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.scene.image.Image
import java.io.File
import javax.imageio.ImageIO
class GraphSaverPNG(
    primaryStage: Stage,
    showAlert: (String, String, AlertType) -> Unit,
    private val getImage:() -> Image,
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
        fileChooser.initialFileName = "graph.png"  // Initial name

        // Set file extension filter
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("PNG Image", "*.png"))

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
        val image = getImage()
        if (image != null) {
            val bufferedImage = SwingFXUtils.fromFXImage(image, null)
            ImageIO.write(bufferedImage, "png", file)
        } else {
            showAlert("Error", "No image to save.", AlertType.ERROR)
        }
    }
}