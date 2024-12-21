package eventer.project.components

import eventer.project.web.View
import eventer.project.state.AgendaAppState
import eventer.project.web.RoutingManager
import io.kvision.core.*
import io.kvision.dropdown.DD
import io.kvision.dropdown.Direction
import io.kvision.dropdown.dropDown
import io.kvision.form.select.SelectInput
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.panel.flexPanel
import io.kvision.utils.px
import org.w3c.dom.HTMLElement
import web.cssom.HtmlAttributes.Companion.border

fun Container.menu(state: AgendaAppState) {
    val languageButton = Button("ENG", style = ButtonStyle.LIGHT) {
        border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
    }

    flexPanel(
        FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.FLEXEND, AlignItems.CENTER,
        spacing = 10
    ) {
        marginTop = 10.px
//                add(languageButton)
        add(SelectInput(
            listOf("en" to tr("ENG"), "cz" to tr("CZE")),
            I18n.language) {
            width = 80.px
            onEvent {
                change = {
                    I18n.language = self.value ?: "en"
                }
            }
        })
        dropDown("", elements = listOf(
            (state.profile?.firstName + " " + state.profile?.lastName) to DD.HEADER.option,
            tr("My profile") to View.PROFILE.url,
            tr("Separator") to DD.SEPARATOR.option,
            tr("Events") to View.EVENTS.url,
            tr("Groups") to "/groups",
            tr("Separator") to DD.SEPARATOR.option,
            tr("Log out") to "/logout",
        ), arrowVisible = false, icon = "fas fa-bars") {
            paddingRight = 30.px
            direction = Direction.DROPUP
        }
    }
}
