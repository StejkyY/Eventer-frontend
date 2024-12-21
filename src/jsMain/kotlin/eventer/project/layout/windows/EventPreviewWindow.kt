package eventer.project.layout.windows

import io.kvision.form.text.Text
import io.kvision.html.Label
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.modal.Modal
import io.kvision.panel.vPanel
import io.kvision.utils.px

class EventPreviewWindow : Modal(caption = tr("Event preview")) {
    private val previewLinkText: Text

    init {
        previewLinkText = Text(label = tr("Link")) {
            marginTop = 15.px
            disabled = true
        }

        vPanel {
            add(Label(tr("Copy this link for event preview.")))
            add(previewLinkText)
        }
    }

    fun open() {
        show()
    }
}