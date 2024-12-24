package eventer.project.helpers

import io.kvision.types.LocalDate

fun addDaysToJSDate(date: LocalDate, days: Int): LocalDate {
    return LocalDate(date.getFullYear(), date.getMonth(), date.getDate() + days)
}

fun addHoursToJSDate(date: LocalDate, hours: Int): LocalDate {
    return LocalDate(date.getTime() + hours * 60 * 60 * 1000)
}

fun addMinutesToJSDate(date: LocalDate, minutes: Int): LocalDate {
    return LocalDate(date.getTime() + minutes * 60 * 1000)
}

fun addTimeToJSDate(date: LocalDate, time: LocalDate): LocalDate {
    return addMinutesToJSDate(addHoursToJSDate(date, time.getHours()), time.getMinutes())
}

fun subtractTwoDates(firstDate: LocalDate, secondDate: LocalDate): Int {
    val differenceInMilliseconds = firstDate.getTime() - secondDate.getTime()
    return (differenceInMilliseconds / (24 * 60 * 60 * 1000)).toInt()
}

fun LocalDate.compareTimes(date: LocalDate): Boolean {
    return this.getHours() == date.getHours() && this.getMinutes() == date.getMinutes()
}

fun LocalDate.lessThan(date: LocalDate): Boolean {
    return (this.getHours() * 60 + this.getMinutes()) < (date.getHours() * 60 + date.getMinutes())
}

operator fun LocalDate.compareTo(other: LocalDate): Int {
    return this.getTime().compareTo(other.getTime())
}