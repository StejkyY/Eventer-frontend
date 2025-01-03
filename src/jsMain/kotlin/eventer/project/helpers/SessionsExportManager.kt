package eventer.project.helpers

import eventer.project.models.Location
import eventer.project.models.Session
import io.kvision.i18n.gettext
import io.kvision.i18n.tr
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import web.dom.document

object SessionsExportManager {
    /**
     * Downloads a file in the browser with the given data, file name, and MIME type.
     */
    private fun downloadFile(data: String, fileName: String, mimeType: String) {
        val blob = Blob(arrayOf(data), BlobPropertyBag("$mimeType;charset=utf-8"))
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as web.html.HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        document.body.appendChild(anchor)
        anchor.click()
        document.body.removeChild(anchor)
        URL.revokeObjectURL(url)
    }

    /**
     * Exports sessions to a CSV file and triggers a download.
     */
    fun CSVexport(sessionsMap: Map<Double, Map<Location, List<Session>>>) {
        val sessions = sessionsMap.flatMap { (_, sessionsByLocation) ->
            sessionsByLocation.flatMap { (_, sessions) -> sessions }.sortedBy { it.startTime?.getTime() }
        }.sortedBy { it.date?.getTime() }

        val csvHeader = "Name,Date,Start Time,Duration,Description,Type,Location\n"
        val csvContent = sessions.joinToString("\n") { session ->

            val sessionStartDateFormatted = formatDate(session.date!!)
            val sessionStartTimeFormatted = formatTime(session.startTime!!)

            listOf(
                "\"${session.name ?: ""}\"",
                "\"$sessionStartDateFormatted\"",
                "\"$sessionStartTimeFormatted\"",
                "\"${session.duration ?: ""}\"",
                "\"${session.description ?: ""}\"",
                "\"${gettext(session.type?.name.toString())}\"",
                "\"${session.location?.name ?: ""}\""
            ).joinToString(",")
        }

        val csvData = "\uFEFF" + csvHeader + csvContent
        downloadFile(csvData, "agenda_sessions.csv", "text/csv")
    }

    fun JSONexport(sessionsMap: Map<Double, Map<Location, List<Session>>>) {
        val sessions = sessionsMap.flatMap { (_, sessionsByLocation) ->
            sessionsByLocation.flatMap { (_, sessions) -> sessions }.sortedBy { it.startTime?.getTime() }
        }.sortedBy { it.date?.getTime() }

        val jsonArray = sessions.map { session ->

            val sessionStartDateFormatted = formatDate(session.date!!)
            val sessionStartTimeFormatted = formatTime(session.startTime!!)

            buildJsonObject {
                put("name", session.name ?: "")
                put("date", sessionStartDateFormatted)
                put("startTime", sessionStartTimeFormatted)
                put("duration", session.duration.toString())
                put("description", session.description ?: "")
                put("type", gettext(session.type?.name.toString()))
                put("location", session.location?.name ?: "")
            }
        }

        val jsonString = Json.encodeToString(JsonArray(jsonArray))

        downloadFile(jsonString, "agenda_sessions.json", "application/json")
    }

    /**
     * Formatting of date: YYYY-MM-DD
     */
    private fun formatDate(date: LocalDate): String {
        return date.getFullYear().toString() + "-" +
                date.getMonth().plus(1).toString().padStart(2, '0') + "-" +
                date.getDate().toString().padStart(2, '0')
    }

    /**
     * Formatting of time: HH-MM-SS
     */
    private fun formatTime(time: LocalTime): String {
        return time.getHours().toString().padStart(2, '0') + ":" +
                time.getMinutes().toString().padStart(2, '0')
    }
}