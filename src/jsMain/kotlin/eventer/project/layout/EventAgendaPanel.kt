package eventer.project.layout

import eventer.project.AppScope
import eventer.project.helpers.*
import eventer.project.models.Location
import eventer.project.models.Session
import eventer.project.state.AgendaAppAction
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import eventer.project.layout.windows.AddToCalendarWindow
import eventer.project.layout.windows.AgendaExportWindow
import eventer.project.layout.windows.EventSessionWindow
import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.i18n.I18n
import io.kvision.i18n.gettext
import io.kvision.i18n.tr
import io.kvision.panel.*
import io.kvision.state.*
import io.kvision.types.LocalTime
import io.kvision.utils.*
import kotlinx.coroutines.launch

enum class CalendarMode {
    EDIT, PREVIEW
}

class EventAgendaPanel(val state: AgendaAppState, val mode: CalendarMode): EventChildPanel() {
    private val dayTextButtonList: MutableList<AgendaTextButton> = mutableListOf()
    private lateinit var dayNavigationButtonLeft: Button
    private lateinit var dayNavigationButtonRight: Button
    private var createNewSessionButton: Button? = null
    private lateinit var buttonExport: Button
    private lateinit var buttonAddToCalendar: Button
    private val exportModalWindow: AgendaExportWindow = AgendaExportWindow()
    private val addToCalendarModalWindow: AddToCalendarWindow = AddToCalendarWindow()
    private var sessionDialogWindow = EventSessionWindow(state, this)
    private var timeline: VPanel = vPanel()
    private var sessionsPanel: HPanel = hPanel()
    private val maxShownDays = 7
    private var dayButtonsFirstIndex = ObservableValue(0)
    private val daysCount: Int
    private var selectedDayButtonIndex = 0
    private var selectedDate = ObservableValue(state.selectedEvent?.startDate!!)
    private var formattedEventSessionsMap = ObservableValue<Map<Double, Map<Location, List<Session>>>>(mapOf())


    init {
        daysCount = subtractTwoDates(state.selectedEvent?.endDate!!, state.selectedEvent.startDate!!) + 1

        formatSessions()
        buttonsInitialization()
        changeDay(0)

        display = Display.FLEX
        flexDirection = FlexDirection.COLUMN
        height = 100.perc
        if(mode == CalendarMode.PREVIEW) {
            border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
        }

        gridPanel (
            templateColumns = "1fr 1fr 1fr",
            alignItems = AlignItems.CENTER,
            justifyItems = JustifyItems.CENTER)  {

            createDayButtons()
            if(mode == CalendarMode.EDIT) {
                checkSessionsInDateRange()
            }
            marginTop = 10.px
            add(hPanel (spacing = 10) {
                add(buttonExport)
                add(buttonAddToCalendar)
            }, 1, 1)
            add(dayButtonsPanel(), 2, 1)
            if(mode == CalendarMode.EDIT) {
                add(createNewSessionButton!!, 3, 1)
            }
        }
        hPanel {
            marginLeft = 0.px
            marginRight = 0.px
            marginTop = 10.px
            border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            width = 100.perc
        }

        sessionsPanel = createSessionsPanel()
        timeline = createTimeline()

        simplePanel {
            height = 100.perc
            position = Position.RELATIVE
            overflow = Overflow.AUTO
            flexGrow = 1

            add(timeline)
            add(sessionsPanel)
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        if(mode == CalendarMode.EDIT) {
            createNewSessionButton = AgendaPrimaryButton(tr("Create new session")) {
                marginLeft = 100.px
                onClick {
                    createSession()
                }
            }
        }
        buttonExport = AgendaPrimaryButton(tr("Export")) {
            onClick {
                if(!changesMade) {
                    exportModalWindow.open(formattedEventSessionsMap.value)
                } else {
                    ConduitManager.showErrorToast(tr("Changes need to be saved first."))
                }
            }
        }
        buttonAddToCalendar = AgendaPrimaryButton(tr("Add to calendar")) {
            onClick {
                if(!changesMade) {
                    addToCalendarModalWindow.open()
                } else {
                    ConduitManager.showErrorToast(tr("Changes need to be saved first."))
                }
            }
        }

        dayNavigationButtonLeft = AgendaIconButton("fas fa-chevron-left") {
            onClick {
                dayButtonsFirstIndex.value = (dayButtonsFirstIndex.value - 1).coerceAtLeast(0)
            }
        }
        dayNavigationButtonRight = AgendaIconButton("fas fa-chevron-right") {
            onClick {
                dayButtonsFirstIndex.value = (dayButtonsFirstIndex.value + 1).coerceAtMost(daysCount - maxShownDays)

            }
        }

    }

    /**
     * Creates panel with day selector.
     * If there is more than 7 days, than there will be two side buttons
     * for cycling through days.
     */
    private fun dayButtonsPanel(): HPanel {
        return hPanel(spacing = 10).bind(dayButtonsFirstIndex) { firstButtonIndex ->
            if (daysCount > maxShownDays) {
                add(dayNavigationButtonLeft)
            }
            val endIndex = (firstButtonIndex + maxShownDays).coerceAtMost(daysCount)
            dayTextButtonList.subList(firstButtonIndex, endIndex).forEach {
                add(it)
            }
            if(daysCount > maxShownDays) {
                add(dayNavigationButtonRight)
            }
            if(firstButtonIndex == 0 ) {
                dayNavigationButtonLeft.disabled = true
            } else {
                dayNavigationButtonLeft.disabled = false
            }
            if(endIndex == daysCount) {
                dayNavigationButtonRight.disabled = true
            } else {
                dayNavigationButtonRight.disabled = false
            }
            for((index, button) in dayTextButtonList.withIndex()) {
                if(index != selectedDayButtonIndex) button.enable()
            }
        }
    }

    /**
     * Removes sessions outside the event date range from the session map.
     */
    private fun checkSessionsInDateRange() {
        for ((date, group) in formattedEventSessionsMap.value) {
            if(date < state.selectedEvent?.startDate?.getTime()!! || date > state.selectedEvent.endDate?.getTime()!!) {
                for (session in group.values.flatten()) {
                    removeSessionFromMap(session)
                }
            }
        }
    }

    /**
     * Sets up map with formatted sessions by locations and days
     */
    private fun formatSessions() {
        if(state.formattedEventSessions == null) {
            formattedEventSessionsMap.value = mutableMapOf<Double, Map<Location, List<Session>>>()
        } else {
            formattedEventSessionsMap.value = state.formattedEventSessions
        }
    }

    /**
     * Creates agenda day buttons.
     */
    private fun createDayButtons() {
        dayTextButtonList.clear()
        val dayText = gettext("Day")

        for(i: Int in 0 until daysCount) {
            val dayButton = AgendaTextButton(tr("$dayText ${i + 1}")) {
                onClick {
                    dayTextButtonList[selectedDayButtonIndex].enable()
                    disable()
                    changeDay(i)
                }
            }
            if(i == 0) {
                dayButton.disable()
            }
            dayTextButtonList.add(dayButton)
        }
    }

    /**
     * Changes current selected day to the new selected one.
     */
    private fun changeDay(newSelectedDayIndex: Int) {
        selectedDayButtonIndex = newSelectedDayIndex
        val newSelectedDate = addDaysToJSDate(state.selectedEvent?.startDate!!, newSelectedDayIndex)
        selectedDate.value = newSelectedDate
    }

    /**
     * Creates a new session in the agenda through session window.
     */
    private fun createSession() {
        AppScope.launch {
            val createdSession = sessionDialogWindow.createSession(formattedEventSessionsMap.value, selectedDate.value)
            if(createdSession == null) {
                return@launch
            }
            else {
                addSessionToMap(createdSession)
                newStateOnChange()
            }
        }
    }

    /**
     * Edits a selected session in the agenda through session window.
     */
    private fun editSession(session: Session) {
        AppScope.launch {
            val updatedSession = sessionDialogWindow.editSession(
                formattedEventSessionsMap.value,
                session)

            if(updatedSession == null) {
                return@launch
            }
            else {
                if(session.location != updatedSession.location) {
                    removeSessionFromMap(session)
                    addSessionToMap(updatedSession)
                }
                updateSessionInMap(updatedSession.copy(dayOrder = session.dayOrder), getSessionIndex(session))
                newStateOnChange()
            }
        }
    }

    /**
     * Removes slected session from the agenda.
     */
    fun removeSession(session: Session) {
        AppScope.launch {
            removeSessionFromMap(session)
            newStateOnChange()
        }
    }

    /**
     * Helper function for getting index of a given session in the map
     * in the selected day and the location.
     */
    private fun getSessionIndex(session: Session): Int {
        return formattedEventSessionsMap.value.get(selectedDate.value.getTime())?.get(session.location)?.indexOf(session) ?: 0
    }

    /**
     * Removes session from the agenda.
     */
    private fun removeSessionFromMap(session: Session) {
        val daySessionsByLocation = formattedEventSessionsMap.value.get(
            selectedDate.value.getTime())?.toMutableMap() ?: return
        val sessionsInLocation = daySessionsByLocation[session.location]?.toMutableList() ?: return
        var removed = false
        val index = getSessionIndex(session)

        if (index in sessionsInLocation.indices) {
            sessionsInLocation.removeAt(index)
            if (sessionsInLocation.isEmpty()) {
                daySessionsByLocation.remove(session.location)
            } else {
                daySessionsByLocation[session.location!!] = sessionsInLocation
            }
            removed = true
        }

        if (!removed) return

        val adjustedMap = formattedEventSessionsMap.value.toMutableMap().apply {
            put(selectedDate.value.getTime()!!, daySessionsByLocation.toMap())
        }.toMap()

        formattedEventSessionsMap.value = adjustedMap
    }

    /**
     * Updates selected session in the agenda.
     */
    private fun updateSessionInMap(session: Session, index: Int){
        val daySessionsByLocation = formattedEventSessionsMap.value.get(selectedDate.value.getTime())?.toMutableMap() ?: return

        val sessionsInLocation = daySessionsByLocation[session.location]?.toMutableList() ?: return

        if (index in sessionsInLocation.indices) {
            sessionsInLocation[index] = session
            daySessionsByLocation[session.location!!] = sessionsInLocation

            val adjustedMap = formattedEventSessionsMap.value.toMutableMap().apply {
                put(selectedDate.value.getTime()!!, daySessionsByLocation.toMap())
            }.toMap()

            formattedEventSessionsMap.value = adjustedMap
        }
    }

    /**
     * Adds session into the agenda.
     */
    private fun addSessionToMap(session: Session) {
        val daySessionsByLocation = formattedEventSessionsMap.value.get(selectedDate.value.getTime())?.toMutableMap() ?: mutableMapOf()

        val location = session.location
        var sessionsInLocation = mutableListOf<Session>()
        var locationOrder: Int
        if(daySessionsByLocation[location] == null){
            locationOrder = daySessionsByLocation.keys.size + 1
        } else {
            sessionsInLocation = daySessionsByLocation[location]?.toMutableList()!!
            locationOrder = daySessionsByLocation.keys.toList().indexOf(location) + 1
        }

        sessionsInLocation.add(session.copy(dayOrder = locationOrder))
        daySessionsByLocation[location!!] = sessionsInLocation

        val adjustedMap = formattedEventSessionsMap.value.toMutableMap().apply {
            put(selectedDate.value.getTime()!!, daySessionsByLocation.toMap())
        }.toMap()

        formattedEventSessionsMap.value = adjustedMap
    }

    /**
     * Creates timeline in the agenda calendar component.
     */
    private fun createTimeline(): VPanel {
        val timePanel = VPanel().bind(formattedEventSessionsMap) { sessionsMap ->
            val maxParallelSessionsCount = sessionsMap[selectedDate.value.getTime()]?.size ?: 0
            marginLeft = 10.px

            for (hour in 0..23) {
                val hourPanel = hPanel() {
                    minHeight = 50.px

                    val hourText = if (hour < 10) "0$hour:00 " else "$hour:00"
                    add(Label(hourText) {
                        paddingRight = 15.px
                    })
                    val ceil = if (maxParallelSessionsCount > 3) {
                        maxParallelSessionsCount
                    } else {
                       3
                    }
                    hPanel {
                        width = 100.perc
                        for (i: Int in 0 until ceil)
                            add(
                                hPanel {
                                    height = 1.px
                                    marginLeft = 0.px
                                    marginRight = 0.px
                                    marginTop = 10.px
                                    border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                                    minWidth = 33.perc
                                }
                            )
                    }
                }
                add(hourPanel)
            }
        }
        return timePanel
    }

    /**
     * Helper function for a text separator in the session block.
     */
    private fun textSeparator(): VPanel {
        return vPanel {
            marginLeft = 5.px
            border = Border(1.px, BorderStyle.SOLID, Color.name(Col.BLACK))
            marginRight = 5.px
        }
    }

    /**
     * Creates formatted texts in the session block.
     */
    private fun sessionBlockTexts(session: Session, endTime: LocalTime): HPanel {
        val textsFormatted = hPanel {
            marginLeft = 5.px
            if(session.duration!! < 60) {
                add(Label(session.name) {
                    fontSize = 0.75.rem
                    fontWeight = FontWeight.BOLD
                    sessionLabelOverflowFormating(this)
                })
                add(textSeparator())
                add(Label(session.startTime!!.toLocaleTimeString().dropLast(3) + " - " + endTime.toLocaleTimeString().dropLast(3)) {
                    fontSize = 0.75.rem
                    sessionLabelOverflowFormating(this)
                })
                add(textSeparator())
                add(Label(tr(session.type!!.name.toString())) {
                    fontSize = 0.75.rem
                    textOverflow = TextOverflow.ELLIPSIS
                    whiteSpace = WhiteSpace.NOWRAP
                    overflow = Overflow.HIDDEN
                    sessionLabelOverflowFormating(this)
                })
                add(textSeparator())
                add(Label(session.location!!.name) {
                    fontSize = 0.75.rem
                    sessionLabelOverflowFormating(this)
                })
            } else {
                vPanel {
                    add(Label(session.name) {
                        fontSize = 0.75.rem
                        fontWeight = FontWeight.BOLD
                        textOverflow = TextOverflow.ELLIPSIS
                    })
                    add(Label(session.startTime!!.toLocaleTimeString().dropLast(3) + " - " + endTime.toLocaleTimeString().dropLast(3)) {
                        fontSize = 0.75.rem
                    })
                }
                add(textSeparator())
                vPanel {
                    marginLeft = 3.px
                    height = 95.perc
                    add(Label(tr(session.type!!.name.toString())) {
                        fontSize = 0.75.rem
                    })
                    add(Label(session.location!!.name) {
                        fontSize = 0.75.rem
                    })
                }
            }
        }
        return textsFormatted
    }

    private fun sessionLabelOverflowFormating(label: Label) {
        label.textOverflow = TextOverflow.ELLIPSIS
        label.whiteSpace = WhiteSpace.NOWRAP
        label.overflow = Overflow.HIDDEN
    }

    /**
     * Creates a session block in the timeline of the calendar component.
     */
    private fun createSessionBlock(session: Session): SimplePanel {
        val endTime = addMinutesToJSDate(session.startTime!!, session.duration!!)
        val sessionBlock = SimplePanel {
            top = ((session.startTime!!.getHours() * 60 + session.startTime!!.getMinutes()) * 5/6).px
            height = ((endTime.getHours() * 60 + endTime.getMinutes() - (session.startTime!!.getHours() * 60 + session.startTime!!.getMinutes())) * 5/6).px
            background = if(session.type!!.name == "Break" ) Background(Color.name(Col.LIGHTGREEN))
            else Background(Color.name(Col.SKYBLUE))
            position = Position.ABSOLUTE
            borderRadius = 5.px
            borderTop = Border(1.px, BorderStyle.SOLID, color = Color.name(Col.WHITE))
            borderLeft = Border(1.px, BorderStyle.SOLID, color = Color.name(Col.WHITE))
            width = 100.perc

            if(mode == CalendarMode.EDIT) {
                cursor = Cursor.POINTER
                onClick {
                    editSession(session)
                }
            } else {
                enableTooltip(
                    TooltipOptions(
                        title = session.description ?: tr("No description available"),
                        placement = Placement.AUTO,
                        triggers = listOf(Trigger.HOVER)
                    )
                )
            }

            add(sessionBlockTexts(session, endTime))
        }
        return sessionBlock
    }

    /**
     * Sets all the session in the created layout into the calendar component.
     */
    private fun createSessionsPanel(): HPanel {
        val sessionPanel = HPanel().bind(formattedEventSessionsMap) { sessionsMap ->
            top = 0.px
            left = 0.px
            width = 93.perc
            marginLeft = 55.px
            paddingLeft = 2.perc
            marginTop = 11.px
            position = Position.ABSOLUTE
            bind(selectedDate) { date ->
                if(sessionsMap[date.getTime()] != null) {
                    for((_, sessions) in sessionsMap[date.getTime()]!!) {
                        val count = sessionsMap[date.getTime()]!!.size
                        val blockMinWidth = when {
                            count <= 1 -> 100.perc
                            count == 2 -> 50.perc
                            else -> 33.perc
                        }
                        val parallelVPanel: VPanel = vPanel {
                            position = Position.RELATIVE
                            minWidth = blockMinWidth
                            height = 100.perc
                        }

                        sessions.forEachIndexed { index, session ->
                            val sessionBlock = createSessionBlock(session)
                            parallelVPanel.add(sessionBlock)
                        }
                        add(parallelVPanel)
                    }
                }
            }
        }
        return sessionPanel
    }

    override fun validate(): Boolean {
        return true
    }

    /**
     * Saves the current state of the agenda.
     */
    override suspend fun save(): Boolean {
        if(saveSessionsState()) {
            ConduitManager.getSessionsForEvent(state.selectedEvent?.id!!)
            ConduitManager.agendaStore.dispatch(AgendaAppAction.formattedEventSessionsLoaded(formattedEventSessionsMap.value))
            return true
        } else return false
    }

    /**
     * Compares the current state of the session to the state of last save.
     */
    private suspend fun saveSessionsState(): Boolean {
        val defaultSessions = state.selectedEventSessions
        val currentSessions = formattedEventSessionsMap.value.flatMap { (_, sessionsByLocation) ->
            sessionsByLocation.flatMap { (_, sessions) -> sessions }
        }

        val deletedSessions: MutableList<Session> = mutableListOf()
        val addedSessions: MutableList<Session> = mutableListOf()
        val updatedSessions: MutableList<Session> = mutableListOf()

        if (defaultSessions != null) {
            for (session in defaultSessions) {
                if (session.id != null && !currentSessions.any { it.id == session.id }) {
                    deletedSessions.add(session)
                }
            }
        }

        for (session in currentSessions) {
            if (session.id == null) addedSessions.add(session)
            else {
                val correspondingSession = defaultSessions?.find { it.id == session.id }
                if (correspondingSession != null && session != correspondingSession) {
                    updatedSessions.add(session)
                }
            }
        }
        return ConduitManager.saveEventAgendaSessions(state.selectedEvent?.id!!, addedSessions, updatedSessions, deletedSessions)
    }
}