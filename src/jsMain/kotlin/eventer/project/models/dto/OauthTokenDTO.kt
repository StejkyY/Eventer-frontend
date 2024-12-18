package eventer.project.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class OauthTokenDTO(val token: String)