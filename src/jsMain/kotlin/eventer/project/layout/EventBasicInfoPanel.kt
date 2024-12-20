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
import io.kvision.html.*
import io.kvision.i18n.I18n
import io.kvision.modal.Confirm
import io.kvision.panel.*
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlin.js.Date

class EventBasicInfoPanel(val state: AgendaAppState) : EventChildPanel() {
    private val inPersonEventButton: Button
    private val hybridEventButton: Button
    private val virtualEventButton: Button
    private val duplicateEventButton: Button
    private val deleteEventButton: Button
    private val basicInfoFormPanel: FormPanel<Event>
    private val startDate: DateTime
    private val endDate: DateTime
    private val startTime: DateTime
    private val endTime: DateTime
    private var eventType = state.selectedEvent?.type

    init {
        height = 100.perc
        overflowY = Overflow.AUTO
        marginLeft = 40.px

        inPersonEventButton = Button(io.kvision.i18n.tr("In Person")){
            width = 100.px
            onClick {
                eventType = EventType.InPerson
                newStateOnChange()
            }
        }
        hybridEventButton = Button(io.kvision.i18n.tr("Hybrid")){
            width = 100.px
            onClick {
                eventType = EventType.Hybrid
                newStateOnChange()
            }
        }
        virtualEventButton = Button(io.kvision.i18n.tr("Virtual")){
            width = 100.px
            onClick {
                eventType = EventType.Virtual
                newStateOnChange()
            }
        }

        initDisableTypeButton()

        inPersonEventButton.onClick {
            disableTypeButton(inPersonEventButton, hybridEventButton, virtualEventButton)
        }
        hybridEventButton.onClick {
            disableTypeButton(hybridEventButton, inPersonEventButton, virtualEventButton)
        }
        virtualEventButton.onClick {
            disableTypeButton(virtualEventButton, hybridEventButton, inPersonEventButton)
        }
        duplicateEventButton = Button(io.kvision.i18n.tr("Duplicate event")){
            width = 100.px
            onClick {
            }
        }
        deleteEventButton = Button(io.kvision.i18n.tr("Delete event"), style = ButtonStyle.DANGER){
            width = 150.px
            marginTop = 20.px
            onClick {
                deleteEvent()
            }
        }

        startDate = DateTime(label = io.kvision.i18n.tr("Start date"), format = "YYYY-MM-DD") {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            minDate = LocalDate()
            showClear = false
            onChange {
                if(getValue()?.getTime()!! > state.selectedEvent?.startDate?.getTime()!!) {
                    Confirm.show(
                        caption = "Confirm changes",
                        text = "You will lose all sessions on days you removed. Do you want to continue?",
                        noCallback = { setValue(Date(state.selectedEvent.startDate.getTime()))} ,
                        yesCallback = {newStateOnChange()})
                } else {
                    newStateOnChange()
                }
            }
        }
        endDate = DateTime(label = io.kvision.i18n.tr("End date"), format = "YYYY-MM-DD") {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            minDate = LocalDate()
            showClear = false
            onChange {
                if(getValue()?.getTime()!! < state.selectedEvent?.endDate?.getTime()!!) {
                    Confirm.show(
                        caption = "Confirm changes",
                        text = "You will lose all sessions on days you removed. Do you want to continue?",
                        noCallback = { setValue(Date(state.selectedEvent.endDate.getTime())) },
                        yesCallback = {newStateOnChange()})
                } else {
                    newStateOnChange()
                }
            }
        }
        startTime = DateTime(label = io.kvision.i18n.tr("Start time"), format = "HH:mm").apply {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            showToday = false
            onChange {
                newStateOnChange()
            }
        }
        endTime = DateTime(label = io.kvision.i18n.tr("End time"), format = "HH:mm") {
            input.input.autocomplete = Autocomplete.OFF
            width = 150.px
            showToday = false
            onChange {
                newStateOnChange()
            }
        }

        basicInfoFormPanel = formPanel {
            height = 100.perc
            width = 100.perc
                vPanel {
                    add(Event::name,
                        Text(label = "${I18n.tr("Name")}") {
                            width = 250.px
                            marginTop = 10.px
                            onChange {
                                newStateOnChange()
                            }
                        })
                    add(Event::location,
                        Text(label = "${I18n.tr("Location")}") {
                            width = 250.px
                            onChange {
                                newStateOnChange()
                            }
                        })
                    add(
                        Event::startDate,
                        startDate
                    )
                    add(
                        Event::endDate,
                        endDate
                    )
                    add(
                        Event::startTime,
                        startTime
                    )
                    add(
                        Event::endTime,
                        endTime
                    )
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

    fun checkEventBeginBeforeEnd(): Boolean {
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

    fun getData(): Event {
        return basicInfoFormPanel.getData()
    }

    override fun validate(): Boolean {
        if(!checkEventBeginBeforeEnd()){
            ConduitManager.showErrorToast("Event start time and date has to be set before end.")
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
            newStateOnChange()
            typeButtonClicked.disabled = true
            if(typeButton2.disabled) typeButton2.disabled = false
            else if(typeButton3.disabled) typeButton3.disabled = false
        }
    }

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

    private fun deleteEvent() {
        Confirm.show("Are you sure?", "Do you want to delete this event?") {
            AppScope.launch {
                ConduitManager.deleteEvent(state.selectedEvent?.id!!)
                ConduitManager.showSuccessToast("Event successfully deleted")
                ConduitManager.showEventsPage()
            }
        }
    }

}