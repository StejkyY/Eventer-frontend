package eventer.project.web

import eventer.project.AppScope
import io.kvision.navigo.Navigo
import io.kvision.routing.Routing
import io.kvision.routing.Strategy
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlin.js.RegExp

enum class View (val url: String) {
    EVENTS("/events"),
    LOGIN("/auth/login"),
    REGISTER("/auth/register"),
    PROFILE("/profile"),
    NEW_EVENT("/new-event"),
    EVENT_BASIC_INFO("/info"),
    EVENT_DESCRIPTION("/description"),
    EVENT_AGENDA("/agenda"),
    EVENT_PREVIEW("/preview");
}

object RoutingManager {
    lateinit var routing: Routing

    fun init() {
        routing = Routing("/", useHash = false, strategy = Strategy.ALL)
        routing.routing()
        routing.resolve()
    }

    fun updatePageLinks() {
        if (::routing.isInitialized) {
            routing.updatePageLinks()
        }
    }

    private fun Routing.routing() {
        routing.on("/", { _ ->
            ConduitManager.showEventsPage()
        }).on(View.EVENTS.url, { _ ->
            ConduitManager.showEventsPage()
        }).on(View.NEW_EVENT.url, { _ ->
            ConduitManager.showNewEventPage()
        }).on(View.LOGIN.url, { _ ->
            ConduitManager.showLoginPage()
        }).on(View.REGISTER.url, { _ ->
            ConduitManager.showRegisterPage()
        }).on(View.PROFILE.url, { _ ->
            ConduitManager.showProfilePage()
        }).on("/event/:id${View.EVENT_PREVIEW.url}", { match ->
            ConduitManager.showEventPreviewPage(match.data["id"].toString().toInt())
        }).on("/event/:id${View.EVENT_BASIC_INFO.url}", { match ->
            ConduitManager.showEventBasicInfoPage(match.data["id"].toString().toInt())
        }).on("/event/:id${View.EVENT_DESCRIPTION.url}", { match ->
            ConduitManager.showEventDescriptionPage(match.data["id"].toString().toInt())
        }).on("/event/:id${View.EVENT_AGENDA.url}", { match ->
            ConduitManager.showEventAgendaPage(match.data["id"].toString().toInt())
        }).on("/google-oauth-logged", { _ ->
            extractAccessTokenFromWindowURL()
        }).on("/microsoft-oauth-logged", { _ ->
            extractAccessTokenFromWindowURL()
        }).on("/logout", { _ ->
            ConduitManager.logout()
        })
    }

    fun extractAccessTokenFromWindowURL() {
        val hash = window.location.hash
        val accessToken = hash.substringAfter("access_token=").substringBefore("&")
        if (accessToken.isNotEmpty()) {
            AppScope.launch {
                ConduitManager.passAccessTokenToMainWindow(accessToken)
            }
        }
    }

    fun redirect(view: View) {
        routing.navigate(view.url)
    }

    fun redirect(url: String) {
        routing.navigate(url)
    }
}