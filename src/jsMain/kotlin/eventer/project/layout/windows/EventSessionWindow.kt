package eventer.project.layout.windows

import eventer.project.AppScope
import eventer.project.layout.EventAgendaPanel
import eventer.project.helpers.AgendaPrimaryButton
import eventer.project.helpers.addMinutesToJSDate
import eventer.project.helpers.lessThan
import eventer.project.models.Location
import eventer.project.models.Session
import eventer.project.models.Type
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
import io.kvision.i18n.gettext
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

class EventSessionWindow(val state: AgendaAppState, eventAgendaPanel: EventAgendaPanel) : Dialog<Session>(caption = tr("Session details"), animation = false) {
    private val sessionPanel: FormPanel<Session>
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var timeSelector: DateTime
    private lateinit var durationSelector: Select
    private lateinit var typeSelector: Select
    private lateinit var buttonNewType: Button
    private lateinit var buttonRemoveType: Button
    private lateinit var newTypeInputText: Text
    private lateinit var backToTypeListButton: Button
    private var selectingTypeByList: Boolean = true
    private lateinit var locationSelector: Select
    private var sessionDate: Date? = null
    private lateinit var buttonNewLocation: Button
    private lateinit var buttonRemoveLocation: Button
    private lateinit var newLocationInputText: Text
    private lateinit var backToLocationListButton: Button
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
        buttonsInitialization()
        selectorsInitialization()
        inputTextsInitialization()

        sessionPanel = formPanel  {
            alignItems = AlignItems.CENTER
            add(Session::name, Text(label = tr("Name"), maxlength = 100) {
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
            addCustom(
                Session::type,
                typeSelector,
                required = true)
            hPanel (spacing = 10) {
                width = 60.perc
                add(buttonNewType, 1,1)
                add(buttonRemoveType, 3, 1)
            }

            add(newLocationInputText)
            add(backToLocationListButton)
            addCustom(
                Session::location,
                locationSelector,
                required = true,
                validatorMessage = {"Missing value."},
                validator = {it.selectedIndex != -1})
            hPanel (spacing = 10) {
                width = 60.perc
                add(buttonNewLocation, 1,1)
                add(buttonRemoveLocation, 3, 1)
            }
            add(Session::description, TextArea(label = tr("Description"), rows = 3)  {
                paddingTop = 15.px
                maxlength = 500
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
                if(selectingLocationByList && locationSelector.selectedIndex != -1){
                    !checkLocationSessionsOverlap(
                        locationsList[locationSelector.selectedIndex],
                        get(Session::startTime)!!,
                        get(Session::duration)!!)
                } else {
                    true
                }
            }
            validatorMessage = { tr("Session time in the location is overlapping.") }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        saveButton = AgendaPrimaryButton(tr("Save"), buttonClassName = "basic-event-button"){
            onClick {
                saveSession()
            }
        }
        deleteButton = Button(tr("Delete"), style = ButtonStyle.DANGER, className = "basic-event-button") {
            onClick {
                deleteSession()
            }
        }
        backToTypeListButton = AgendaPrimaryButton(tr("Back"), buttonClassName = "sessions-window-back-button") {
            onClick {
                showTypeList()
            }
        }

        buttonNewType = AgendaPrimaryButton(tr("New type"), buttonClassName = "sessions-window-new-button") {
            onClick {
                showNewTypeInput()
            }
        }

        buttonRemoveType = Button(
            tr("Delete selected"),
            style = ButtonStyle.DANGER,
            className = "sessions-window-delete-button") {
            onClick {
                deleteSelectedType()
            }
        }

        buttonRemoveLocation = Button(
            tr("Delete selected"),
            style = ButtonStyle.DANGER,
            className = "sessions-window-delete-button") {
            onClick {
                deleteSelectedLocation()
            }
        }

        backToLocationListButton = AgendaPrimaryButton(tr("Back"), buttonClassName = "sessions-window-back-button") {
            onClick {
                showLocationList()
            }
        }

        buttonNewLocation = AgendaPrimaryButton(tr("New location"), buttonClassName = "sessions-window-new-button") {
            onClick {
                showNewLocationInput()
            }
        }

        backToTypeListButton.hide()
        backToLocationListButton.hide()
    }


    /**
     * Initializes used selectors.
     */
    private fun selectorsInitialization() {
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
            options = list.map { it.id.toString() to tr(it.name!!) }
        }

        typeSelector.onChange {
            if(typeSelector.getValue() != null &&
                typesList[typeSelector.selectedIndex].name!! in
                listOf(
                    gettext("Break"),
                    gettext("Workshop"),
                    gettext("Session"),
                    gettext("Lecture"))) {
                buttonRemoveType.hide()
            } else {
                buttonRemoveType.show()
            }
        }

        locationSelector = Select(
            label = tr("Location")
        ).bind(locationsList) { list ->
            paddingTop = 15.px
            options = list.map { it.id.toString() to it.name!! }
        }
    }

    /**
     * Initializes used input texts.
     */
    private fun inputTextsInitialization() {
        newTypeInputText = Text(label = tr("New type"), maxlength = 50) {
            autocomplete = Autocomplete.OFF
        }
        newLocationInputText = Text(label = tr("New location"), maxlength = 50) {
            paddingTop = 15.px
            autocomplete = Autocomplete.OFF
        }

        newTypeInputText.hide()
        newLocationInputText.hide()
    }

    /**
     * Checks if the edited/new session is overlapping with atleast one of the sessions
     * during the selected time in the selected location.
     */
    private fun checkLocationSessionsOverlap(
        location: Location,
        formSessionStartTime: LocalTime,
        formSessionDuration: Int) : Boolean {
        return sessionsMap?.get(sessionDate?.getTime())?.get(location)?.any{
            it != editingSession && checkFormSessionOverlap(it, formSessionStartTime, formSessionDuration)
        } ?: false
    }

    /**
     * Compares overlapping of a session and session in the form window.
     */
    private fun checkFormSessionOverlap(session: Session, formSessionStartTime: Date, formSessionDuration: Int): Boolean {
        val editingSessionEndTime = addMinutesToJSDate(session.startTime!!, session.duration!!)
        val otherSessionEndTime = addMinutesToJSDate(formSessionStartTime, formSessionDuration)
        return session.startTime?.lessThan(otherSessionEndTime)!! && formSessionStartTime.lessThan(editingSessionEndTime)
    }

    /**
     * Updates options in duration selector according to the start time in the form.
     */
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

    /**
     * Saves the new/edited session opened in the form.
     */
    private fun saveSession() {
        if(sessionPanel.validate()) {
            AppScope.launch {
                val location: Location?

                if(selectingLocationByList) location = locationsList[locationSelector.selectedIndex]
                else {
                    //if location was created, then it will be saved in database
                    location = ConduitManager.addLocation(Location(
                        name = newLocationInputText.value,
                        eventId = (state.selectedEvent?.id)))
                    if(location != null) {
                        locationsList.syncWithList(listOf(location) + locationsList)
                        ConduitManager.showSuccessToast(tr("New session location added."))
                    } else {
                        ConduitManager.showErrorToast(tr("Error when saving session."))
                        close()
                    }
                }

                val type: Type?
                if(selectingTypeByList) type = typesList[typeSelector.selectedIndex]
                else {
                    //if type was created, then it will be saved in database
                    type = ConduitManager.addType(Type(name = newTypeInputText.value))
                    if(type != null) {
                        typesList.syncWithList(listOf(type) + typesList)
                        ConduitManager.showSuccessToast(tr("New session type added."))
                    } else {
                        ConduitManager.showErrorToast(tr("Error when saving session."))
                        close()
                    }
                }

                val durationInMinutes = durationSelector.getValue()?.toIntOrNull() ?: 0
                if (state.selectedEvent != null) {
                    val session = Session (
                        id = editingId,
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
                ConduitManager.showSuccessToast(tr("Session saved succesfully."))
                close()
            }
        }
    }

    /**
     * Closes the window.
     */
    private fun close() {
        editingId = null
        sessionPanel.clearData()
        newTypeInputText.input.setState(null)
        newLocationInputText.input.setState(null)
        this@EventSessionWindow.hide()
    }

    /**
     * Deletes opened edited session in the form.
     */
    private fun deleteSession() {
        Confirm.show(tr("Are you sure?"), tr("Do you want to delete this session?")) {
            AppScope.launch {
                parentAgendaPanel.removeSession(editingSession!!)
                close()
            }
        }
    }

    /**
     * Shows session location list and hides creating of new location.
     */
    private fun showLocationList() {
        locationSelector.show()
        newLocationInputText.hide()
        backToLocationListButton.hide()
        selectingLocationByList = true
        buttonNewLocation.show()
        buttonRemoveLocation.show()
    }

    /**
     * Shows new session location input and hides locations list.
     */
    private fun showNewLocationInput() {
        locationSelector.hide()
        newLocationInputText.show()
        backToLocationListButton.show()
        buttonNewLocation.hide()
        buttonRemoveLocation.hide()
        selectingLocationByList = false
    }

    /**
     * Shows session type list and hides creating of new type.
     */
    private fun showTypeList() {
        typeSelector.show()
        newTypeInputText.hide()
        buttonRemoveType.show()
        backToTypeListButton.hide()
        selectingTypeByList = true
        buttonNewType.show()
    }

    /**
     * Shows new session type input and hides types list.
     */
    private fun showNewTypeInput() {
        typeSelector.hide()
        newTypeInputText.show()
        backToTypeListButton.show()
        buttonNewType.hide()
        buttonRemoveType.hide()
        selectingTypeByList = false
    }

    /**
     * Deletes selected created type from types list.
     */
    private fun deleteSelectedType() {
        if(typeSelector.selectedIndex != -1 && typesList.size > 4) {
            AppScope.launch {
                ConduitManager.deleteType(typesList[typeSelector.selectedIndex].id!!)
                typesList.removeAt(typeSelector.selectedIndex)
                typesList.syncWithList(typesList)
            }
        }
    }

    /**
     * Deletes selected created location from locations list.
     */
    private fun deleteSelectedLocation() {
        if(locationSelector.selectedIndex != -1 && locationsList.size > 0) {
            AppScope.launch {
                ConduitManager.deleteLocation(locationsList[locationSelector.selectedIndex].id!!)
                locationsList.removeAt(locationSelector.selectedIndex)
                locationsList.syncWithList(locationsList)
            }
        }
    }

    /**
     * Sets the form for editing a session from the event agenda.
     */
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
        sessionPanel.clearValidation()
        return getResult()
    }

    /**
     * Sets the form for creating a new session to the event agenda.
     */
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
        sessionPanel.clearValidation()
        return getResult()
    }
}