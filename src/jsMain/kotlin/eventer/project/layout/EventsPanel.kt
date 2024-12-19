package eventer.project.layout

import eventer.project.components.AgendaPrimaryButton
import eventer.project.models.Event
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.Sort
import eventer.project.web.View
import io.kvision.core.*
import io.kvision.dropdown.Direction
import io.kvision.dropdown.dropDown
import io.kvision.form.text.Text
import io.kvision.form.text.TextInput
import io.kvision.html.*
import io.kvision.i18n.I18n
import io.kvision.panel.*
import io.kvision.state.ObservableList
import io.kvision.state.bind
import io.kvision.state.observableListOf
import io.kvision.table.*
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.syncWithList
import web.html.HTML.body

class EventsPanel(state: AgendaAppState) : SimplePanel() {

    private val newEventButton: Button

    private var filteredEvents = observableListOf<Event>()

    init {
        width = 90.perc
        margin = 20.px
        marginLeft = auto
        marginRight = auto
        padding = 20.px
        border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))

        if(state.events != null ) filteredEvents.syncWithList(state.events)

        newEventButton = AgendaPrimaryButton(io.kvision.i18n.tr("Create new event")) {
            onClick {
                RoutingManager.redirect(View.NEW_EVENT)
            }
        }
        gridPanel (templateColumns = "1fr 1fr 1fr", alignItems = AlignItems.CENTER, justifyItems = JustifyItems.CENTER)  {
            gridColumnGap = 300
            add(Label(io.kvision.i18n.tr("Events")) {
                fontSize = 28.px
            }, 2 ,1)
            add(newEventButton, 3, 1)
            paddingBottom = 20.px
        }

        hPanel {
            marginLeft = 0.px
            marginRight = 0.px
            border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            width = 100.perc
        }

        flexPanel(
            FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.CENTER, AlignItems.CENTER,
            spacing = 5
        ) {
            paddingTop = 20.px
//            add(dropDown("My events", listOf(io.kvision.i18n.tr("Invited events") to "#/basic"), forDropDown = true) {
//                paddingRight = 30.px
//                paddingBottom = 15.px
//                direction = Direction.DROPDOWN
//            })
            add(Text(InputType.SEARCH) {
                input.autocomplete = Autocomplete.OFF
                placeholder = "${I18n.tr("Search:")}"
                width = 50.perc
                setEventListener<TextInput> {
                    input = {
                        val searchText = self.value?.lowercase() ?: ""
                        filteredEvents.syncWithList(state.events.orEmpty().filter { event ->
                            event.name?.lowercase()?.contains(searchText)!! ||
                                    event.state.toString().lowercase().contains(searchText) ||
                                    event.startDate?.toLocaleDateString()?.lowercase()?.contains(searchText) == true ||
                                    event.endDate?.toLocaleDateString()?.lowercase()?.contains(searchText) == true
                        }.toMutableList())
                    }
                }
            })

        }

        hPanel {
            marginLeft = 0.px
            marginRight = 0.px
            border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            width = 100.perc
        }

        table(types = setOf(TableType.BORDERLESS, TableType.HOVER)){
            height = 100.perc
            overflowY = Overflow.SCROLL
            marginTop = 20.px

            addHeaderCell(HeaderCell(I18n.tr("Title")))
            addHeaderCell(HeaderCell(I18n.tr("Status")))
            addHeaderCell(HeaderCell(I18n.tr("Start date")))
            addHeaderCell(HeaderCell(I18n.tr("End date")))
            addHeaderCell(HeaderCell(""))

            bind(filteredEvents) { events ->
                if(!filteredEvents.isEmpty()) {
                    filteredEvents.forEach {event ->
                        row {
                            cell(event.name)
                            cell(event.state.toString())
                            cell(event.startDate?.toLocaleDateString()!!)
                            cell(event.endDate?.toLocaleDateString()!!)
                            if(event.userEventRole == null) cell("")
                            else cell(event.userEventRole?.name.toString()) {
                                if(event.userEventRole?.name == "Owner") {
                                    this.fontWeight = FontWeight.BOLD
                                }
                            }
                            border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                            onClick {
                                RoutingManager.redirect("/event/${event.id}${View.EVENT_BASIC_INFO.url}")
                            }
                        }
                    }
                }
            }
        }
    }
}