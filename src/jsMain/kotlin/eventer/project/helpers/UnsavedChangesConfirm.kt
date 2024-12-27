package eventer.project.helpers

import io.kvision.modal.Confirm

class UnsavedChangesConfirm(): Confirm() {
    fun show(text: String, yesCallback: (() -> Unit)) {
        show(
            io.kvision.i18n.tr("Unsaved changes"),
            text = text,
            yesTitle = io.kvision.i18n.tr("Leave"),
            noTitle = io.kvision.i18n.tr("Stay"),
            cancelTitle = io.kvision.i18n.tr("Cancel"),
            yesCallback = yesCallback)
    }
}