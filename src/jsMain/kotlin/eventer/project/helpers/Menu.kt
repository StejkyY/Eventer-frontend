package eventer.project.helpers

import eventer.project.web.View
import eventer.project.state.AgendaAppState
import io.kvision.core.*
import io.kvision.dropdown.DD
import io.kvision.dropdown.Direction
import io.kvision.dropdown.dropDown
import io.kvision.form.select.SelectInput
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.panel.flexPanel
import io.kvision.utils.px

fun Container.menu(state: AgendaAppState) {

    flexPanel(
        FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.FLEXEND, AlignItems.CENTER,
        spacing = 10
    ) {
        marginTop = 10.px
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
            "Separator" to DD.SEPARATOR.option,
            tr("Events") to View.EVENTS.url,
            "Separator" to DD.SEPARATOR.option,
            tr("Logout") to "/logout",
        ), arrowVisible = false, icon = "fas fa-bars") {
            paddingRight = 30.px
            direction = Direction.DROPUP
        }
    }
}
