package buttons

import AlertType
import GraphData
import GraphEdge
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class GraphSaverGraphML (
    primaryStage: Stage,
    showAlert: (String, String, AlertType) -> Unit,
    private val getGraphInputAreaText:() -> String
): GraphSaver(primaryStage, showAlert){
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
        fileChooser.title = "Save Graph as JSON"
        fileChooser.initialFileName = "graph.txt"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("txt Files", "*.txt"))
        val file = fileChooser.showSaveDialog(primaryStage)
        if (file != null) {
            val graphData = extractGraphData()
            saveGraphAsGraphML(graphData, file)
        } else {
            showAlert("Cancelled", "No file selected for saving graphML.", AlertType.ERROR)
        }
    }
    private fun saveGraphAsGraphML(graphData: GraphData, file: File) {
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.newDocument()

        val root = doc.createElement("graphml")
        root.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns") // what does this do?
        doc.appendChild(root)

        // key for edge labels
        val key = doc.createElement("key")
        key.setAttribute("id", "label")
        key.setAttribute("for", "edge")
        key.setAttribute("attr.name", "label")
        key.setAttribute("attr.type", "string")
        root.appendChild(key)

        val graph = doc.createElement("graph")
        graph.setAttribute("id", "G")
        graph.setAttribute("edgedefault", "directed")
        root.appendChild(graph)

        // adding nodes
        for (vertex in graphData.vertices) {
            val node = doc.createElement("node")
            node.setAttribute("id", vertex)
            graph.appendChild(node)
        }
        // Add edges
        for ((i, edge) in graphData.edges.withIndex()) {
            val edgeElement = doc.createElement("edge")
            edgeElement.setAttribute("id", "e$i")
            edgeElement.setAttribute("source", edge.from)
            edgeElement.setAttribute("target", edge.to)

            // Supports edge-labeling
            edge.label?.let {
                val data = doc.createElement("data")
                data.setAttribute("key", "label")
                data.textContent = it
                edgeElement.appendChild(data)
            }
            graph.appendChild(edgeElement)
        }
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        val result = StreamResult(file)
        val source = DOMSource(doc)
        transformer.transform(source, result)
    }
    private fun extractGraphData(): GraphData {
        val inputText = getGraphInputAreaText().trim()
        val edgeLines = inputText.lines().filter{ it.contains("->")}

        val edges = edgeLines.map {line ->
            val mainParts = line.split("->").map{ it.trim() }
            val from = mainParts[0].trim()
            val toAndLabel = mainParts[1].split(":")
            val to = toAndLabel[0].trim()
            val label = if (toAndLabel.size > 1) toAndLabel[1].trim() else null
            GraphEdge(from, to, label)
        }
        val vertices = edges.flatMap{listOf(it.from, it.to)}.toSet().toList()
        return GraphData(vertices, edges)
    }

}