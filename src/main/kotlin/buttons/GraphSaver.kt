package buttons

import AlertType
import javafx.scene.control.Button
import javafx.stage.Stage

abstract class GraphSaver(
    val primaryStage: Stage,
    val showAlert: (String, String, AlertType) -> Unit
) {
    abstract fun createSaveGraphButton(buttonName: String): Button
}