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

    //Deferred object to manage login flow and wait for user credentials
    private var loginDeferred = CompletableDeferred<UserCredentials>()

    /**
     * Executes a given block of code that requires authentication.
     * Ensures the user is logged in before executing the block. If the user's session is
     * invalid or expired, prompts the user to log in again.
     */
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

    /**
     * Handles the login process for the application.
     * Redirects the user to the login page and waits for the user to complete the login.
     * On successful login, redirects the user back to the previous page.
     */
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

    /**
     * Completes the login process by providing user credentials.
     */
    fun completeLogin(credentials: UserCredentials) {
        loginDeferred.complete(credentials)
    }

    /**
     * Logs out the user by resetting the deferred object.
     */
    fun logout() {
        loginDeferred = CompletableDeferred()
    }
}