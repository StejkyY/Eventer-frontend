package eventer.project.components

import eventer.project.web.View
import eventer.project.state.AgendaAppState
import eventer.project.web.RoutingManager
import io.kvision.core.*
import io.kvision.dropdown.DD
import io.kvision.dropdown.Direction
import io.kvision.dropdown.dropDown
import io.kvision.html.*
import io.kvision.panel.flexPanel
import io.kvision.utils.px
import org.w3c.dom.HTMLElement

fun Container.menu(state: AgendaAppState) {
    val languageButton = Button("ENG", style = ButtonStyle.LIGHT) {
        border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
    }


    flexPanel(
        FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.FLEXEND, AlignItems.CENTER,
        spacing = 10
    ) {
        marginTop = 10.px
                add(languageButton)
                dropDown("", elements = listOf(
                    (state.profile?.firstName + " " + state.profile?.lastName) to DD.HEADER.option,
                    io.kvision.i18n.tr("My profile") to View.PROFILE.url,
                    io.kvision.i18n.tr("Separator") to DD.SEPARATOR.option,
                    io.kvision.i18n.tr("Events") to View.EVENTS.url,
                    io.kvision.i18n.tr("Groups") to "/groups",
                    io.kvision.i18n.tr("Separator") to DD.SEPARATOR.option,
                    io.kvision.i18n.tr("Log out") to "/logout",
                ), arrowVisible = false, icon = "fas fa-bars") {
                    paddingRight = 30.px
                    direction = Direction.DROPUP
                }
        }
    }
