package eventer.project

import eventer.project.models.UserCredentials
import eventer.project.state.AgendaAppAction
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.View
import io.kvision.modal.Alert
import io.kvision.remote.*
import io.kvision.rest.RemoteRequestException
import kotlinx.coroutines.CompletableDeferred


object Security {

    private var loginDeferred = CompletableDeferred<UserCredentials>()

    suspend fun <T> withAuth(block: suspend () -> T): T {
        if(ConduitManager.getLocalStorageToken(ConduitManager.JWT_TOKEN) == null){
            login()
        } else {
            try {
                if (ConduitManager.agendaStore.getState().profile == null){
                    ConduitManager.readProfile()
                }
                return block()
            } catch (e: RemoteRequestException) {
                if(e.code.toInt() == HTTP_UNAUTHORIZED) {
                    login()
                }
            }
        }
        return block()
    }

    suspend fun login() {
        var loggedIn = false
        RoutingManager.redirect(View.LOGIN)
        loginDeferred = CompletableDeferred()

        while (!loggedIn) {
            try {
                val credentials = loginDeferred.await()
                if (!ConduitManager.userLogin(credentials)) {
                    loginDeferred = CompletableDeferred()
                } else {
                    loggedIn = true
                    ConduitManager.showPreviousPage()
                }
            } catch (e: Throwable) {
                console.log(e)
            }
        }
    }

    fun completeLogin(credentials: UserCredentials) {
        loginDeferred.complete(credentials)
    }

    fun logout() {
        loginDeferred = CompletableDeferred()
    }
}