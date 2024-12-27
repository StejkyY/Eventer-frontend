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

class LoginPanel: SimplePanel() {
//    private val googleButton: Button
//    private val facebookButton: Button
//    private val registerButton: Button

    private val loginFormPanel: FormPanel<UserCredentials>
    private val passwordInput: Password

    init {
        passwordInput = Password(label = tr("Password"))
        loginFormPanel = this.formPanel {
            marginTop = 5.perc
            width = 400.px
            marginLeft = auto
            marginRight = auto
            padding = 20.px
            border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            textAlign = TextAlign.CENTER

            vPanel {
                add(Label(tr("Login")) {
                    fontSize = 28.px
                })
                paddingBottom = 20.px
            }

            vPanel (spacing = 10) {
                add(UserCredentials::email, Text(label = tr("E-mail")) {
                    autocomplete = Autocomplete.OFF
                }, required = true)
                add(UserCredentials::password, passwordInput, required = true)
                add(Button(tr("Login")) {
                    marginTop = 20.px
                    onClick {
                        processCredentials()
                    }
                })


                span {
                    +tr("Or you can use")
                    fontSize = 14.px
                    paddingTop = 20.px
                }

                add(Button(tr("Google")) {
                    disabled = true
                })
                add(Button(tr("Facebook")){
                    disabled = true
                })

                span {
                    +tr("Don't have an account yet?")
                    fontSize = 14.px
                    paddingTop = 30.px
                }

                    add(Button(tr("Register")) {
                        onClick {
                            this@LoginPanel.hide()
                            RoutingManager.redirect(View.REGISTER)
                        }
                    })
                }

                onEvent {
                    keydown = {
                        if (it.keyCode == ENTER_KEY) {
                            processCredentials()
                        }
                    }
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
