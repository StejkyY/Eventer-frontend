package eventer.project.layout.windows

import eventer.project.components.AgendaPrimaryButton
import eventer.project.AppScope
import eventer.project.state.AgendaAppAction
import eventer.project.web.ConduitManager
import eventer.project.web.Provider
import io.kvision.core.AlignItems
import io.kvision.core.BsColor
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.Label
import io.kvision.i18n.tr
import io.kvision.modal.Modal
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.state.bind
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.dom.MessageEvent
import web.timers.Interval
import web.timers.clearInterval
import web.timers.setInterval

class AddToCalendarWindow : Modal(caption = "Add sessions to calendar") {
    private val buttonGoogleCalendarLogin: Button
    private val buttonOutlookLogin: Button
    private var buttonAddToGoogleCalendar: Button
    private var buttonAddToOutlookCalendar: Button
    private var buttonUnlinkGoogleAccount: Button
    private var buttonUnlinkMicrosoftAccount: Button
    private var statusLoggedGoogle: Button
    private var statusLoggedMicrosoft: Button

    init {
        statusLoggedGoogle = Button("Logged", style = ButtonStyle.SUCCESS) {
            disabled = true
        }
        statusLoggedMicrosoft = Button("Logged", style = ButtonStyle.SUCCESS) {
            disabled = true
        }
        buttonAddToGoogleCalendar = AgendaPrimaryButton(tr("Add sessions")) {
            marginLeft = 5.px
            onClick {
                try {
                    AppScope.launch {
                        if(!ConduitManager.sendSessionsToExternalCalendar(Provider.GOOGLE)) {
                            ConduitManager.showErrorToast(tr("Calendar is empty."))
                        }
                        ConduitManager.showSuccessToast(tr("Sessions were added succesfully to your calendar."))
                    }
                } catch (e: Exception) {
                    ConduitManager.showErrorToast(tr("Failed adding sessions to calendar."))
                }
            }
        }
        buttonAddToOutlookCalendar = AgendaPrimaryButton(tr("Add sessions")) {
            marginLeft = 5.px
            onClick {
                try {
                    AppScope.launch {
                        ConduitManager.sendSessionsToExternalCalendar(Provider.MICROSOFT)
                        ConduitManager.showSuccessToast(tr("Sessions were added succesfully to your calendar."))
                    }
                } catch (e: Exception) {
                    ConduitManager.showErrorToast(tr("Failed adding sessions to calendar."))
                }
            }
        }
        buttonGoogleCalendarLogin = AgendaPrimaryButton(tr("Login")) {
            marginLeft = 5.px
            onClick {
                googleCalendarAuthorize()
            }
        }
        buttonOutlookLogin = AgendaPrimaryButton(tr("Login")) {
            marginLeft = 5.px
            onClick {
                microsoftOutlookAuthorize()
            }
        }

        buttonUnlinkGoogleAccount = Button(io.kvision.i18n.tr("Unlink"), style = ButtonStyle.DANGER) {
            marginLeft = 5.px
            onClick {
                AppScope.launch {
                    ConduitManager.unlinkGoogleAccount()
                }
            }
        }
        buttonUnlinkMicrosoftAccount = Button(io.kvision.i18n.tr("Unlink"), style = ButtonStyle.DANGER) {
            marginLeft = 5.px
            onClick {
                AppScope.launch {
                    ConduitManager.unlinkMicrosoftAccount()
                }
            }
        }

        vPanel().bind(ConduitManager.agendaStore) { state ->
            hPanel() {
                alignItems = AlignItems.CENTER
                add(Label("Google Calendar:") {
                    width = 150.px
                })
                if(state.googleAccountSynced) {
                    add(statusLoggedGoogle)
                    add(buttonAddToGoogleCalendar)
                    add(buttonUnlinkGoogleAccount)
                } else {
                    add(buttonGoogleCalendarLogin)
                }
            }
            hPanel() {
                alignItems = AlignItems.CENTER
                marginTop = 10.px
                add(Label("Microsoft Outlook:") {
                    width = 150.px
                })
                if(state.microsoftAccountSynced) {
                    add(statusLoggedMicrosoft)
                    add(buttonAddToOutlookCalendar)
                    add(buttonUnlinkMicrosoftAccount)
                } else {
                    add(buttonOutlookLogin)
                }
            }
        }
    }

    fun open() {
        checkAccountsSynced()
        show()
    }

    fun checkAccountsSynced() {
        if(ConduitManager.getLocalStorageToken(ConduitManager.GOOGLE_ACCESS_TOKEN) != null &&
            !ConduitManager.agendaStore.getState().googleAccountSynced) {
            ConduitManager.agendaStore.dispatch(AgendaAppAction.googleAccountLinked)
        }
        if(ConduitManager.getLocalStorageToken(ConduitManager.MICROSOFT_ACCESS_TOKEN) != null &&
            !ConduitManager.agendaStore.getState().microsoftAccountSynced) {
            ConduitManager.agendaStore.dispatch(AgendaAppAction.googleAccountLinked)
        }
    }

    private var messageListener: ((dynamic) -> Unit)? = null

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

    private fun googleCalendarAuthorize() {
        val authURL = "http://localhost:8080/oauth/google/login?redirectUrl=http://localhost:3000/google-oauth-logged"
        authorizeWindow(authURL, Provider.GOOGLE)
    }

    private fun microsoftOutlookAuthorize() {
        val authURL = "http://localhost:8080/oauth/microsoft/login?redirectUrl=http://localhost:3000/microsoft-oauth-logged"
        authorizeWindow(authURL, Provider.MICROSOFT)
    }

}