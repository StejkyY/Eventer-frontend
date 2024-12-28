package eventer.project.layout.windows

import eventer.project.helpers.AgendaPrimaryButton
import eventer.project.AppScope
import eventer.project.helpers.withProgress
import eventer.project.state.AgendaAppAction
import eventer.project.web.ConduitManager
import eventer.project.helpers.ExternalCalendarSessionsManager
import eventer.project.web.Provider
import io.kvision.core.AlignItems
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.Label
import io.kvision.i18n.tr
import io.kvision.modal.Modal
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.state.bind
import io.kvision.utils.px
import kotlinx.coroutines.launch

class AddToCalendarWindow : Modal(caption = tr("Add sessions to calendar")) {
    private lateinit var buttonGoogleCalendarLogin: Button
    private lateinit var buttonOutlookLogin: Button
    private lateinit var buttonAddToGoogleCalendar: Button
    private lateinit var buttonAddToOutlookCalendar: Button
    private lateinit var buttonUnlinkGoogleAccount: Button
    private lateinit var buttonUnlinkMicrosoftAccount: Button
    private lateinit var statusLoggedGoogle: Button
    private lateinit var statusLoggedMicrosoft: Button

    init {
        buttonsInitialization()
        vPanel().bind(ConduitManager.agendaStore) { state ->
            hPanel() {
                alignItems = AlignItems.CENTER
                add(Label(tr("Google Calendar:"), className = "third-party-calendar-label"))
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
                add(Label(tr("Microsoft Outlook:"), className = "third-party-calendar-label"))
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

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        statusLoggedGoogle = Button(tr("Synced"), style = ButtonStyle.SUCCESS) {
            disabled = true
        }
        statusLoggedMicrosoft = Button(tr("Synced"), style = ButtonStyle.SUCCESS) {
            disabled = true
        }
        buttonAddToGoogleCalendar = AgendaPrimaryButton(tr("Add sessions"), buttonClassName = "third-party-calendar-button") {
            onClick {
                try {
                    AppScope.withProgress {
                        disabled = true
                        if(ExternalCalendarSessionsManager.sendEventSessionsToExternalCalendar(Provider.GOOGLE)) {
                            ConduitManager.showSuccessToast(tr("Sessions were added succesfully to your calendar."))
                        }
                        disabled = false
                    }
                } catch (e: Exception) {
                    ConduitManager.showErrorToast(tr("Failed adding sessions to calendar."))
                    disabled = false
                }
            }
        }
        buttonAddToOutlookCalendar = AgendaPrimaryButton(tr("Add sessions"), buttonClassName = "third-party-calendar-button") {
            onClick {
                try {
                    AppScope.withProgress {
                        disabled = true
                        if(ExternalCalendarSessionsManager.sendEventSessionsToExternalCalendar(Provider.MICROSOFT)) {
                            ConduitManager.showSuccessToast(tr("Sessions were added succesfully to your calendar."))
                        }
                        disabled = false
                    }
                } catch (e: Exception) {
                    disabled = false
                    ConduitManager.showErrorToast(tr("Failed adding sessions to calendar."))
                }
            }
        }
        buttonGoogleCalendarLogin = AgendaPrimaryButton(tr("Login"), buttonClassName = "third-party-calendar-button") {
            onClick {
                ExternalCalendarSessionsManager.googleCalendarAuthorize()
            }
        }
        buttonOutlookLogin = AgendaPrimaryButton(tr("Login"), buttonClassName = "third-party-calendar-button") {
            onClick {
                ExternalCalendarSessionsManager.microsoftOutlookAuthorize()
            }
        }

        buttonUnlinkGoogleAccount = Button(
            tr("Unlink"),
            style = ButtonStyle.DANGER,
            className = "third-party-calendar-button") {
            onClick {
                AppScope.launch {
                    ConduitManager.unlinkGoogleAccount()
                }
            }
        }
        buttonUnlinkMicrosoftAccount = Button(
            tr("Unlink"),
            style = ButtonStyle.DANGER,
            className = "third-party-calendar-button") {
            onClick {
                AppScope.launch {
                    ConduitManager.unlinkMicrosoftAccount()
                }
            }
        }
    }

    /**
     * Opens the modal window.
     */
    fun open() {
        checkAccountsSynced()
        show()
    }

    /**
     * Checks the synchronization status of linked accounts.
     */
    fun checkAccountsSynced() {
        if(ConduitManager.getLocalStorageToken(ConduitManager.GOOGLE_ACCESS_TOKEN) != null &&
            !ConduitManager.agendaStore.getState().googleAccountSynced) {
            ConduitManager.agendaStore.dispatch(AgendaAppAction.googleAccountLinked)
        }
        if(ConduitManager.getLocalStorageToken(ConduitManager.MICROSOFT_ACCESS_TOKEN) != null &&
            !ConduitManager.agendaStore.getState().microsoftAccountSynced) {
            ConduitManager.agendaStore.dispatch(AgendaAppAction.microsoftAccountLinked)
        }
    }

}