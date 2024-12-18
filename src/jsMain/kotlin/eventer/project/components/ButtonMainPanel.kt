package eventer.project.components

import io.kvision.core.Border
import io.kvision.core.BorderStyle
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.html.Button
import io.kvision.panel.SimplePanel
import io.kvision.panel.vPanel
import io.kvision.utils.px

class ButtonMainPanel(buttonList: List<Button>) : SimplePanel() {
    init {
        vPanel {
            width = 200.px
            height = 600.px
            border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            spacing = 20
            paddingTop = 20.px
            buttonList.forEach {
                    button ->
                    add(button)
            }
        }
    }
}