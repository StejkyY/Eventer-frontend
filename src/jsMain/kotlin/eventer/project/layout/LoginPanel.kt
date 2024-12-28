package eventer.project.layout

import eventer.project.web.ConduitManager
import eventer.project.web.View
import eventer.project.models.UserCredentials
import eventer.project.web.RoutingManager
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.Autocomplete
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.html.span
import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import io.kvision.panel.SimplePanel
import io.kvision.panel.vPanel
import io.kvision.utils.ENTER_KEY
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import js.decorators.DecoratorContextKind

class LoginPanel: SimplePanel() {
    private lateinit var googleLoginButton: Button
    private lateinit var microsoftLoginButton: Button
    private lateinit var loginButton: Button
    private lateinit var registerRedirectButton: Button

    private val loginFormPanel: FormPanel<UserCredentials>
    private val passwordInput: Password

    init {
        textAlign = TextAlign.CENTER
        passwordInput = Password(label = tr("Password"))
        buttonsInitialization()

        loginFormPanel = this.formPanel(className = "basic-form-panel")  {
            marginTop = 5.perc

            vPanel {
                add(Label(tr("Login"), className = "main-label"))
                paddingBottom = 20.px
            }

            vPanel (spacing = 10) {
                add(UserCredentials::email, Text(label = tr("E-mail")) {
                    autocomplete = Autocomplete.OFF
                }, required = true)
                add(UserCredentials::password, passwordInput, required = true)
                add(loginButton)

                span(className = "medium-label") {
                    +tr("Or you can use")
                    paddingTop = 20.px
                }

                add(googleLoginButton)
                add(microsoftLoginButton)

                span(className = "medium-label") {
                    +tr("Don't have an account yet?")
                    paddingTop = 30.px
                }

                add(registerRedirectButton)

                onEvent {
                    keydown = {
                        if (it.keyCode == ENTER_KEY) {
                            processCredentials()
                        }
                    }
                }
            }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        loginButton = Button(tr("Login")) {
            marginTop = 20.px
            onClick {
                processCredentials()
            }
        }

        googleLoginButton = Button(tr("Google")) {
            disabled = true
        }
        microsoftLoginButton = Button(tr("Microsoft")){
            disabled = true
        }

        registerRedirectButton = Button(tr("Register")) {
            onClick {
                this@LoginPanel.hide()
                RoutingManager.redirect(View.REGISTER)
            }
        }
    }

    /**
     * Sends credentials from the login form panel to the backend.
     */
    private fun processCredentials() {
        if(loginFormPanel.validate()) {
            ConduitManager.login(loginFormPanel.getData())
            passwordInput.value = null
        }
    }
}
