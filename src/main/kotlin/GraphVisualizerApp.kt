import javafx.animation.PauseTransition
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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
        graphInputArea.text = "A -> B : 2\nB -> C:3\nC -> A"  // Default example
        graphInputArea.textProperty().addListener { _, _, _ ->
            updateDelay.stop()
            updateDelay.setOnFinished { updateGraph() }
            updateDelay.play()
        }

        // Vertex List Panel - logic for change
        vertexListView.items = FXCollections.observableArrayList()
        vertexListView.setCellFactory { _ ->
            object : ListCell<Pair<String, SimpleBooleanProperty>>() {
                private val checkBox = CheckBox()

                init {
                    // Listener to handle checkbox action manually
                    checkBox.setOnAction {
                        // Update the SimpleBooleanProperty when checkbox is toggled
                        item?.second?.set(checkBox.isSelected)
                        println("toggled ${checkBox.text} : ${checkBox.isSelected}")
                        updateGraph()
                    }
                }

                override fun updateItem(item: Pair<String, SimpleBooleanProperty>?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        text = null
                        graphic = null
                    } else {
                        // Set the checkbox text and bind its selection property to the SimpleBooleanProperty
                        checkBox.text = item.first
                        //checkBox.selectedProperty().unbind()  // Unbind before re-binding
                        checkBox.isSelected = item.second.get()  // Set initial value
                        checkBox.setOnAction {
                            item.second.set(checkBox.isSelected)  // Manually update the property
                            updateGraph()  // Redraw the graph only when toggled
                        }

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

        val vertices = edges.flatMap{
            it -> it.split("->", ":").take(2).map { it.trim() }}.toSet()

        Platform.runLater {
            val newVertices = vertices - vertexToggleMap.keys

            newVertices.forEach { vertexToggleMap[it] = vertexToggleMap.getOrDefault(it, SimpleBooleanProperty(true)) }

            // Remove any vertices that no longer exist
            val removedVertices = vertexToggleMap.keys - vertices
            removedVertices.forEach { vertexToggleMap.remove(it) }

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

                val parts = edge.split("->", ":").map { it.trim() }
                if (parts.size == 2 && parts[0] in enabledVertices && parts[1] in enabledVertices) {
                    append("${parts[0]} --> ${parts[1]}\n")
                }else if (parts.size == 3 && parts[0] in enabledVertices && parts[1] in enabledVertices) {
                    append("${parts[0]} --> ${parts[1]} : ${parts[2]}\n")
                }
                // Most likely not needed anymore
                /*
                else if (parts[0] !in enabledVertices && parts[1] in enabledVertices) {
                    append("rectangle ${parts[1]}\n")
                } else if (parts[0] in enabledVertices && parts[1] !in enabledVertices) {
                    append("rectangle ${parts[0]}\n")
                }
                 */

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

