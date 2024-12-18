package eventer.project.models.dto

import eventer.project.models.Location
import eventer.project.models.Type
import kotlinx.serialization.Serializable

@Serializable
data class TypeDTO(val type: Type)

@Serializable
data class TypesDTO(val types: List<Type>)
