import javafx.animation.PauseTransition
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Stage
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.time.Duration

class GraphVisualizerApp : Application() {
    private val graphInputArea = TextArea()
    private val vertexList: ObservableList<String> = FXCollections.observableArrayList()
    private val vertexToggleMap = mutableMapOf<String, SimpleBooleanProperty>()
    private val graphImageView = ImageView()
    private var updateTask: javafx.concurrent.Task<Unit>? = null
    private val vertexListView = ListView<Pair<String, SimpleBooleanProperty>>()

    private val updateDelay = PauseTransition(javafx.util.Duration(500.0))


    override fun start(primaryStage: Stage) {
        val inputLabel = Label("Graph Input (Edge List, e.g., A -> B):")
        graphInputArea.text = "A -> B\nB -> C\nC -> A"  // Default example
        graphInputArea.textProperty().addListener { _, _, _ ->
            updateDelay.stop()
            updateDelay.setOnFinished { updateGraph() }
            updateDelay.play()
        }

        // Vertex List Panel
        vertexListView.items = FXCollections.observableArrayList()
        vertexListView.setCellFactory { _ ->
            object : ListCell<Pair<String, SimpleBooleanProperty>>() {
                private val checkBox = CheckBox()

                override fun updateItem(item: Pair<String, SimpleBooleanProperty>?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        text = null
                        graphic = null
                    } else {
                        checkBox.text = item.first
                        checkBox.selectedProperty().bindBidirectional(item.second)
                        checkBox.setOnAction { updateGraph() }
                        graphic = checkBox
                    }
                }
            }
        }

        val leftPane = VBox(10.0, Label("Vertex List:"), vertexListView)
        leftPane.prefWidth = 200.0

        // Graph Display Area
        val graphPane = StackPane(graphImageView)
        val root = BorderPane().apply {
            top = VBox(5.0, inputLabel, graphInputArea)
            left = leftPane
            center = graphPane
        }

        primaryStage.apply {
            title = "Graph Visualizer"
            scene = Scene(root, 800.0, 600.0)
            show()
        }

        updateGraph() // Generate initial graph
    }

    /** Parses the input text and updates the vertex list & diagram **/
    private fun updateGraph() {
        val inputText = graphInputArea.text.trim()
        val edges = inputText.lines().filter { it.contains("->") }.map { it.trim() }

        val vertices = edges.flatMap { it.split("->").map { it.trim() } }.toSet()
        Platform.runLater {
            // Update the list of vertices if new ones are added
            val newVertices = vertices - vertexToggleMap.keys
            newVertices.forEach { vertexToggleMap[it] = SimpleBooleanProperty(true) }

            // Remove any vertices that no longer exist
            val removedVertices = vertexToggleMap.keys - vertices
            removedVertices.forEach { vertexToggleMap.remove(it) }

            // Update the ObservableList for UI
            vertexList.setAll(vertexToggleMap.keys)

            // Update vertexListView dynamically
            updateVertexListView()
        }
        drawGraph(edges)
    }
    private fun updateVertexListView() {
        val newItems = FXCollections.observableArrayList<Pair<String, SimpleBooleanProperty>>()
        vertexToggleMap.forEach {(vertex, property) ->
            newItems.add(vertex to property)
        }
        vertexListView.items = newItems
    }
    /** Generates the PlantUML diagram and updates the ImageView **/
    private fun drawGraph(edges: List<String>) {
        val enabledVertices = vertexToggleMap.filterValues { it.get() }.keys

        val plantUmlCode = buildString {
            append("@startuml\n")
            enabledVertices.forEach { append("rectangle \"$it\"\n") }
            edges.forEach { edge ->
                val parts = edge.split("->").map { it.trim() }
                if (parts.size == 2 && parts[0] in enabledVertices && parts[1] in enabledVertices) {
                    append("${parts[0]} --> ${parts[1]}\n")
                }
            }
            append("@enduml\n")
        }
        try {
            val reader = SourceStringReader(plantUmlCode)
            val diagramDescription = reader.generateDiagramDescription() // Ensure it's non-null

            if (diagramDescription.description.contains("Syntax Error")) { // Check for syntax error
                throw IllegalArgumentException("Invalid PlantUML syntax detected!")
            }

            val outputFile = File.createTempFile("graph_", ".png")
            FileOutputStream(outputFile).use { outputStream ->
                reader.outputImage(outputStream, FileFormatOption(FileFormat.PNG))
            }

            Platform.runLater {
                graphImageView.image = Image(FileInputStream(outputFile))
            }
        } catch (e: Exception) {
            showAlert("Error", "Invalid graph input\n${e.message}")
        }
    }
    private fun showAlert(title: String, message: String) {
        Platform.runLater {
            graphImageView.image = null // Remove previous image
            val alert = Alert(Alert.AlertType.ERROR, message, ButtonType.OK)
            alert.title = title
            alert.headerText = title // Show title in dialog header
            alert.showAndWait()
        }
    }
}

fun main() {
    Application.launch(GraphVisualizerApp::class.java)
}
