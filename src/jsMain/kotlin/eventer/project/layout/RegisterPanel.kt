package eventer.project.layout

import eventer.project.AppScope
import eventer.project.web.ConduitManager
import eventer.project.web.View
import eventer.project.models.User
import eventer.project.web.RoutingManager
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.html.span
import io.kvision.i18n.I18n
import io.kvision.i18n.gettext
import io.kvision.i18n.tr
import io.kvision.modal.Alert
import io.kvision.panel.*
import io.kvision.rest.RemoteRequestException
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

class RegisterPanel: SimplePanel() {
    private val registerButton: Button
    private val registerFormPanel: FormPanel<User>

    init {
        registerButton =  Button(tr("Register")){
            onClick {
                this@RegisterPanel.processRegister()
            }
            marginTop = 20.px
        }

        registerFormPanel = this.formPanel  {
            width = 400.px
            marginTop = 5.perc
            marginLeft = auto
            marginRight = auto
            padding = 20.px
            border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))
            textAlign = TextAlign.CENTER

            vPanel {
                add(Label(tr("Register")) {
                    fontSize = 28.px
                })
                paddingBottom = 20.px
            }

            vPanel (spacing = 10) {
                add(User::firstName, Text(label = tr("First name"), maxlength = 50), required = true)
                add(User::lastName, Text(label = tr("Last name"), maxlength = 50), required = true)
                add(User::email, Text(label = tr("E-mail"), maxlength = 50), required = true,
                    validatorMessage = { gettext("E-mail address does not have correct syntax.") }) {
                    it.getValue()?.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) ?: false
                }
                add(User::password,
                    Password(label = tr("Password")) { maxlength = 64 },
                    required = true,
                    validatorMessage = { tr("Password does not meet criteria.") }) {
                    if(it.getValue() != null) {
                        it.getValue()!!.length >= 8 && it.getValue()?.any { it.isUpperCase() }!! && it.getValue()?.any { it.isDigit() }!!
                    }
                    true
                }
                add(User::password2,
                    Password(label = tr("Password again")) { maxlength = 64 },
                    required = true,
                    validatorMessage = { gettext("Password does not meet criteria.") }) {
                    if (it.getValue() != null) {
                        it.getValue()!!.length >= 8 && it.getValue()?.any { it.isUpperCase() }!! && it.getValue()
                            ?.any { it.isDigit() }!!
                    }
                    true
                }
                span {
                    +tr("Password needs to be atleast 8 characters long," +
                            " contain atleast one upper case and one number")
                    fontSize = 10.px
                }

                validator = {form ->
                    val password = form[User::password].toString()
                    val password2 = form[User::password2].toString()

                    val passwordsMatch = password == password2

                    if (!passwordsMatch) {
                        form.getControl(User::password)?.validatorError = gettext("Passwords are not the same")
                        form.getControl(User::password2)?.validatorError = gettext("Passwords are not the same")
                    }
                    passwordsMatch
                }
                add(registerButton)

                span {
                    +tr("Already have an account?")
                    fontSize = 14.px
                    paddingTop = 30.px
                }

                add(Button(tr("Login")) {
                    onClick {
                        this@RegisterPanel.hide()
                        RoutingManager.redirect(View.LOGIN)
                    }
                })
            }
        }
    }

    private fun processRegister() {
        if(registerFormPanel.validate()) {
            val userData = registerFormPanel.getData()
            AppScope.launch {
                if(ConduitManager.registerProfile(userData)) {
                    Alert.show(text = tr("User registered. You can now log in.")) {
                        registerFormPanel.clearData()
                        RoutingManager.redirect(View.LOGIN)
                    }
                }
            }
        }
    }
}