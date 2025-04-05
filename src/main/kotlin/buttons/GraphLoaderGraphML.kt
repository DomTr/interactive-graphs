package buttons

import AlertType
import GraphData
import GraphEdge
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class GraphLoaderGraphML(primaryStage: Stage,
                         updateGraph: () -> Unit,
                         showAlert: (String, String, AlertType) -> Unit,
                         changeGraphInputArea: (String) -> Unit
) : GraphLoader(primaryStage, updateGraph, showAlert, changeGraphInputArea){
    override fun createLoadGraphButton(buttonName: String, fileName:String ): Button {
        val loadGraph = Button(buttonName)
        loadGraph.setOnAction { handleLoadGraph() }
        loadGraph.setOnMouseEntered { loadGraph.style = "-fx-background-color: lightblue;" }
        loadGraph.setOnMouseExited { loadGraph.style = "" }
        loadGraph.setOnMouseClicked { loadGraph.style = "-fx-border-color: blue" }
        return loadGraph
    }
    private fun handleLoadGraph() {
        val fileChooser = FileChooser()
        val initialDirectory = File(System.getProperty("user.home"))
        fileChooser.initialDirectory = initialDirectory
        fileChooser.title = "Select graphML file of graph to be displayed"
        val ex1 = FileChooser.ExtensionFilter("TXT files ()", "*.txt")
        fileChooser.extensionFilters.addAll(ex1)
        val selectedFile = fileChooser.showOpenDialog(primaryStage)

        if (selectedFile != null) {
            println("Open file")
            try {
                val graphData = readGraphML(selectedFile)

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
    private fun readGraphML(file: File): GraphData {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("node")
        val edgeList = doc.getElementsByTagName("edge")

        val vertices = mutableListOf<String>()
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i) as Element
            vertices.add(node.getAttribute("id"))
        }
        val edges = mutableListOf<GraphEdge>()
        for (i in 0 until edgeList.length) {
            val edge = edgeList.item(i) as Element
            val from = edge.getAttribute("source")
            val to = edge.getAttribute("target")

            val dataList = edge.getElementsByTagName("data")
            var label: String? = null
            for (j in 0 until dataList.length) {
                val data = dataList.item(j) as Element
                if (data.getAttribute("key") == "label") {
                    label = data.textContent.trim()
                    break
                }
            }
            edges.add(GraphEdge(from, to, label))
        }
        return GraphData(vertices, edges)
    }
}