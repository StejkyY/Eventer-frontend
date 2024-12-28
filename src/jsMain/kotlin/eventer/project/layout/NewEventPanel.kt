package eventer.project.layout

import eventer.project.*
import eventer.project.helpers.AgendaIconButton
import eventer.project.models.Event
import eventer.project.models.EventPrivacy
import eventer.project.models.EventState
import eventer.project.models.EventType
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.View
import io.kvision.core.*
import io.kvision.i18n.tr
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.time.DateTime
import io.kvision.html.Autocomplete
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.panel.*
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlin.js.Date

class NewEventPanel(val state: AgendaAppState) : FormPanel<Event>() {
    private lateinit var createEventButton: Button
    private lateinit var inPersonEventButton: Button
    private lateinit var hybridEventButton: Button
    private lateinit var virtualEventButton: Button
    private lateinit var backButton: AgendaIconButton
    private var eventType: EventType? = EventType.InPerson
    private lateinit var startDate: DateTime
    private lateinit var endDate: DateTime
    private lateinit var startTime: DateTime
    private lateinit var endTime: DateTime


    private val newEventFormPanel: FormPanel<Event>

    init {
        textAlign = TextAlign.CENTER
        buttonsInitialization()
        dateSelectorsInitialization()
        timeSelectorsInitialization()

        newEventFormPanel = formPanel(className = "basic-form-panel") {

            gridPanel (
                templateColumns = "1fr 1fr 1fr",
                alignItems = AlignItems.CENTER,
                justifyItems = JustifyItems.CENTER) {
                add(backButton, 1, 1)
                add(Label(tr("Create new event"), className = "main-label") {
                    width = 250.px
                }, 2, 1)
                paddingBottom = 15.px
            }

            hPanel(className = "separator-line") {}

            add(Event::name,
                Text(label = tr("Event name"), maxlength = 100) {
                    paddingTop = 15.px
                    autocomplete = Autocomplete.OFF
                }, required = true)

            flexPanel(
                FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.CENTER, AlignItems.CENTER,
                spacing = 5
            ){
                paddingBottom = 10.px
                add(inPersonEventButton)
                add(hybridEventButton)
                add(virtualEventButton)
            }

            vPanel {
                alignItems = AlignItems.CENTER
                add(
                    Event::location,
                    Text(label = tr("Event location"), maxlength = 100) {
                    autocomplete = Autocomplete.OFF
                }, required = true)
                add(Event::startDate, startDate, required = true)
                add(Event::startTime, startTime)
                add(Event::endDate, endDate, required = true)
                add(Event::endTime, endTime)
                validator = {
                    checkEventBeginBeforeEnd()
                }
                validatorMessage = { tr("Event start has to be set before the event end") }
                add(createEventButton)
            }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        createEventButton = Button(tr("Create"), className = "basic-event-button"){
            onClick {
                saveEvent()
            }
        }
        inPersonEventButton = Button(tr("In Person"), className = "basic-event-button")
        hybridEventButton = Button(tr("Hybrid"), className = "basic-event-button")
        virtualEventButton = Button(tr("Virtual"), className = "basic-event-button")

        inPersonEventButton.onClick {
            eventType = EventType.InPerson
            disableTypeButton(inPersonEventButton, hybridEventButton, virtualEventButton)
        }
        hybridEventButton.onClick {
            eventType = EventType.Hybrid
            disableTypeButton(hybridEventButton, inPersonEventButton, virtualEventButton)
        }
        virtualEventButton.onClick {
            eventType = EventType.Virtual
            disableTypeButton(virtualEventButton, hybridEventButton, inPersonEventButton)
        }
        backButton = AgendaIconButton(icon = "fas fa-arrow-left") {
            onClick {
                this@NewEventPanel.hide()
                ConduitManager.showPreviousPage()
            }
        }
        disableTypeButton(inPersonEventButton, hybridEventButton, virtualEventButton)
    }

    /**
     * Initializes used date selectors.
     */
    private fun dateSelectorsInitialization() {
        startDate = DateTime(format = "YYYY-MM-DD", label = tr("Start date")).apply {
            input.input.autocomplete = Autocomplete.OFF
            placeholder = tr("Enter date")
            minDate = LocalDate()
        }
        endDate = DateTime(format = "YYYY-MM-DD", label = tr("End date")).apply {
            input.input.autocomplete = Autocomplete.OFF
            placeholder = tr("Enter date")
            minDate = LocalDate()
        }
    }

    /**
     * Initializes used time selectors.
     */
    private fun timeSelectorsInitialization() {
        startTime = DateTime(format = "HH:mm", label = tr("Start time")).apply {
            input.input.autocomplete = Autocomplete.OFF
            showToday = false
        }
        endTime = DateTime(format = "HH:mm", label = tr("End time")).apply {
            input.input.autocomplete = Autocomplete.OFF
            showToday = false
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

    /**
     * Saves the created event.
     */
    private fun saveEvent() {
        AppScope.launch {
            if(newEventFormPanel.validate()) {
                val eventData = newEventFormPanel.getData()
                val formEvent = Event(
                    name =  eventData.name,
                    location = eventData.location,
                    startDate = eventData.startDate,
                    endDate = eventData.endDate,
                    startTime = eventData.startTime,
                    endTime = eventData.endTime,
                    type = eventType,
                    state = checkEventState(eventData.startDate!!, eventData.startTime),
                    userEventRole = state.eventRoles?.find { it.name == "Owner" },
                    privacy = EventPrivacy.Private
                )
                val createdEvent = ConduitManager.addEvent(formEvent)
                if(createdEvent != null) {
                    RoutingManager.redirect("/event/${createdEvent.id}${View.EVENT_BASIC_INFO.url}")
                }
            }
        }
    }

    /**
     * Creates the state of the new event.
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
            typeButtonClicked.disabled = true
            if(typeButton2.disabled) typeButton2.disabled = false
            else if(typeButton3.disabled) typeButton3.disabled = false
        }
    }
}