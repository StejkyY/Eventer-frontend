package eventer.project.models.dto

import eventer.project.models.Location
import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(val location: Location)

@Serializable
data class LocationsDTO(val locations: List<Location>)
