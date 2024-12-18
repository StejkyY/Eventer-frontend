package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class IdentityProvider {
    Local, Google, Microsoft
}