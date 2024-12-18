package eventer.project.layout

import io.kvision.html.Button
import io.kvision.panel.SimplePanel

abstract class EventChildPanel : SimplePanel() {
    private var saveButton: Button? = null
    var changesMade = false

    abstract fun validate(): Boolean
    abstract suspend fun save() : Boolean

    fun setSaveButton(button: Button) {
        saveButton = button
    }

    protected fun newStateOnChange() {
        if(validate()) {
            if(!changesMade) {
                saveButton?.disabled = false
                changesMade = true
            }
        }
    }
}