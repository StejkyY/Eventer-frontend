package eventer.project.layout

import eventer.project.AppScope
import eventer.project.models.Event
import eventer.project.models.EventState
import eventer.project.models.EventType
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.time.DateTime
import io.kvision.html.Autocomplete
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.modal.Confirm
import io.kvision.panel.*
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlin.js.Date

class EventBasicInfoPanel(val state: AgendaAppState): EventChildPanel(childPanelClassName = "event-basic-info-panel") {
    private lateinit var inPersonEventButton: Button
    private lateinit var hybridEventButton: Button
    private lateinit var virtualEventButton: Button
    private lateinit var duplicateEventButton: Button
    private lateinit var deleteEventButton: Button
    private val basicInfoFormPanel: FormPanel<Event>
    private lateinit var startDate: DateTime
    private lateinit var endDate: DateTime
    private lateinit var startTime: DateTime
    private lateinit var endTime: DateTime
    private var eventType = state.selectedEvent?.type

    init {
        buttonsInitialization()
        dateSelectorsInitialization()
        timeSelectorsInitialization()

        basicInfoFormPanel = formPanel {
            height = 100.perc
            width = 100.perc
                vPanel {
                    add(Event::name,
                        Text(label = tr("Name"), maxlength = 100) {
                            width = 250.px
                            marginTop = 10.px
                            onChange {
                                newStateOnChange()
                            }
                        })
                    add(Event::location,
                        Text(label = tr("Location"), maxlength = 100) {
                            width = 250.px
                            onChange {
                                newStateOnChange()
                            }
                        })
                    add(Event::startDate, startDate)
                    add(Event::endDate, endDate)
                    add(Event::startTime, startTime)
                    add(Event::endTime, endTime)
                    add(vPanel(alignItems = AlignItems.START) {
                        paddingBottom = 20.px
                        add(hPanel(spacing = 10){
                            marginTop = 10.px
                            add(inPersonEventButton)
                            add(hybridEventButton)
                            add(virtualEventButton)
                        })
                        add(deleteEventButton)
                    })
                }
        }
        if(state.selectedEvent != null) {
            basicInfoFormPanel.setData(state.selectedEvent)
            startDate.value = Date(state.selectedEvent.startDate?.getTime()!!)
            endDate.value = Date(state.selectedEvent.endDate?.getTime()!!)
            if(state.selectedEvent.startTime != null) {
                startTime.value = Date(state.selectedEvent.startTime.getTime())
            }
            if(state.selectedEvent.endTime != null) {
                endTime.value = Date(state.selectedEvent.endTime.getTime())
            }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        inPersonEventButton = Button(tr("In Person"), className = "basic-event-button"){
            onClick {
                eventType = EventType.InPerson
                disableTypeButton(inPersonEventButton, hybridEventButton, virtualEventButton)
                newStateOnChange()
            }
        }
        hybridEventButton = Button(tr("Hybrid"), className = "basic-event-button"){
            onClick {
                eventType = EventType.Hybrid
                disableTypeButton(hybridEventButton, inPersonEventButton, virtualEventButton)
                newStateOnChange()
            }
        }
        virtualEventButton = Button(tr("Virtual"), className = "basic-event-button"){
            onClick {
                eventType = EventType.Virtual
                disableTypeButton(virtualEventButton, hybridEventButton, inPersonEventButton)
                newStateOnChange()
            }
        }

        initDisableTypeButton()

        deleteEventButton = Button(
            tr("Delete event"),
            style = ButtonStyle.DANGER,
            className = "event-delete-button"){
            onClick {
                deleteEvent()
            }
        }
    }

    /**
     * Initializes used date selectors.
     */
    private fun dateSelectorsInitialization() {
        startDate = DateTime(label = tr("Start date"), format = "YYYY-MM-DD") {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            minDate = LocalDate()
            showClear = false
            onChange {
                if(getValue()?.getTime()!! > state.selectedEvent?.startDate?.getTime()!!) {
                    Confirm.show(
                        caption = tr("Confirm changes"),
                        text = tr("You will lose all sessions on days you removed. Do you want to continue?"),
                        noCallback = { setValue(Date(state.selectedEvent.startDate.getTime()))} ,
                        yesCallback = {newStateOnChange()})
                } else {
                    newStateOnChange()
                }
            }
        }
        endDate = DateTime(label = tr("End date"), format = "YYYY-MM-DD") {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            minDate = LocalDate()
            showClear = false
            onChange {
                if(getValue()?.getTime()!! < state.selectedEvent?.endDate?.getTime()!!) {
                    Confirm.show(
                        caption = tr("Confirm changes"),
                        text = tr("You will lose all sessions on days you removed. Do you want to continue?"),
                        noCallback = { setValue(Date(state.selectedEvent.endDate.getTime())) },
                        yesCallback = {newStateOnChange()})
                } else {
                    newStateOnChange()
                }
            }
        }
    }

    /**
     * Initializes used time selectors.
     */
    private fun timeSelectorsInitialization() {
        startTime = DateTime(label = tr("Start time"), format = "HH:mm").apply {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            showToday = false
            onChange {
                newStateOnChange()
            }
        }
        endTime = DateTime(label = tr("End time"), format = "HH:mm") {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            showToday = false
            onChange {
                newStateOnChange()
            }
        }
    }

    /**
     * Check if the start date and time is set before the end date and time.
     */
    fun checkEventBeginBeforeEnd(): Boolean {
        if (startDate.getValue() == null || endDate.getValue() == null){
            return false
        }
        val startDateValue = startDate.getValue()?.getTime()!!
        val endDateValue = endDate.getValue()?.getTime()!!
        if(startDateValue < endDateValue) {
            return true
        } else if (startDateValue > endDateValue){
            return false
        } else {
            var timeResult = true
            if (startTime.getValue() != null && endTime.getValue() != null) {
                timeResult = startTime.getValue()?.getTime()!! <= endTime.getValue()?.getTime()!!
            }
            return timeResult
        }
    }

    override fun validate(): Boolean {
        if(!checkEventBeginBeforeEnd()){
            ConduitManager.showErrorToast(tr("Event start time and date has to be set before end."))
            return false
        }
        else return basicInfoFormPanel.validate()
    }

    override suspend fun save() : Boolean {
        val eventData = basicInfoFormPanel.getData()
        val event = state.selectedEvent!!.copy(
            name = eventData.name,
            location = eventData.location,
            startDate = eventData.startDate,
            endDate = eventData.endDate,
            startTime = eventData.startTime,
            endTime = eventData.endTime,
            type = eventType,
            state = checkEventState(eventData.startDate!!, eventData.startTime))
        return ConduitManager.updateEvent(event)
    }


    /**
     * Checks the state of the edited event.
     */
    private fun checkEventState(eventStartDate: Date, eventStartTime: LocalTime?): EventState {
        return if (eventStartDate.getDate() == LocalDate().getDate()) {
            if (eventStartTime == null || eventStartTime.getTime() >= LocalTime.now()) {
                EventState.InProgress
            } else {
                EventState.Upcoming
            }
        } else {
            EventState.Upcoming
        }
    }


    /**
     * Disables selected event type button.
     */
    private fun disableTypeButton (typeButtonClicked: Button, typeButton2: Button, typeButton3: Button) {
        if(!typeButtonClicked.disabled) {
            newStateOnChange()
            typeButtonClicked.disabled = true
            if(typeButton2.disabled) typeButton2.disabled = false
            else if(typeButton3.disabled) typeButton3.disabled = false
        }
    }


    /**
     * Disables button with event type based on the initiation.
     */
    private fun initDisableTypeButton () {
        if(state.selectedEvent != null) {
            when (state.selectedEvent.type) {
                EventType.InPerson -> {
                    inPersonEventButton.disabled = true
                }
                EventType.Virtual -> {
                    virtualEventButton.disabled = true
                }
                else -> {
                    hybridEventButton.disabled = true
                }
            }
        }
    }


    /**
     * Deletes event.
     */
    private fun deleteEvent() {
        Confirm.show(tr("Are you sure?"), tr("Do you want to delete this event?")) {
            AppScope.launch {
                ConduitManager.deleteEvent(state.selectedEvent?.id!!)
                ConduitManager.showSuccessToast(tr("Event successfully deleted"))
                ConduitManager.showEventsPage()
            }
        }
    }

}