package eventer.project.layout

import eventer.project.models.Event
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.RichText
import io.kvision.panel.vPanel
import io.kvision.utils.perc
import io.kvision.utils.px

class EventDescriptionPanel(val state: AgendaAppState) : EventChildPanel() {
    private val descriptionFormPanel: FormPanel<Event>
    private val richText: RichText

    init {
        richText = RichText() {
            marginTop = 10.px
            maxlength = 500
            inputHeight = 490.px
        }
        descriptionFormPanel = formPanel {
            width = 95.perc
            add(Event::description, richText)
            if(state.selectedEvent != null) {
                setData(state.selectedEvent)
            }
        }
        richText.onChange {
            if(descriptionFormPanel.getData().description != state.selectedEvent?.description) {
                newStateOnChange()
            }
        }
        vPanel {
            alignItems = AlignItems.CENTER
            add(descriptionFormPanel)
        }
    }

    fun getData(): Event {
        return descriptionFormPanel.getData()
    }

    override fun validate(): Boolean {
        return descriptionFormPanel.validate()
    }

    override suspend fun save(): Boolean {
        val event = state.selectedEvent?.copy(description = descriptionFormPanel.getData().description)
        return ConduitManager.updateEvent(event!!)
//        Model.eventPreviewLoad(state.previewEventState?.copy(event = event)!!)
    }
}