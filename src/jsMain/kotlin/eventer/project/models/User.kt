package eventer.project.models

import kotlinx.serialization.Serializable


@Serializable
data class UserPasswordChange(
    val currentPassword: String? = null,
    val newPassword: String? = null
)

@Serializable
data class UserCredentials(
    val email: String? = null,
    val password: String? = null
)

@Serializable
data class User(
    val id: Int? = null,
    val token: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val password2: String? = null,
    val userIdentityId: Int? = null
)