package eventer.project.layout.windows

import eventer.project.components.AgendaPrimaryButton
import eventer.project.models.Location
import eventer.project.models.Session
import io.kvision.core.AlignItems
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.i18n.tr
import io.kvision.modal.Modal
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
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
    private val buttonJSONExport: Button
    private val buttonCSVExport: Button
    private var sessionsMap: Map<Double, Map<Location, List<Session>>>? = null

    init {
        buttonJSONExport = AgendaPrimaryButton(tr("Download")){
            marginLeft = 5.px
            onClick {
                JSONexport()
            }
        }
        buttonCSVExport = AgendaPrimaryButton(tr("Download")){
            marginLeft = 5.px
            onClick {
                CSVexport()
            }
        }

        vPanel {
            hPanel {
                alignItems = AlignItems.CENTER
                add(Label(tr("JSON format")) {
                    width = 100.px
                })
                add(buttonJSONExport)
            }
            hPanel {
                alignItems = AlignItems.CENTER
                marginTop = 10.px
                add(Label(tr("CSV format")) {
                    width = 100.px
                })
                add(buttonCSVExport)
            }
        }
    }

    fun open(sessionsMap: Map<Double, Map<Location, List<Session>>>) {
        this.sessionsMap = sessionsMap
        show()
    }

    private fun downloadFile(data: String, fileName: String) {
        val blob = Blob(arrayOf(data), BlobPropertyBag("text/csv"))
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as web.html.HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        document.body.appendChild(anchor)
        anchor.click()
        document.body.removeChild(anchor)
        URL.revokeObjectURL(url)
    }

    private fun CSVexport() {
        val sessions = sessionsMap?.flatMap { (_, sessionsByLocation) ->
            sessionsByLocation.flatMap { (_, sessions) -> sessions }.sortedBy { it.startTime?.getTime() }
        }?.sortedBy { it.date?.getTime() }

        if (sessions != null) {
            val csvHeader = "Name,Date,Start Time,Duration,Description,Type,Location\n"
            val csvContent = sessions.joinToString("\n") { session ->
                val sessionStartDateFormatted = session.date?.getFullYear().toString() + "-" +
                        session.date?.getMonth()?.plus(1).toString().padStart(2, '0') + "-" +
                        session.date?.getDate().toString().padStart(2, '0')
                val sessionStartTimeFormatted = session.startTime?.getHours().toString().padStart(2, '0') + ":" +
                        session.startTime?.getMinutes().toString().padStart(2, '0')
                listOf(
                    "\"${session.name ?: ""}\"",
                    "\"$sessionStartDateFormatted\"",
                    "\"$sessionStartTimeFormatted\"",
                    "\"${session.duration ?: ""}\"",
                    "\"${session.description ?: ""}\"",
                    "\"${session.type?.name ?: ""}\"",
                    "\"${session.location?.name ?: ""}\""
                ).joinToString(",")
            }

            val csvData = csvHeader + csvContent
            downloadFile(csvData, "agenda_sessions.csv")
        }
    }

    private fun JSONexport() {
        val sessions = sessionsMap?.flatMap { (_, sessionsByLocation) ->
            sessionsByLocation.flatMap { (_, sessions) -> sessions }.sortedBy { it.startTime?.getTime() }
        }?.sortedBy { it.date?.getTime() }

        if (sessions != null) {
            val jsonArray = sessions.map { session ->
                val sessionStartDateFormatted = session.date?.getFullYear().toString() + "-" +
                        session.date?.getMonth()?.plus(1).toString().padStart(2, '0') + "-" +
                        session.date?.getDate().toString().padStart(2, '0')
                val sessionStartTimeFormatted = session.startTime?.getHours().toString().padStart(2, '0') + ":" +
                        session.startTime?.getMinutes().toString().padStart(2, '0')

                buildJsonObject {
                    put("name", session.name ?: "")
                    put("date", sessionStartDateFormatted)
                    put("startTime", sessionStartTimeFormatted)
                    put("duration", session.duration.toString())
                    put("description", session.description ?: "")
                    put("type", session.type?.name ?: "")
                    put("location", session.location?.name ?: "")
                }
            }

            val jsonString = Json.encodeToString(JsonArray(jsonArray))

            downloadFile(jsonString, "agenda_sessions.json")
        }
    }
}