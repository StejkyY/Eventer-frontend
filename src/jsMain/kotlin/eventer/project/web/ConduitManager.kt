package eventer.project.web

import eventer.project.AppScope
import eventer.project.Security
import eventer.project.components.addMinutesToJSDate
import eventer.project.components.addTimeToJSDate
import eventer.project.models.*


import eventer.project.state.AgendaAppAction
import eventer.project.state.AgendaAppState
import eventer.project.state.agendaAppReducer
import eventer.project.web.RoutingManager.redirect
import io.kvision.core.BsColor
import io.kvision.i18n.I18n
import io.kvision.redux.createTypedReduxStore
import io.kvision.rest.*
import io.kvision.toast.ToastContainer
import io.kvision.toast.ToastContainerPosition
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.RequestInit
import web.http.Headers
import kotlin.js.json

//data class EditingEventState(val event: Event, val sessions: List<Session>, val locations: List<Location>) {
//
//    val defaultState: EditingEventState = this
//
//    fun getDefaultState(): EditingEventState {
//        return defaultState
//    }
//}

@Serializable
enum class Sort {
    FN, LN, E, F
}

enum class Provider {
    GOOGLE, MICROSOFT
}

object ConduitManager {

    const val JWT_TOKEN = "jwtToken"
    const val GOOGLE_ACCESS_TOKEN = "googleAccessToken"
    const val MICROSOFT_ACCESS_TOKEN = "microsoftAccessToken"

    val agendaStore = createTypedReduxStore(::agendaAppReducer, AgendaAppState())

    val toastContainer = ToastContainer(ToastContainerPosition.TOPRIGHT)

    private suspend fun genericRequestExceptionHandler(e: RemoteRequestException) {
        if(e.code.toInt() == 0) showErrorToast("Error with connection to the server. Please try again later.")
        else showErrorToast(I18n.tr(e.response?.text()?.await()!!))
    }

//    val events: ObservableList<Event> = observableListOf()
//    val editingEventState = ObservableValue(EditingEventState(Event(), listOf(), listOf()))
//    val sessionTypes: ObservableList<Type> = observableListOf()
//    val profile = ObservableValue(Profile())

    fun initialize() {
        RoutingManager.init()
        eventsInit()
    }

    fun eventsInit() {
        AppScope.launch {
            getEventList()
            getEventRoles()
        }
    }

    fun showProfilePage() {
        agendaStore.dispatch(AgendaAppAction.profilePage)
    }
    fun showEventsPage() {
        AppScope.launch {
            getEventList()
            agendaStore.dispatch(AgendaAppAction.eventsPage)
        }
    }
    fun showLoginPage() {
        agendaStore.dispatch(AgendaAppAction.loginPage)
    }
    fun showRegisterPage() {
        agendaStore.dispatch(AgendaAppAction.registerPage)
    }
    fun showNewEventPage() {
        agendaStore.dispatch(AgendaAppAction.newEventPage)
    }

    fun loadEvent(eventId: Int, agendaLoad: Boolean = false) {
        AppScope.launch {
            val event = Api.getEvent(eventId)
            if (agendaLoad || agendaStore.getState().selectedEvent?.id != eventId) {
                getSessionsForEvent(eventId)
                getSessionTypes()
                getSessionLocations(eventId)
            }
            agendaStore.dispatch(AgendaAppAction.eventLoaded(event))
            formatSessions()
        }
    }

    fun showEventBasicInfoPage(eventId: Int) {
        loadEvent(eventId)
        agendaStore.dispatch(AgendaAppAction.eventBasicInfoPage)
    }

    fun showEventDescriptionPage(eventId: Int) {
        loadEvent(eventId)
        agendaStore.dispatch(AgendaAppAction.eventDescriptionPage)
    }

    fun showEventAgendaPage(eventId: Int) {
        loadEvent(eventId, true)
        agendaStore.dispatch(AgendaAppAction.eventAgendaPage)
    }

    fun showEventPreviewPage(eventId: Int) {
        AppScope.launch {
            val event = Api.getEvent(eventId)
            agendaStore.dispatch(AgendaAppAction.eventLoaded(event))
            formatSessions()
            agendaStore.dispatch(AgendaAppAction.eventPreviewPage)
        }
    }

    fun showPreviousPage() {
        if ((agendaStore.store.state.previousView == View.EVENT_BASIC_INFO ||
                    agendaStore.store.state.previousView == View.EVENT_DESCRIPTION ||
                        agendaStore.store.state.previousView == View.EVENT_AGENDA) && agendaStore.store.state.selectedEvent != null) {
            redirect("/event/${agendaStore.store.state.selectedEvent!!.id}${agendaStore.store.state.previousView.url}")
        } else {
            redirect(agendaStore.store.state.previousView)
        }
    }

    suspend fun addEvent(event: Event): Event? {
        try {
            var receivedEvent = Event()
            Security.withAuth {
                receivedEvent = Api.addEvent(event)
            }
            getEventList()
            return receivedEvent
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return null
        }
    }

    suspend fun updateEvent(event: Event): Boolean {
        try {
            Security.withAuth {
                val receivedEvent = Api.updateEvent(event)
                agendaStore.dispatch(AgendaAppAction.eventUpdated(receivedEvent))
            }
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    suspend fun deleteEvent(id: Int): Boolean {
        try {
            Security.withAuth {
                Api.deleteEvent(id)
                getEventList()
            }
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    suspend fun updateProfile(profile: User){
        try {
            Security.withAuth {
                val user = Api.updateProfile(profile)
                saveLocalStorageToken(JWT_TOKEN, user.token!!)
            }
            readProfile()
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
        }
    }

    suspend fun deleteProfile() {
        try {
            Security.withAuth {
                Api.deleteProfile()
                logout()
            }
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
        }
    }

    fun login (userCredentials: UserCredentials) {
        AppScope.launch {
            Security.completeLogin(userCredentials)
        }
    }

    suspend fun getEventList() {
        try {
            Security.withAuth {
                val newEvents = Api.getEventsList()
                agendaStore.dispatch(AgendaAppAction.eventsLoaded(newEvents))
            }
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
        }
    }

    suspend fun getEventRoles() {
        Security.withAuth {
            val newRoles = Api.getEventRoles()
            agendaStore.dispatch(AgendaAppAction.eventRolesLoaded(newRoles))
        }
    }

    suspend fun getSessionsForEvent(eventId: Int) {
        Security.withAuth {
            val newSessions = Api.getEventSessions(eventId)
            agendaStore.dispatch(AgendaAppAction.eventSessionsLoaded(newSessions))
        }
    }

//    suspend fun formatEventForEvent(eventId: Int) {
//        val newSessions = sessionService.getEventSessionsFormatted(eventId)
//        agendaStore.dispatch(AgendaAppAction.eventSessionsFormattedLoaded(newSessions))
//    }

    suspend fun deleteSession(sessionId: Int): Boolean {
        Security.withAuth {
            Api.deleteSession(sessionId)
        }
        return true
    }

    suspend fun addSession(session: Session) {
        Security.withAuth {
            Api.addSession(session)
        }
    }


    suspend fun updateSession(session: Session) {
        Security.withAuth {
            Api.updateSession(session)
        }
    }

    suspend fun saveEventAgendaSessions(eventId: Int,
                                        addedSessions: List<Session>,
                                        updatedSessions: List<Session>,
                                        deletedSessions: List<Session>): Boolean {
        try {
            Security.withAuth {
                Api.saveEventAgendaSessions(eventId, addedSessions, updatedSessions, deletedSessions)
            }
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    fun formatSessions() {
        val allSessionsGroupedByDate = agendaStore.getState().selectedEventSessions!!.groupBy {
            it.date?.getTime()!!
        }
        val formattedSessionsMap = mutableMapOf<Double, Map<Location, List<Session>>>()

        for ((date, sessions) in allSessionsGroupedByDate) {
            val sessionsGroupedByLocation = sessions.groupBy { it.location!! } .mapValues { (_, locationSessions) ->
                locationSessions.sortedBy { it.dayOrder }
            }
            formattedSessionsMap[date] = sessionsGroupedByLocation
        }

        agendaStore.dispatch(AgendaAppAction.formattedEventSessionsLoaded(formattedSessionsMap))
    }

//    suspend fun addSessionList(sessionList: List<Session>) {
//        Security.withAuth {
//            for (session in sessionList) {
//                sessionService.addSession(session)
//            }
//        }
//    }
//
//    suspend fun updateSessionList(sessionList: List<Session>) {
//        Security.withAuth {
//            for (session in sessionList) sessionService.updateSession(session)
//            getSessionsForEvent()
//        }
//    }
//
//    suspend fun removeSessionList(sessionList: List<Session>) {
//        Security.withAuth {
//            for (session in sessionList) sessionService.deleteSession(session.id!!)
//            getSessionsForEvent()
//        }
//    }

    suspend fun addType(type: Type): Type? {
        try {
            var result = Type()
            Security.withAuth {
                result = Api.addType(type)
            }
            return result
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return null
        }
    }

    suspend fun deleteType(typeId: Int): Boolean {
        try {
            Security.withAuth {
                Api.deleteType(typeId)
            }
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    suspend fun getSessionTypes() {
        try {
            Security.withAuth {
                val newTypes = Api.getSessionTypes()
                agendaStore.dispatch(AgendaAppAction.sessionTypesLoaded(newTypes))
            }
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
        }
    }

    suspend fun addLocation(location: Location): Location? {
        try {
            var result = Location()
            Security.withAuth {
                result = Api.addLocation(location)
            }
            return result
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return null
        }
    }

    suspend fun deleteLocation(locationId: Int): Boolean {
        try {
            Security.withAuth {
                Api.deleteLocation(locationId)
            }
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    suspend fun getSessionLocations(eventId: Int) {
        try {
            Security.withAuth {
                val newLocations = Api.getSessionLocations(eventId)
                agendaStore.dispatch(AgendaAppAction.sessionLocationsLoaded(newLocations))
            }
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
        }
    }

    suspend fun googleAccountSynced(token: String) {
        Security.withAuth {
            saveLocalStorageToken(GOOGLE_ACCESS_TOKEN, token)
            agendaStore.dispatch(AgendaAppAction.googleAccountLinked)
        }
    }

    suspend fun microsoftAccountSynced(token: String) {
        Security.withAuth {
            saveLocalStorageToken(MICROSOFT_ACCESS_TOKEN, token)
            agendaStore.dispatch(AgendaAppAction.microsoftAccountLinked)
        }
    }

    suspend fun unlinkGoogleAccount() {
        Security.withAuth {
            deleteLocalStorageToken(GOOGLE_ACCESS_TOKEN)
            agendaStore.dispatch(AgendaAppAction.googleAccountUnlinked)
        }
    }

    suspend fun unlinkMicrosoftAccount() {
        Security.withAuth {
            deleteLocalStorageToken(MICROSOFT_ACCESS_TOKEN)
            agendaStore.dispatch(AgendaAppAction.microsoftAccountUnlinked)
        }
    }

    suspend fun readProfile() {
        val profile = Api.getCurrentProfile()
        agendaStore.dispatch(AgendaAppAction.profileLoaded(profile))
    }

    suspend fun registerProfile(profile: User): Boolean {
        try {
            Api.registerUser(profile)
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Boolean {
        try {
            Api.updatePassword(currentPassword, newPassword)
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    suspend fun userLogin(userCredentials: UserCredentials): Boolean {
        try {
            val user = Api.login(userCredentials)
            saveLocalStorageToken(JWT_TOKEN, user.token!!)
            agendaStore.dispatch(AgendaAppAction.profileLoaded(user))
            return true
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }

    fun logout() {
        deleteLocalStorageToken(JWT_TOKEN)
        Security.logout()
        agendaStore.dispatch(AgendaAppAction.logout)
        eventsInit()
    }

    fun getLocalStorageToken(tokenName: String): String? {
        return localStorage[tokenName]
    }

    fun saveLocalStorageToken(tokenName: String, token: String) {
        localStorage[tokenName] = token
    }

    private fun deleteLocalStorageToken(tokenName: String) {
        localStorage.removeItem(tokenName)
    }

    fun showErrorToast(message: String) {
        toastContainer.showToast(message, color = BsColor.DANGERBG)
    }

    fun showSuccessToast(message: String) {
        toastContainer.showToast(message, color = BsColor.SUCCESSBG)
    }

    fun passAccessTokenToMainWindow(token: String) {
        window.opener?.asDynamic().postMessage(token, "*")
        window.close()
    }

    suspend fun getRefreshedAccessToken(provider: Provider): Boolean {
        try {
            val token = Api.getRefreshedAccessToken(provider)
            if(token != null) {
                val tokenName = if(provider == Provider.GOOGLE) GOOGLE_ACCESS_TOKEN else MICROSOFT_ACCESS_TOKEN
                saveLocalStorageToken(tokenName, token)
                return true
            } else {
                return false
            }
        } catch (e: RemoteRequestException) {
            genericRequestExceptionHandler(e)
            return false
        }
    }
}
