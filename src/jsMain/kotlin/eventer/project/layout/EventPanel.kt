package eventer.project.layout

import eventer.project.AppScope
import eventer.project.components.AgendaIconButton
import eventer.project.components.AgendaPrimaryButton
import eventer.project.components.MenuTextButton
import eventer.project.components.UnsavedChangesConfirm
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.View
import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.panel.*
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vh
import io.kvision.utils.vw
import kotlinx.coroutines.launch

class EventPanel(val state: AgendaAppState, val childPanel: EventChildPanel) : SimplePanel() {
    private val buttonBasicInfo: MenuTextButton
    private val buttonDescription: MenuTextButton
    private val buttonAgenda: MenuTextButton
    private val eventPreviewButton: Button
    private val unsavedChangesConfirmWindow: UnsavedChangesConfirm = UnsavedChangesConfirm()
    private val saveButton: Button
    private val backButton: Button

    init {
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
                    RoutingManager.redirect("/event/${state.selectedEvent?.id}${View.EVENT_PREVIEW.url}")
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

        hPanel {
            paddingTop = 20.px
            height = 100.perc
            vPanel {
                width = 13.vw
                borderTop = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                borderBottom = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                spacing = 20
                paddingTop = 20.px
                add(backButton)
                add(buttonBasicInfo)
                add(buttonDescription)
                add(buttonAgenda)
            }
            vPanel {
                width = 85.vw
                border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                gridPanel (
                    templateColumns = "1fr 1fr 1fr", alignItems = AlignItems.CENTER, justifyItems = JustifyItems.CENTER
                ) {

                    add(hPanel {
                        add(eventPreviewButton)
                    }, 1, 1)

                    if(state.selectedEvent != null) {
                        add(Label(state.selectedEvent.name) {
                            fontSize = 28.px
                        }, 2, 1)
                    }

                    add(saveButton,3, 1)
                    paddingTop = 15.px
                    paddingBottom = 15.px
                }

                hPanel {
                    marginLeft = 0.px
                    marginRight = 0.px
                    border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                    width = 100.perc
                }
                simplePanel {
                    height = 75.vh
                    add(childPanel)
                }
            }
        }
    }

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

    private fun goToBasicInfoPanel() {
        RoutingManager.redirect("/event/${ConduitManager.agendaStore.getState().selectedEvent?.id}${View.EVENT_BASIC_INFO.url}")
    }

    private fun goToDescriptionPanel() {
        RoutingManager.redirect("/event/${ConduitManager.agendaStore.getState().selectedEvent?.id}${View.EVENT_DESCRIPTION.url}")
    }

    private fun goToAgendaPanel() {
        RoutingManager.redirect("/event/${ConduitManager.agendaStore.getState().selectedEvent?.id}${View.EVENT_AGENDA.url}")
    }


    private fun save() {
        AppScope.launch {
            if(childPanel.validate() && childPanel.save()) {
                saveButton.disabled = true
                ConduitManager.showSuccessToast(tr("Event changes were saved succesfully."))
            }
        }
    }
}