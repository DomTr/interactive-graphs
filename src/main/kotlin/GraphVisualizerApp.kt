import buttons.*
import javafx.animation.PauseTransition
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.Group
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
import javafx.scene.control.ButtonType


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
        graphInputArea.text = "A -> B : 2\nB -> C:3\nC -> A\n"  // Default example
        graphInputArea.textProperty().addListener { _, _, _ ->
            updateDelay.stop()
            updateDelay.setOnFinished { updateGraph() }
            updateDelay.play()
        }

        // Vertex List Panel change logic
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
                        checkBox.isSelected = item.second.get()  // Initial value is set
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
        val graphLoader = GraphLoaderTXT(primaryStage, ::updateGraph, ::showAlert, ::changeGraphInputArea)
        val graphLoaderJSON = GraphLoaderJSON(primaryStage, ::updateGraph, ::showAlert, ::changeGraphInputArea)
        val graphLoaderML = GraphLoaderGraphML(primaryStage, ::updateGraph, ::showAlert, ::changeGraphInputArea)
        val graphSaverPNG = GraphSaverPNG(primaryStage, ::showAlert, ::getImage,)
        val graphSaverJSON = GraphSaverJSON(primaryStage, ::showAlert, ::getGraphInputAreaText)
        val graphSaverGraphML = GraphSaverGraphML(primaryStage, ::showAlert, ::getGraphInputAreaText)
        val graphSaverTXT = GraphSaverTXT(primaryStage, ::showAlert, ::getGraphInputAreaText)


        val loadLargeGraph = graphLoader.createLoadGraphButton("Load large graph", "exampleGraphs/largeGraph.txt")
        val loadMediumGraph = graphLoader.createLoadGraphButton("Load medium graph", "exampleGraphs/mediumGraph.txt")
        val loadGraphFromFile = graphLoader.createLoadGraphButton("Load graph from file", "")
        val loadGraphFromJSON = graphLoaderJSON.createLoadGraphButton("Load graph from JSON", "")
        val loadGraphFromGraphML = graphLoaderML.createLoadGraphButton("Load graph from graphML", "")

        val saveGraphPNG = graphSaverPNG.createSaveGraphButton("Save graph as PNG")
        val saveGraphJSON = graphSaverJSON.createSaveGraphButton("Save graph as JSON")
        val saveGraphGraphML = graphSaverGraphML.createSaveGraphButton("Save graph in graphML")
        val saveGraphTXT = graphSaverTXT.createSaveGraphButton("Save graph in TXT")

        val exampleButtons = VBox(5.0, loadLargeGraph, loadMediumGraph)
        val fileButtons = VBox(5.0, loadGraphFromFile, saveGraphTXT, saveGraphPNG)
        val jsonButtons = VBox(5.0, loadGraphFromJSON, saveGraphJSON)
        val graphMLButtons = VBox(5.0, loadGraphFromGraphML, saveGraphGraphML)

        val examplePane = TitledPane("Examples", exampleButtons)
        val filePane = TitledPane("File Operations", fileButtons)
        val jsonPane = TitledPane("JSON", jsonButtons)
        val graphMLPane = TitledPane("GraphML", graphMLButtons)

        val buttonAccordion = Accordion(examplePane, filePane, jsonPane, graphMLPane)
        buttonAccordion.expandedPane = examplePane // Optional: expand by default
        buttonAccordion.prefWidth = 200.0

        val inputSection = VBox(5.0, inputLabel, graphInputArea)
        inputSection.prefWidth = 400.0

        val topBox = HBox(20.0, inputSection, buttonAccordion)
        topBox.padding = Insets(10.0)

        val graphPane = StackPane()
        val scrollPane = ScrollPane()
        scrollPane.setPrefSize(1000.0, 1000.0)
        val imageGroup = Group()
        imageGroup.children.add(graphImageView)
        graphImageViewSetUp(imageGroup, scrollPane)
        scrollPane.content = imageGroup
        graphPane.children.add(scrollPane)

        val root = BorderPane().apply {
            top = topBox
            left = leftPane
            center = scrollPane
        }

        primaryStage.apply {
            title = "Graph Visualizer"
            scene = Scene(root, 1000.0, 1000.0)
            show()
        }
        updateGraph() // Generate initial graph
    }

    private fun graphImageViewSetUp(imageGroup: Group, scrollPane: ScrollPane) {
        graphImageView.fitWidth = 700.0
        graphImageView.fitHeight = 500.0
        graphImageView.isPreserveRatio = true

        graphImageView.setOnZoom { event ->
            val zoomFactor = event.zoomFactor
            graphImageView.fitWidth *= zoomFactor
            graphImageView.fitHeight *= zoomFactor
        }

        // Panning functionality using mouse drag
        val initialX = arrayOf(0.0)
        val initialY = arrayOf(0.0)
        val initialHValue = arrayOf(0.0)
        val initialVValue = arrayOf(0.0)

        imageGroup.setOnMousePressed { event ->
            initialX[0] = event.sceneX
            initialY[0] = event.sceneY
            initialHValue[0] = scrollPane.hvalue
            initialVValue[0] = scrollPane.vvalue
        }

        imageGroup.setOnMouseDragged { event ->
            val deltaX = (event.sceneX - initialX[0]) / graphImageView.fitWidth
            val deltaY = (event.sceneY - initialY[0]) / graphImageView.fitHeight

            scrollPane.hvalue = (initialHValue[0] - deltaX).coerceIn(0.0, 1.0)
            scrollPane.vvalue = (initialVValue[0] - deltaY).coerceIn(0.0, 1.0)
        }
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
    }

    private fun getImage(): Image{
        return graphImageView.image
    }
    private fun getGraphInputAreaText(): String{
        return graphInputArea.text
    }

    private fun changeGraphInputArea(graph: String) {
        graphInputArea.text = graph
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

            // Remove any vertices that are disabled
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
            if (edges.size < 20 && enabledVertices.size < 20) {
                System.setProperty("PLANTUML_LIMIT_SIZE", "4096")  // 4096 is default
            } else if (edges.size < 100) {
                System.setProperty("PLANTUML_LIMIT_SIZE", "8192")  // 8192 is for medium graphs
            }
            else { // can be adjusted as needed
                System.setProperty("PLANTUML_LIMIT_SIZE", "16384")

            }
            enabledVertices.forEach { append("rectangle \"$it\"\n") }
            edges.forEach { edge ->

                val parts = edge.split("->", ":").map { it.trim() }
                if (parts.size == 2 && parts[0] in enabledVertices && parts[1] in enabledVertices) {
                    append("${parts[0]} --> ${parts[1]}\n")
                }else if (parts.size == 3 && parts[0] in enabledVertices && parts[1] in enabledVertices) {
                    append("${parts[0]} --> ${parts[1]} : ${parts[2]}\n")
                }
            }
            append("@enduml\n")
        }
        try {
            val reader = SourceStringReader(plantUmlCode)
            val diagramDescription = reader.generateDiagramDescription()

            if (diagramDescription.description.contains("Syntax Error")) {
                throw IllegalArgumentException("Invalid PlantUML syntax detected!")
            }

            val outputFile = File.createTempFile("graph_", ".png")
            FileOutputStream(outputFile).use { outputStream ->
                reader.outputImage(outputStream, FileFormatOption(FileFormat.PNG))
            }
            val image = Image(FileInputStream(outputFile))
            Platform.runLater {
                graphImageView.image = image
                graphImageView.fitWidth = image.width.coerceAtMost(700.0)
                graphImageView.fitHeight = image.height.coerceAtMost(500.0)

                val parentPane = graphImageView.parent as? Region
                parentPane?.minWidth = graphImageView.fitWidth
                parentPane?.minHeight = graphImageView.fitHeight
            }
        } catch (e: Exception) {
            showAlert("Error", "Invalid graph input\n${e.message}", AlertType.ERROR)
        }
    }
    /** Error handling **/
    private fun showAlert(title: String, message: String, alertType: AlertType) {
        Platform.runLater {
            var alert = Alert(Alert.AlertType.ERROR, message, ButtonType.OK)
            if (alertType == AlertType.SUCCESS) {
                alert = Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK)
            }
            graphImageView.image = null // Remove previous image
            alert.title = title
            alert.headerText = title // Show title in dialog header
            alert.showAndWait()
        }
    }
}

fun main() {
    Application.launch(GraphVisualizerApp::class.java)
}

// May be not needed anymore
fun readGraph(fileName: String) :String {
    val graphString = File(fileName).bufferedReader().readLines()
    val edges = graphString.filter { it.contains("->") }.map { it.trim() }
    val vertices = edges.flatMap{
            it -> it.split("->", ":").take(2).map { it.trim() }}.toSet()

    val plantUmlCode = buildString {
        append("@startuml\n")
        vertices.forEach { append("rectangle \"$it\"\n") }
        edges.forEach { edge ->

            val parts = edge.split("->", ":").map { it.trim() }
            if (parts.size == 2) {
                append("${parts[0]} --> ${parts[1]}\n")
            } else if (parts.size == 3) {
                append("${parts[0]} --> ${parts[1]} : ${parts[2]}\n")
            }
        }
        append("@enduml\n")
    }
    return plantUmlCode
}
