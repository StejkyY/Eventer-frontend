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
import io.kvision.html.*
import io.kvision.i18n.I18n
import io.kvision.panel.SimplePanel
import io.kvision.panel.vPanel
import io.kvision.utils.auto
import io.kvision.utils.px

class LoginPanel: SimplePanel() {
//    private val googleButton: Button
//    private val facebookButton: Button
//    private val registerButton: Button

    private var credentials : UserCredentials? = null
    private val loginFormPanel: FormPanel<UserCredentials>
    private val passwordInput: Password

    init {
        passwordInput = Password(label = "${I18n.tr("Password")}")
        loginFormPanel = this.formPanel {
            width = 400.px
            margin = 20.px
            marginLeft = auto
            marginRight = auto
            padding = 20.px
            border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            textAlign = TextAlign.CENTER

            vPanel {
                add(Label(io.kvision.i18n.tr("Login")) {
                    fontSize = 28.px
                })
                paddingBottom = 20.px
            }

            vPanel (spacing = 10) {
                add(UserCredentials::email, Text(label = "${I18n.tr("E-mail")}") {
                    autocomplete = Autocomplete.OFF
                }, required = true)
                add(UserCredentials::password, passwordInput, required = true)
                add(Button(io.kvision.i18n.tr("Login")) {
                    marginTop = 20.px
                    onClick {
                        processCredentials()
                    }
                })


                span {
                    +io.kvision.i18n.tr("Or you can use")
                    fontSize = 14.px
                    paddingTop = 20.px
                }

                add(Button(io.kvision.i18n.tr("Google")))
                add(Button(io.kvision.i18n.tr("Facebook")))

                span {
                    +io.kvision.i18n.tr("Don't have an account yet?")
                    fontSize = 14.px
                    paddingTop = 30.px
                }

                    add(Button(io.kvision.i18n.tr("Register")) {
                        onClick {
                            this@LoginPanel.hide()
                            RoutingManager.redirect(View.REGISTER)
                        }
                    })
                }
            }
    }

    private fun processCredentials() {
        if(loginFormPanel.validate()) {
            ConduitManager.login(loginFormPanel.getData())
            passwordInput.value = null
        }
    }
}
