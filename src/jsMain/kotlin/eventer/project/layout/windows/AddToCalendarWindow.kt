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
    private val buttonGoogleCalendarLogin: Button
    private val buttonOutlookLogin: Button
    private var buttonAddToGoogleCalendar: Button
    private var buttonAddToOutlookCalendar: Button
    private var buttonUnlinkGoogleAccount: Button
    private var buttonUnlinkMicrosoftAccount: Button
    private var statusLoggedGoogle: Button
    private var statusLoggedMicrosoft: Button

    init {
        statusLoggedGoogle = Button(tr("Synced"), style = ButtonStyle.SUCCESS) {
            disabled = true
        }
        statusLoggedMicrosoft = Button(tr("Synced"), style = ButtonStyle.SUCCESS) {
            disabled = true
        }
        buttonAddToGoogleCalendar = AgendaPrimaryButton(tr("Add sessions")) {
            marginLeft = 5.px
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
        buttonAddToOutlookCalendar = AgendaPrimaryButton(tr("Add sessions")) {
            marginLeft = 5.px
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
        buttonGoogleCalendarLogin = AgendaPrimaryButton(tr("Login")) {
            marginLeft = 5.px
            onClick {
                ExternalCalendarSessionsManager.googleCalendarAuthorize()
            }
        }
        buttonOutlookLogin = AgendaPrimaryButton(tr("Login")) {
            marginLeft = 5.px
            onClick {
                ExternalCalendarSessionsManager.microsoftOutlookAuthorize()
            }
        }

        buttonUnlinkGoogleAccount = Button(tr("Unlink"), style = ButtonStyle.DANGER) {
            marginLeft = 5.px
            onClick {
                AppScope.launch {
                    ConduitManager.unlinkGoogleAccount()
                }
            }
        }
        buttonUnlinkMicrosoftAccount = Button(tr("Unlink"), style = ButtonStyle.DANGER) {
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
                add(Label(tr("Google Calendar:")) {
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
                add(Label(tr("Microsoft Outlook:")) {
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