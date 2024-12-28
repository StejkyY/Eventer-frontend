package eventer.project.helpers

import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.modal.Confirm
import io.kvision.utils.px

class AgendaPrimaryButton(
    text: String,
    buttonClassName: String? = null,
    init: (AgendaPrimaryButton.() -> Unit)? = null)
    : Button(text = text, style = ButtonStyle.LIGHT, className = buttonClassName) {
    init {
        this.border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
        init?.invoke(this)
    }
}

class MenuTextButton(text: String, init: (MenuTextButton.() -> Unit)? = null) : Button(text = text) {
    init {
        setStyle("background", "none")
        setStyle("color", "inherit")
        setStyle("box-sizing", "border-box")
        this.border = Border(1.px, BorderStyle.SOLID, Color.name(Col.WHITE))
        borderRadius = 0.px
        init?.invoke(this)
    }

    fun disable() {
        disabled = true
        this.borderTop = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
        this.borderBottom = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
    }

    fun enable() {
        disabled = false
        this.borderTop = Border(1.px, BorderStyle.SOLID, Color.name(Col.WHITE))
        this.borderBottom = Border(1.px, BorderStyle.SOLID, Color.name(Col.WHITE))
    }
}

class AgendaIconButton(icon: String? = null, init: (AgendaIconButton.() -> Unit)? = null) : Button(text = "", icon = icon) {
    init {
        setStyle("background", "none")
        setStyle("color", "inherit")
        setStyle("border", "none")
        init?.invoke(this)
    }
}

class AgendaTextButton(text: String, init: (AgendaTextButton.() -> Unit)? = null) : Button(text = text) {

    init {
        setStyle("background", "none")
        setStyle("color", "inherit")
        setStyle("box-sizing", "border-box")
        this.border = Border(2.px, BorderStyle.SOLID, Color.name(Col.WHITE))
        borderRadius = 0.px
        onClick {
            disable()
        }
        init?.invoke(this)
    }

    fun disable() {
        disabled = true
        this.borderBottom = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
    }

    fun enable() {
        disabled = false
        this.borderBottom = Border(2.px, BorderStyle.SOLID, Color.name(Col.WHITE))
    }
}