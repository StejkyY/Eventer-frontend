package eventer.project.web

import eventer.project.web.ConduitManager.JWT_TOKEN
import io.kvision.core.StringPair
import eventer.project.AppScope
import eventer.project.Security
import eventer.project.components.addMinutesToJSDate
import eventer.project.components.addTimeToJSDate
import eventer.project.models.*
import eventer.project.models.dto.*
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import kotlinx.serialization.UseContextualSerialization


import eventer.project.state.AgendaAppAction
import eventer.project.state.AgendaAppState
import eventer.project.state.agendaAppReducer
import eventer.project.web.ConduitManager.agendaStore
import io.kvision.navigo.Navigo
import io.kvision.redux.createTypedReduxStore
import io.kvision.remote.SecurityException
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
import kotlin.js.Promise
import kotlin.js.json

object Api {

    val API_URL: String = js("process.env.API_URL") as String? ?: "http://localhost:8080"
    private val restClient = RestClient()

    private fun authRequest(): List<StringPair> {
        return ConduitManager.getLocalStorageToken(JWT_TOKEN)?.let {
            listOf("Authorization" to "Bearer $it")
        } ?: emptyList()
    }

    suspend fun getEvent(eventId: Int): Event {
        return restClient.call<EventDTO>("$API_URL/events/$eventId") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().event
    }

    suspend fun getEventsList(): List<Event> {
        return restClient.call<EventsDTO>("$API_URL/events") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().events
    }

    suspend fun getEventRoles(): List<EventRole> {
        return restClient.call<EventRolesDTO>("$API_URL/events/roles") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().eventRoles
    }

    suspend fun getEventSessions(eventId: Int): List<Session> {
        return restClient.call<SessionsDTO>("$API_URL/events/$eventId/sessions") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().sessions
    }

    suspend fun addEvent(event: Event): Event {
        return restClient.post<EventDTO, EventDTO>("$API_URL/events",
            EventDTO(event)) {
            headers = ::authRequest
        }.await().event
    }

    suspend fun updateEvent(event: Event): Event {
        return restClient.call<EventDTO, EventDTO>("$API_URL/events/${event.id}",
            EventDTO(event)) {
            method = HttpMethod.PUT
            headers = ::authRequest
        }.await().event
    }

    suspend fun deleteEvent(eventId: Int) {
        restClient.requestDynamic("$API_URL/events/$eventId") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }

    suspend fun saveEventAgendaSessions(eventId: Int,
                                          addedSessions: List<Session>,
                                          updatedSessions: List<Session>,
                                          deletedSessions: List<Session>) {
        restClient.requestDynamic("$API_URL/events/$eventId/sessions",
            EventAgendaSessionsDTO(
                addedSessions,
                updatedSessions,
                deletedSessions
            )) {
            method = HttpMethod.POST
            headers = ::authRequest
        }.await()
    }

    suspend fun addSession(session: Session) {
        restClient.post<SessionDTO, SessionDTO>("$API_URL/sessions",
            SessionDTO(session)){
            headers = ::authRequest
        }.await()
    }

    suspend fun updateSession(session: Session) {
        restClient.call<SessionDTO, SessionDTO>("$API_URL/sessions/${session.id}",
            SessionDTO(session)) {
            method = HttpMethod.PUT
            headers = ::authRequest
        }.await()
    }

    suspend fun deleteSession(sessionId: Int) {
        restClient.requestDynamic("$API_URL/sessions/$sessionId") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }

    suspend fun addType(type: Type): Type {
        return restClient.post<TypeDTO, TypeDTO>("$API_URL/types", TypeDTO(type)){
            headers = ::authRequest
        }.await().type
    }

    suspend fun deleteType(typeId: Int) {
        restClient.requestDynamic("$API_URL/types/$typeId") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }

    suspend fun getSessionTypes(): List<Type> {
        return restClient.call<TypesDTO>("$API_URL/types") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().types
    }

    suspend fun addLocation(location: Location): Location {
        return restClient.post<LocationDTO, LocationDTO>("$API_URL/locations", LocationDTO(location)){
            headers = ::authRequest
        }.await().location
    }

    suspend fun deleteLocation(locationId: Int) {
        restClient.requestDynamic("$API_URL/locations/$locationId") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }

    suspend fun getSessionLocations(eventId: Int): List<Location> {
        return restClient.call<LocationsDTO>("$API_URL/events/$eventId/locations") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().locations
    }

    suspend fun getRefreshedAccessToken(provider: Provider): String? {
        val provider = provider.toString().lowercase()
       val response = restClient.requestDynamic("$API_URL/oauth/$provider/token-refresh") {
            method = HttpMethod.GET
            headers = ::authRequest
       }.await()
        val responseJson = response.unsafeCast<Map<String, Any>>()
        return responseJson["access_token"] as? String
    }

    suspend fun login(userCredentials: UserCredentials): User {
        return restClient.post<UserDTO, UserDTO>(
            "$API_URL/auth/login",
            UserDTO(
                User(
                    email = userCredentials.email,
                    password = userCredentials.password
                )
            )
        ){
            headers = ::authRequest
        }.await().user
    }

    suspend fun registerUser(user: User) {
        restClient.post<UserDTO, UserDTO>("$API_URL/auth/register",
            UserDTO (
                user
            )) {
        }.await()
    }

    suspend fun getCurrentProfile(): User {
        return restClient.call<UserDTO>("$API_URL/users/current") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().user
    }


    suspend fun updateProfile(profile: User): User {
        return restClient.call<UserDTO, UserDTO>("$API_URL/users/${profile.id}",
            UserDTO(profile)) {
            method = HttpMethod.PUT
            headers = ::authRequest
        }.await().user
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String)  {
//        restClient.post<UserPasswordDTO, UserPasswordDTO>("$API_URL/users/current/change-password",
//            UserPasswordDTO(
//                UserPasswordChange(currentPassword, newPassword)
//            )) {
//            method = HttpMethod.POST
//            headers = ::authRequest
//        }.await()
        restClient.requestDynamic("$API_URL/users/current/change-password",
            UserPasswordDTO(
                UserPasswordChange(currentPassword, newPassword)
            )) {
            method = HttpMethod.POST
            headers = ::authRequest
        }.await()
    }

    suspend fun deleteProfile() {
        restClient.requestDynamic("$API_URL/users/current") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }
}