package buttons

import AlertType
import javafx.scene.control.Button
import javafx.stage.Stage

abstract class GraphLoader (
    val primaryStage: Stage,
    val updateGraph: () -> Unit,
    val showAlert: (String, String, AlertType) -> Unit,
    val changeGraphInputArea: (String) -> Unit
){
    abstract fun createLoadGraphButton(buttonName: String, fileName:String): Button
}