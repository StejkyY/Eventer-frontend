package eventer.project.layout

import eventer.project.*
import eventer.project.components.AgendaIconButton
import eventer.project.models.Event
import eventer.project.models.EventPrivacy
import eventer.project.models.EventState
import eventer.project.models.EventType
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.View
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.time.DateTime
import io.kvision.html.*
import io.kvision.i18n.I18n
import io.kvision.modal.Alert
import io.kvision.panel.*
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlin.js.Date

class NewEventPanel(val state: AgendaAppState) : FormPanel<Event>() {
    private val createEventButton: Button
    private val inPersonEventButton: Button
    private val hybridEventButton: Button
    private val virtualEventButton: Button
    private val backButton: AgendaIconButton
    private var eventType: EventType? = EventType.InPerson
    private val startDate: DateTime
    private val endDate: DateTime
    private val startTime: DateTime
    private val endTime: DateTime


    private val newEventFormPanel: FormPanel<Event>

    init {
        createEventButton = Button(io.kvision.i18n.tr("Create")){
            width = 100.px
            onClick {
                saveEvent()
            }
        }
        inPersonEventButton = Button(io.kvision.i18n.tr("In Person")){
            width = 100.px
        }
        hybridEventButton = Button(io.kvision.i18n.tr("Hybrid")){
            width = 100.px
        }
        virtualEventButton = Button(io.kvision.i18n.tr("Virtual")){
            width = 100.px
        }

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

        startDate = DateTime(format = "YYYY-MM-DD", label = io.kvision.i18n.tr("Start date")).apply {
            input.input.autocomplete = Autocomplete.OFF
            placeholder = io.kvision.i18n.tr("Enter date")
            minDate = LocalDate()
        }
        endDate = DateTime(format = "YYYY-MM-DD", label = io.kvision.i18n.tr("End date")).apply {
            input.input.autocomplete = Autocomplete.OFF
            placeholder = io.kvision.i18n.tr("Enter date")
            minDate = LocalDate()
        }
        startTime = DateTime(format = "HH:mm", label = io.kvision.i18n.tr("Start time")).apply {
            input.input.autocomplete = Autocomplete.OFF
            showToday = false
        }
        endTime = DateTime(format = "HH:mm", label = io.kvision.i18n.tr("End time")).apply {
            input.input.autocomplete = Autocomplete.OFF
            showToday = false
        }
        disableTypeButton(inPersonEventButton, hybridEventButton, virtualEventButton)
        newEventFormPanel = formPanel {
            width = 400.px
            margin = 20.px
            marginLeft = auto
            marginRight = auto
            padding = 20.px
            border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            textAlign = TextAlign.CENTER

            gridPanel (templateColumns = "1fr 1fr 1fr", alignItems = AlignItems.CENTER, justifyItems = JustifyItems.CENTER) {
                add(backButton, 1, 1)
                add(Label(io.kvision.i18n.tr("Create new event")) {
                    fontSize = 28.px
                    width = 250.px
                }, 2, 1)
                paddingBottom = 15.px
            }

            hPanel {
                marginLeft = 0.px
                marginRight = 0.px
                border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                width = 100.perc
            }

            add(Event::name,
                Text(label = "${I18n.tr("Event name")}") {
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
                add(Event::location, Text(label = "${I18n.tr("Event location")}") {
                    autocomplete = Autocomplete.OFF
                }, required = true)
                add(
                    Event::startDate,
                    startDate, required = true
                )
                add(
                    Event::startTime,
                    startTime
                )
                add(
                    Event::endDate,
                    endDate, required = true
                )
                add(
                    Event::endTime,
                    endTime
                )
                validator = {
                    val startDateValue = startDate.getValue()?.getTime()!!
                    val endDateValue = endDate.getValue()?.getTime()!!
                    if(startDateValue < endDateValue) {
                        true
                    } else if (startDateValue > endDateValue){
                        false
                    } else {
                        var timeResult = true
                        if (startTime.getValue() != null && endTime.getValue() != null) {
                            timeResult = startTime.getValue()?.getTime()!! <= endTime.getValue()?.getTime()!!
                        }
                        timeResult
                    }
                }
                validatorMessage = { io.kvision.i18n.tr("Event start has to be set before the event end") }
                add(createEventButton)
            }
        }
    }

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

    private fun disableTypeButton (typeButtonClicked: Button, typeButton2: Button, typeButton3: Button) {
        if(!typeButtonClicked.disabled) {
            typeButtonClicked.disabled = true
            if(typeButton2.disabled) typeButton2.disabled = false
            else if(typeButton3.disabled) typeButton3.disabled = false
        }
    }
}