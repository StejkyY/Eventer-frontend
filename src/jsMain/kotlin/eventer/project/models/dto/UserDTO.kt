package eventer.project.models.dto

import eventer.project.models.User
import eventer.project.models.UserCredentials
import eventer.project.models.UserPasswordChange
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(val user: User)

@Serializable
data class UserPasswordDTO(val userPasswordChange: UserPasswordChange)

@Serializable
data class UserCredentialsDTO(val userCredentials: UserCredentials?)

