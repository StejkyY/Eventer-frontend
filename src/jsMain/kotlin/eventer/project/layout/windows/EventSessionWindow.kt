package eventer.project.layout.windows

import eventer.project.AppScope
import eventer.project.layout.EventAgendaPanel
import eventer.project.components.AgendaPrimaryButton
import eventer.project.components.addMinutesToJSDate
import eventer.project.components.lessThan
import eventer.project.models.Location
import eventer.project.models.Session
import eventer.project.models.Type
import eventer.project.state.AgendaAppAction
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.select.Select
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.form.time.DateTime
import io.kvision.html.Autocomplete
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.modal.Confirm
import io.kvision.modal.Dialog
import io.kvision.panel.flexPanel
import io.kvision.panel.hPanel
import io.kvision.state.ObservableList
import io.kvision.state.bind
import io.kvision.state.observableListOf
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.syncWithList
import kotlinx.coroutines.launch
import kotlin.js.Date

class EventSessionWindow(val state: AgendaAppState, eventAgendaPanel: EventAgendaPanel) : Dialog<Session>(caption = "Session details", animation = false) {
    private val sessionPanel: FormPanel<Session>
    private val saveButton: Button
    private val deleteButton: Button
    private val timeSelector: DateTime
    private val durationSelector: Select
    private val typeSelector: Select
    private val buttonNewType: Button
    private val buttonRemoveType: Button
    private val newTypeInputText: Text
    private val backToTypeListButton: Button
    private var selectingTypeByList: Boolean = true
    private val locationSelector: Select
    private var sessionDate: Date? = null
    private val buttonNewLocation: Button
    private val buttonRemoveLocation: Button
    private val newLocationInputText: Text
    private val backToLocationListButton: Button
    private var selectingLocationByList: Boolean = true
    private var editingId: Int? = null
    private var parentAgendaPanel = eventAgendaPanel
    private var editingSession: Session? = null
    private var typesList: ObservableList<Type> = observableListOf()
    private var locationsList: ObservableList<Location> = observableListOf()
    private var sessionsMap: Map<Double, Map<Location, List<Session>>>? = null

    init {
        typesList.syncWithList(state.sessionTypes!!)
        locationsList.syncWithList(state.selectedEventLocations!!)
        saveButton = AgendaPrimaryButton(tr("Save")){
            width = 100.px
            onClick {
                saveSession()
            }
        }
        deleteButton = Button(io.kvision.i18n.tr("Delete"), style = ButtonStyle.DANGER) {
                width = 100.px
                onClick {
                    deleteSession()
                }
        }
        timeSelector = DateTime(format = "HH:mm", label = tr("Start time")) {
            onChange {
                val startTimeValue = this.getValue()
                if (startTimeValue != null) {
                    updateDurationOptions(startTimeValue)
                }
            }
        }

        val selectDurationOptionsList : MutableList<Pair<String, String>> = mutableListOf()

        durationSelector = Select(
            options = selectDurationOptionsList,
            label = tr("Duration"),
        )

        typeSelector = Select(
            label = tr("Type")
        ).bind(typesList) { list ->
            options = list.map { it.id.toString() to it.name!! }
        }

        newTypeInputText = Text(label = "New type") {
            autocomplete = Autocomplete.OFF
        }

        backToTypeListButton = AgendaPrimaryButton(tr("Back")) {
            paddingTop = 5.px
        }

        newTypeInputText.hide()
        backToTypeListButton.hide()

        buttonNewType = AgendaPrimaryButton(tr("New type")) {
            paddingTop = 5.px
            maxWidth = 120.px
            minWidth = 120.px
            onClick {
                showNewTypeInput()
            }
        }

        buttonRemoveType = Button(io.kvision.i18n.tr("Delete selected"), style = ButtonStyle.DANGER) {
            paddingTop = 5.px
            onClick {
                deleteSelectedType()
            }
        }

        typeSelector.onEvent {
            if(typeSelector.getValue() != null &&
                    typesList[typeSelector.selectedIndex].name!! in
                        listOf("Break", "Workshop", "Session", "Lecture")) {
                buttonRemoveType.hide()
            } else {
                buttonRemoveType.show()
            }
        }

        backToTypeListButton.onClick {
            showTypeList()
        }

        locationSelector = Select(
            label = tr("Location")
        ).bind(locationsList) { list ->
            options = list.map { it.id.toString() to it.name!! }
        }

        newLocationInputText = Text(label = "New location") {
            autocomplete = Autocomplete.OFF
        }

        buttonRemoveLocation = Button(io.kvision.i18n.tr("Delete selected"), style = ButtonStyle.DANGER) {
            paddingTop = 5.px
            onClick {
                deleteSelectedLocation()
            }
        }

        backToLocationListButton = AgendaPrimaryButton(tr("Back")) {
            paddingTop = 5.px
        }

        newLocationInputText.hide()
        backToLocationListButton.hide()

        buttonNewLocation = AgendaPrimaryButton(tr("New location")) {
            paddingTop = 5.px
            maxWidth = 120.px
            minWidth = 120.px
            onClick {
                showNewLocationInput()
            }
        }

        backToLocationListButton.onClick {
            showLocationList()
        }

        sessionPanel = formPanel  {
            alignItems = AlignItems.CENTER
            add(Session::name, Text(label = "${I18n.tr("Name")}:") {
                autocomplete = Autocomplete.OFF
            }, required = true)
            add(
                Session::startTime,
                timeSelector,
                required = true
            )
            addCustom(Session::duration, durationSelector, required = true)

            add(newTypeInputText)
            add(backToTypeListButton)
            addCustom(Session::type, typeSelector, required = true)
            hPanel (spacing = 10) {
                width = 60.perc
                add(buttonNewType, 1,1)
                add(buttonRemoveType, 3, 1)
            }

            add(newLocationInputText)
            add(backToLocationListButton)
            addCustom(Session::location, locationSelector, required = true)
            hPanel (spacing = 10) {
                width = 60.perc
                add(buttonNewLocation, 1,1)
                add(buttonRemoveLocation, 3, 1)
            }
            add(Session::description, TextArea(label = tr("Description"), rows = 3)  {
                paddingTop = 5.px
            })
            flexPanel(
                FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.SPACEBETWEEN, AlignItems.CENTER,
                spacing = 5
            ) {
                add(saveButton)
                hPanel {  }
                add(deleteButton)
            }
            validator = {
                if(selectingLocationByList){
                    !checkLocationSessionsOverlap(
                        locationsList[locationSelector.selectedIndex],
                        get(Session::startTime)!!,
                        get(Session::duration)!!)
                } else {
                    true
                }
            }
            validatorMessage = { io.kvision.i18n.tr("Session time in the location is overlapping.") }
        }
    }

    private fun checkLocationSessionsOverlap(location: Location, formSessionStartTime: LocalTime, formSessionDuration: Int) : Boolean {
        return sessionsMap?.get(sessionDate?.getTime())?.get(location)?.any{
            it != editingSession && checkFormSessionOverlap(it, formSessionStartTime, formSessionDuration)
        } ?: false
    }

    private fun checkFormSessionOverlap(session: Session, formSessionStartTime: Date, formSessionDuration: Int): Boolean {
        val editingSessionEndTime = addMinutesToJSDate(session.startTime!!, session.duration!!)
        val otherSessionEndTime = addMinutesToJSDate(formSessionStartTime, formSessionDuration)
        return session.startTime?.lessThan(otherSessionEndTime)!! && formSessionStartTime.lessThan(editingSessionEndTime)
    }

    private fun updateDurationOptions(startTime: LocalTime) {
        val selectDurationOptionsList: MutableList<Pair<String, String>> = mutableListOf()

        val startMinutes = startTime.getHours() * 60 + startTime.getMinutes()
        val endMinutes = 23 * 60

        var currentMinutes = startMinutes + 30

        while (currentMinutes <= endMinutes) {
            val hours = currentMinutes / 60
            val minutes = currentMinutes % 60
            val durationInMinutes = currentMinutes - startMinutes
            val minutesString = if(minutes >= 10) {
                minutes.toString()
            } else {
                "0$minutes"
            }
            val label = if (durationInMinutes % 60 == 0) {
                "$hours:$minutesString (" + "${durationInMinutes / 60} h)"
            } else {
                "$hours:$minutesString (" + "${durationInMinutes / 60} h ${durationInMinutes % 60} min)"
            }
            selectDurationOptionsList.add(durationInMinutes.toString() to label)
            currentMinutes += 15
        }

        durationSelector.options = selectDurationOptionsList
    }

    private fun saveSession() {
        if(sessionPanel.validate()) {
            AppScope.launch {
                val location: Location?
                if(selectingLocationByList) location = locationsList[locationSelector.selectedIndex]
                else {
                    location = ConduitManager.addLocation(Location(
                        name = newLocationInputText.value,
                        eventId = (state.selectedEvent?.id)))
                    if(location != null) {
                        locationsList.syncWithList(listOf(location) + locationsList)
                        ConduitManager.showSuccessToast(io.kvision.i18n.tr("New session location added."))
                    } else {
                        ConduitManager.showErrorToast(io.kvision.i18n.tr("Error when saving session."))
                        close()
                    }
                }

                val type: Type?
                if(selectingTypeByList) type = typesList[typeSelector.selectedIndex]
                else {
                    type = ConduitManager.addType(Type(name = newTypeInputText.value))
                    if(type != null) {
                        typesList.syncWithList(listOf(type) + typesList)
                        ConduitManager.showSuccessToast(io.kvision.i18n.tr("New session type added."))
                    } else {
                        ConduitManager.showErrorToast(io.kvision.i18n.tr("Error when saving session."))
                        close()
                    }
                }

                val durationInMinutes = durationSelector.getValue()?.toIntOrNull() ?: 0
                if (state.selectedEvent != null) {
                    val session = Session (
                        id = sessionPanel[Session::id],
                        name = sessionPanel[Session::name],
                        date = sessionDate,
                        startTime = sessionPanel[Session::startTime],
                        duration = durationInMinutes,
                        description = sessionPanel[Session::description],
                        type = type,
                        location = location,
                        eventId = (state.selectedEvent.id)
                    )
                    setResult(session)
                }
                ConduitManager.showSuccessToast(io.kvision.i18n.tr("Session saved succesfully."))
                close()
            }
        }
    }

    private fun close() {
        editingId = null
        sessionPanel.clearData()
        newTypeInputText.input.setState(null)
        newLocationInputText.input.setState(null)
        this@EventSessionWindow.hide()
    }

    private fun deleteSession() {
        Confirm.show("Are you sure?", "Do you want to delete this session?") {
            AppScope.launch {
                parentAgendaPanel.removeSession(editingSession!!)
                close()
            }
        }
    }

    private fun showLocationList() {
        locationSelector.show()
        newLocationInputText.hide()
        backToLocationListButton.hide()
        selectingLocationByList = true
        buttonNewLocation.show()
        buttonRemoveLocation.show()
    }

    private fun showNewLocationInput() {
        locationSelector.hide()
        newLocationInputText.show()
        backToLocationListButton.show()
        buttonNewLocation.hide()
        buttonRemoveLocation.hide()
        selectingLocationByList = false
    }

    private fun showTypeList() {
        typeSelector.show()
        newTypeInputText.hide()
        buttonRemoveType.show()
        backToTypeListButton.hide()
        selectingTypeByList = true
        buttonNewType.show()
    }

    private fun showNewTypeInput() {
        typeSelector.hide()
        newTypeInputText.show()
        backToTypeListButton.show()
        buttonNewType.hide()
        buttonRemoveType.hide()
        selectingTypeByList = false
    }

    private fun deleteSelectedType() {
        if(typeSelector.getValue() != null && typesList.size > 4) {
            AppScope.launch {
                ConduitManager.deleteType(typesList[typeSelector.selectedIndex].id!!)
                typesList.removeAt(typeSelector.selectedIndex)
                typesList.syncWithList(typesList)
            }
        }
    }

    private fun deleteSelectedLocation() {
        if(locationSelector.getValue() != null && locationsList.size > 0) {
            AppScope.launch {
                ConduitManager.deleteLocation(locationsList[locationSelector.selectedIndex].id!!)
                locationsList.removeAt(locationSelector.selectedIndex)
                locationsList.syncWithList(locationsList)
            }
        }
    }

    suspend fun editSession(sessions: Map<Double, Map<Location, List<Session>>>, session: Session): Session? {
        sessionPanel.setData(session)
        timeSelector.value = session.startTime
        editingSession = session
        editingId = session.id
        updateDurationOptions(session.startTime!!)
        typeSelector.setValue(session.type?.id)
        locationSelector.setValue(session.location?.id)
        sessionDate = session.date
        sessionsMap = sessions
        deleteButton.show()
        showLocationList()
        showTypeList()
        return getResult()
    }

    suspend fun createSession(sessions: Map<Double, Map<Location, List<Session>>>, date: LocalDate): Session? {
        deleteButton.hide()
        sessionPanel.clearData()
        val defaultTime = LocalTime(date.getFullYear(), date.getMonth(), date.getDay(), 14, 0)
        updateDurationOptions(defaultTime)
        timeSelector.setValue(defaultTime)
        durationSelector.setValue(null)
        typeSelector.setValue(null)
        locationSelector.setValue(null)
        sessionDate = date
        sessionsMap = sessions
        editingSession = null
        showLocationList()
        showTypeList()
        return getResult()
    }
}