package eventer.project.layout.windows

import eventer.project.helpers.AgendaPrimaryButton
import eventer.project.helpers.SessionsExportManager
import eventer.project.models.Location
import eventer.project.models.Session
import io.kvision.core.AlignItems
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.i18n.tr
import io.kvision.modal.Modal
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import io.kvision.utils.px
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import web.dom.document

class AgendaExportWindow : Modal(caption = tr("Agenda sessions export")) {
    private lateinit var buttonJSONExport: Button
    private lateinit var buttonCSVExport: Button
    private var sessionsMap: Map<Double, Map<Location, List<Session>>>? = null

    init {
        buttonsInitilization()

        vPanel {
            hPanel {
                alignItems = AlignItems.CENTER
                add(Label(tr("JSON format"), className = "export-label"))
                add(buttonJSONExport)
            }
            hPanel {
                alignItems = AlignItems.CENTER
                marginTop = 10.px
                add(Label(tr("CSV format"), className = "export-label"))
                add(buttonCSVExport)
            }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitilization() {
        buttonJSONExport = AgendaPrimaryButton(tr("Download"), buttonClassName = "export-download-button"){
            onClick {
                SessionsExportManager.JSONexport(sessionsMap!!)
            }
        }
        buttonCSVExport = AgendaPrimaryButton(tr("Download"), buttonClassName = "export-download-button"){
            onClick {
                SessionsExportManager.CSVexport(sessionsMap!!)
            }
        }
    }

    /**
     * Opens the modal and sets the sessions map.
     */
    fun open(sessionsMap: Map<Double, Map<Location, List<Session>>>) {
        this.sessionsMap = sessionsMap
        show()
    }
}