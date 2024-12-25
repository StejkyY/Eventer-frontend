package eventer.project.web

import eventer.project.web.ConduitManager.JWT_TOKEN
import io.kvision.core.StringPair
import eventer.project.models.*
import eventer.project.models.dto.*


import io.kvision.rest.*
import kotlinx.coroutines.await

object Api {

    // Fetches environment variables and extracts backend API URL or defaults to localhost
    val processEnv = js("PROCESS_ENV")
    val API_URL = processEnv.API_URL as String? ?: "http://localhost:8080"
    private val restClient = RestClient()

    /**
     * Generates an authentication header for API requests using the JWT token stored in browser local storage.
     */
    private fun authRequest(): List<StringPair> {
        return ConduitManager.getLocalStorageToken(JWT_TOKEN)?.let {
            listOf("Authorization" to "Bearer $it")
        } ?: emptyList()
    }

    /**
     * Retrieves event by its ID from the backend.
     */
    suspend fun getEvent(eventId: Int): Event {
        return restClient.call<EventDTO>("$API_URL/events/$eventId") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().event
    }

    /**
     * Retrieves all events created by the user from the backend.
     */
    suspend fun getUserEventsList(): List<Event> {
        return restClient.call<EventsDTO>("$API_URL/events") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().events
    }

    /**
     * Retrieves all event roles from the backend.
     */
    suspend fun getEventRoles(): List<EventRole> {
        return restClient.call<EventRolesDTO>("$API_URL/events/roles") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().eventRoles
    }

    /**
     * Retrieves all session for an event by its ID from the backend.
     */
    suspend fun getEventSessions(eventId: Int): List<Session> {
        return restClient.call<SessionsDTO>("$API_URL/events/$eventId/sessions") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().sessions
    }

    /**
     * Adds new event to the backend.
     */
    suspend fun addEvent(event: Event): Event {
        return restClient.post<EventDTO, EventDTO>("$API_URL/events",
            EventDTO(event)) {
            headers = ::authRequest
        }.await().event
    }

    /**
     * Updates an event in the backend.
     */
    suspend fun updateEvent(event: Event): Event {
        return restClient.call<EventDTO, EventDTO>("$API_URL/events/${event.id}",
            EventDTO(event)) {
            method = HttpMethod.PUT
            headers = ::authRequest
        }.await().event
    }

    /**
     * Deletes event by its ID in the backend.
     */
    suspend fun deleteEvent(eventId: Int) {
        restClient.requestDynamic("$API_URL/events/$eventId") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }

    /**
     * Saves sessions from the event agenda by event ID.
     * Sends added sessions, updated sessions and deleted sessions from the event agenda
     */
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

    /**
     * Adds new session type to the backend.
     */
    suspend fun addType(type: Type): Type {
        return restClient.post<TypeDTO, TypeDTO>("$API_URL/types", TypeDTO(type)){
            headers = ::authRequest
        }.await().type
    }

    /**
     * Delete session type by its ID in the backend.
     */
    suspend fun deleteType(typeId: Int) {
        restClient.requestDynamic("$API_URL/types/$typeId") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }

    /**
     * Retrieves all session types from the backend (custom + default).
     */
    suspend fun getSessionTypes(): List<Type> {
        return restClient.call<TypesDTO>("$API_URL/types") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().types
    }

    /**
     * Add a new session location to the backend.
     */
    suspend fun addLocation(location: Location): Location {
        return restClient.post<LocationDTO, LocationDTO>("$API_URL/locations", LocationDTO(location)){
            headers = ::authRequest
        }.await().location
    }

    /**
     * Delete session location in the backend.
     */
    suspend fun deleteLocation(locationId: Int) {
        restClient.requestDynamic("$API_URL/locations/$locationId") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }

    /**
     * Adds new event to the backend.
     */
    suspend fun getSessionLocations(eventId: Int): List<Location> {
        return restClient.call<LocationsDTO>("$API_URL/events/$eventId/locations") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().locations
    }

    /**
     * Retreives a new access token for the specified provider from the backend.
     */
    suspend fun getRefreshedAccessToken(provider: Provider): String? {
        val provider = provider.toString().lowercase()
       val response = restClient.requestDynamic("$API_URL/oauth/$provider/token-refresh") {
            method = HttpMethod.GET
            headers = ::authRequest
       }.await()
        val responseJson = response.unsafeCast<Map<String, Any>>()
        return responseJson["access_token"] as? String
    }

    /**
     * Sends user credentials for a user login to the backend.
     */
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

    /**
     * Sends a new user for a register to the backend.
     */
    suspend fun registerUser(user: User) {
        restClient.post<UserDTO, UserDTO>("$API_URL/auth/register",
            UserDTO (
                user
            )) {
        }.await()
    }

    /**
     * Retrieves currently authenticated user from the backend.
     */
    suspend fun getCurrentProfile(): User {
        return restClient.call<UserDTO>("$API_URL/users/current") {
            method = HttpMethod.GET
            headers = ::authRequest
        }.await().user
    }

    /**
     * Sends user for updating to the backend.
     */
    suspend fun updateProfile(profile: User): User {
        return restClient.call<UserDTO, UserDTO>("$API_URL/users/${profile.id}",
            UserDTO(profile)) {
            method = HttpMethod.PUT
            headers = ::authRequest
        }.await().user
    }

    /**
     * Sends new and current password for a password change to the backend.
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String)  {
        restClient.requestDynamic("$API_URL/users/current/change-password",
            UserPasswordDTO(
                UserPasswordChange(currentPassword, newPassword)
            )) {
            method = HttpMethod.POST
            headers = ::authRequest
        }.await()
    }

    /**
     * Deletes currently authenticated user in the backend.
     */
    suspend fun deleteProfile() {
        restClient.requestDynamic("$API_URL/users/current") {
            method = HttpMethod.DELETE
            headers = ::authRequest
        }.await()
    }
}