package eventer.project.layout

import eventer.project.AppScope
import eventer.project.helpers.*
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.View
import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.i18n.tr
import io.kvision.panel.*
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vh
import io.kvision.utils.vw
import kotlinx.browser.window

class EventPanel(val state: AgendaAppState, val childPanel: EventChildPanel) : SimplePanel() {
    private lateinit var buttonBasicInfo: MenuTextButton
    private lateinit var buttonDescription: MenuTextButton
    private lateinit var buttonAgenda: MenuTextButton
    private lateinit var eventPreviewButton: Button
    private val unsavedChangesConfirmWindow: UnsavedChangesConfirm = UnsavedChangesConfirm()
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    init {
        buttonsInitialization()

        hPanel {
            paddingTop = 20.px
            height = 100.perc
            vPanel(className = "event-edit-menu") {
                spacing = 20
                add(backButton)
                add(buttonBasicInfo)
                add(buttonDescription)
                add(buttonAgenda)
            }
            vPanel(className = "event-edit-header") {
                gridPanel (
                    templateColumns = "1fr 1fr 1fr",
                    alignItems = AlignItems.CENTER,
                    justifyItems = JustifyItems.CENTER
                ) {

                    add(hPanel {
                        add(eventPreviewButton)
                    }, 1, 1)

                    if(state.selectedEvent != null) {
                        add(Label(state.selectedEvent.name, className = "main-label"), 2, 1)
                    }

                    add(saveButton,3, 1)
                    paddingTop = 15.px
                    paddingBottom = 15.px
                }

                hPanel(className = "separator"){}
                simplePanel {
                    height = 75.vh
                    add(childPanel)
                }
            }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        saveButton = AgendaPrimaryButton(tr("Save changes")) {
            disabled = true
            onClick {
                save()
            }
        }

        backButton = AgendaIconButton("fas fa-arrow-left") {
            onClick {
                if(saveButton.disabled) {
                    RoutingManager.redirect(View.EVENTS)
                } else {
                    unsavedChangesConfirmWindow.show(tr("You have unsaved changes, are you sure you want to leave?"),
                        yesCallback = { RoutingManager.redirect(View.EVENTS)})
                }
            }
        }

        eventPreviewButton = AgendaPrimaryButton(tr("Preview")) {
            onClick {
                if(!childPanel.changesMade) {
                    val previewUrl = "/event/${state.selectedEvent?.id}${View.EVENT_PREVIEW.url}"
                    setAttribute("href", previewUrl)
                    window.open(previewUrl, "_blank")?.focus()
                } else {
                    ConduitManager.showErrorToast(tr("Changes need to be saved first."))
                }
            }
        }

        childPanel.setSaveButton(saveButton)

        buttonBasicInfo = MenuTextButton(tr("Basic info"))
        buttonDescription = MenuTextButton(tr("Description"))
        buttonAgenda = MenuTextButton(tr("Agenda"))

        disableSelectedButton()
        buttonBasicInfo.onClick {
            if(!childPanel.changesMade) {
                goToBasicInfoPanel()
            }  else {
                unsavedChangesConfirmWindow.show(
                    tr("You have unsaved changes, do you want to leave the page?"),
                    yesCallback = { goToBasicInfoPanel() })
            }
        }
        buttonDescription.onClick {
            if(!childPanel.changesMade) {
                goToDescriptionPanel()
            } else {
                unsavedChangesConfirmWindow.show(
                    tr("You have unsaved changes, do you want to leave the page?"),
                    yesCallback = { goToDescriptionPanel() })
            }
        }
        buttonAgenda.onClick {
            if (!childPanel.changesMade) {
                goToAgendaPanel()
            } else {
                unsavedChangesConfirmWindow.show(
                    tr("You have unsaved changes, do you want to leave the page?"),
                    yesCallback = { goToAgendaPanel() })
            }
        }
    }

    /**
     * Disables selected button from the menu for switching between child panels.
     */
    private fun disableSelectedButton() {
        if(state.view == View.EVENT_BASIC_INFO) {
            buttonBasicInfo.disable()
            buttonAgenda.enable()
            buttonDescription.enable()
        } else if (state.view == View.EVENT_DESCRIPTION) {
            buttonDescription.disable()
            buttonBasicInfo.enable()
            buttonAgenda.enable()
        } else {
            buttonAgenda.disable()
            buttonBasicInfo.enable()
            buttonDescription.enable()
        }
    }

    /**
     * Redirects to the panel with basic info about the event.
     */
    private fun goToBasicInfoPanel() {
        RoutingManager.redirect("/event/${ConduitManager.agendaStore.getState().selectedEvent?.id}" +
                View.EVENT_BASIC_INFO.url
        )
    }

    /**
     * Redirects to the panel with event's description.
     */
    private fun goToDescriptionPanel() {
        RoutingManager.redirect("/event/${ConduitManager.agendaStore.getState().selectedEvent?.id}" +
                View.EVENT_DESCRIPTION.url
        )
    }

    /**
     * Redirects to the panel with event's agenda.
     */
    private fun goToAgendaPanel() {
        RoutingManager.redirect("/event/${ConduitManager.agendaStore.getState().selectedEvent?.id}" +
                View.EVENT_AGENDA.url
        )
    }

    private fun save() {
        AppScope.withProgress {
            if(childPanel.validate() && childPanel.save()) {
                saveButton.disabled = true
                ConduitManager.showSuccessToast(tr("Event changes were saved succesfully."))
            }
        }
    }
}