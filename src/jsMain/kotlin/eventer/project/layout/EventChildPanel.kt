package eventer.project.layout

import io.kvision.html.Button
import io.kvision.panel.SimplePanel

abstract class EventChildPanel(childPanelClassName: String? = null) : SimplePanel(className = childPanelClassName) {
    private var saveButton: Button? = null
    var changesMade = false


    /**
     * Validation of the data in the panel.
     */
    abstract fun validate(): Boolean

    /**
     * Saving the data in the panel.
     */
    abstract suspend fun save() : Boolean

    /**
     * Sets the used save button in the panel based on parent panel.
     */
    fun setSaveButton(button: Button) {
        saveButton = button
    }

    /**
     * After a change in the data in the panel, the save button is disabled.
     */
    protected fun newStateOnChange() {
        if(validate()) {
            if(!changesMade) {
                saveButton?.disabled = false
                changesMade = true
            }
        }
    }
}