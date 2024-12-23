package eventer.project.web

import eventer.project.AppScope
import eventer.project.components.addMinutesToJSDate
import eventer.project.components.addTimeToJSDate
import eventer.project.models.Session
import eventer.project.web.ConduitManager.GOOGLE_ACCESS_TOKEN
import eventer.project.web.ConduitManager.MICROSOFT_ACCESS_TOKEN
import eventer.project.web.ConduitManager.agendaStore
import eventer.project.web.ConduitManager.getLocalStorageToken
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.modal.Alert
import io.kvision.rest.HTTP_UNAUTHORIZED
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.MessageEvent
import org.w3c.fetch.RequestInit
import web.http.Headers
import kotlin.js.json

object ExternalCalendarSessionsManager {

    private var messageListener: ((dynamic) -> Unit)? = null
    val FRONTEND_URL = Api.processEnv.FRONTEND_URL as String? ?: "http://localhost:3000"

    private fun authorizeWindow(authURL: String, provider: Provider) {
        val windowWidth = 600
        val windowHeight = 700

        val left = (window.screenX + (window.innerWidth - windowWidth) / 2)
        val top = (window.screenY + (window.innerHeight - windowHeight) / 2)

        window.open(authURL, "_blank", "width=$windowWidth,height=$windowHeight,top=$top,left=$left")

        messageListener?.let { window.removeEventListener("message", it) }

        messageListener = { event ->
            val eventData = event.unsafeCast<MessageEvent>().data
            val token = eventData as? String
            if (!token.isNullOrEmpty()) {
                AppScope.launch {
                    if (provider == Provider.GOOGLE) {
                        ConduitManager.googleAccountSynced(token)
                    } else {
                        ConduitManager.microsoftAccountSynced(token)
                    }
                }
            }
        }

        window.addEventListener("message", messageListener)
    }

    fun googleCalendarAuthorize() {
        val authURL = "${Api.API_URL}/oauth/google/login?redirectUrl=${FRONTEND_URL}/google-oauth-logged"
        authorizeWindow(authURL, Provider.GOOGLE)
    }

    fun microsoftOutlookAuthorize() {
        val authURL = "${Api.API_URL}/oauth/microsoft/login?redirectUrl=${FRONTEND_URL}/microsoft-oauth-logged"
        authorizeWindow(authURL, Provider.MICROSOFT)
    }

    suspend fun sendEventSessionsToExternalCalendar(provider: Provider): Boolean {
        val currentSessions = agendaStore.store.state.selectedEventSessions
        val eventName = agendaStore.store.state.selectedEvent?.name!!

        if (!isAccessTokenValid(provider)) {
            if(!ConduitManager.getRefreshedAccessToken(provider)) {
                Alert.show(text = tr("Problem with access to the service occured," +
                        " please log in the service again.")
                )
                return false
            }
        }

        if (currentSessions != null) {
            currentSessions.forEach { session ->
                try {
                    sendSessionToExternalCalendar(session, eventName, provider)
                } catch (e: Exception){
                    console.log(e)
                    throw e
                }
            }
            return true
        } else return false
    }

    private suspend fun sendSessionToExternalCalendar(session: Session, eventName: String, provider: Provider) {
        val googleCalendarApiUrl = "https://www.googleapis.com/calendar/v3/calendars/primary/events"
        val microsoftOutlookApiUrl = "https://graph.microsoft.com/v1.0/me/events"

        val apiUrl = if (provider == Provider.GOOGLE) googleCalendarApiUrl else microsoftOutlookApiUrl

        val headers = Headers()
        val token = if (provider == Provider.GOOGLE) getLocalStorageToken(GOOGLE_ACCESS_TOKEN) else getLocalStorageToken(
            MICROSOFT_ACCESS_TOKEN
        )
        headers.append("Authorization", "Bearer $token")
        headers.append("Content-Type", "application/json")


        val eventBody = JSON.stringify(
            if (provider == Provider.GOOGLE) buildGoogleCalendarEventJson(session, eventName)
            else buildMicrosoftEventJson(session, eventName)
        )

        val response = window.fetch(apiUrl, RequestInit(
            method = "POST",
            headers = headers,
            body = eventBody
        )
        ).await()

        if (response.ok) {
            console.log("Session '${session.name}' added successfully!")
        } else {
            console.log("Failed to add session '${session.name}': ${response.statusText}")
            throw Exception("Failed to add session")
        }
    }

    private fun buildGoogleCalendarEventJson(session: Session, eventName: String): dynamic {
        val startDateTime = addTimeToJSDate(session.date!!, session.startTime!!)
        val endDateTime = addMinutesToJSDate(startDateTime, session.duration!!)
        return json(
            "summary" to "$eventName: ${session.name}",
            "description" to session.description,
            "location" to session.location?.name,
            "start" to json(
                "dateTime" to startDateTime.toISOString(),
                "timeZone" to "Europe/Prague"
            ),
            "end" to json(
                "dateTime" to endDateTime.toISOString(),
                "timeZone" to "Europe/Prague"
            )
        )
    }

    private fun buildMicrosoftEventJson(session: Session, eventName: String): dynamic {
        val startDateTime = addTimeToJSDate(session.date!!, session.startTime!!)
        val endDateTime = addMinutesToJSDate(startDateTime, session.duration!!)
        return json(
            "subject" to "$eventName: ${session.name}",
            "body" to json(
                "contentType" to "HTML",
                "content" to session.description
            ),
            "start" to json(
                "dateTime" to startDateTime.toISOString(),
                "timeZone" to "Europe/Prague"
            ),
            "end" to json(
                "dateTime" to endDateTime.toISOString(),
                "timeZone" to "Europe/Prague"
            ),
            "location" to json(
                "displayName" to session.location?.name
            )
        )
    }

    private suspend fun isAccessTokenValid(provider: Provider): Boolean {
        val testUrl: String
        if (provider == Provider.GOOGLE){
            testUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo"
        } else {
            testUrl = "https://graph.microsoft.com/v1.0/me"
        }

        val headers = Headers()
        val token: String?
        if (provider == Provider.GOOGLE){
            token = getLocalStorageToken(GOOGLE_ACCESS_TOKEN)
        } else {
            token = getLocalStorageToken(MICROSOFT_ACCESS_TOKEN)
        }

        headers.append("Authorization", "Bearer $token")

        val response = window.fetch(testUrl, RequestInit(
            method = "GET",
            headers = headers
        )).await()

        return response.ok
    }
}