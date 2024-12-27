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
    private lateinit var registerButton: Button
    private lateinit var loginRedirectButton: Button
    private lateinit var firstNameText: Text
    private lateinit var lastNameText: Text
    private lateinit var emailText: Text
    private lateinit var passwordText: Text
    private lateinit var passwordText2: Text

    private val registerFormPanel: FormPanel<User>

    init {
        buttonsInitialization()
        textFieldsInitialization()

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
                add(User::firstName, firstNameText, required = true)
                add(User::lastName, lastNameText, required = true)
                add(
                    User::email,
                    emailText,
                    required = true,
                    validatorMessage = { gettext("E-mail address does not have correct syntax.") },
                    validator = {checkEmailSyntaxValid(it.getValue()!!)})
                add(User::password,
                    passwordText,
                    required = true,
                    validatorMessage = { tr("Password does not meet criteria.") },
                    validator = { password ->
                        if (password.getValue() == null) {
                            false
                        } else {
                            clearRegisterFormPanelValidation(password.getValue()!!)
                        }
                    })
                add(User::password2,
                    passwordText2,
                    required = true,
                    validatorMessage = { gettext("Password does not meet criteria.") },
                    validator = { password ->
                        if (password.getValue() == null) {
                            false
                        } else {
                            clearRegisterFormPanelValidation(password.getValue()!!)
                        }
                    })
                span {
                    +tr("Password needs to be atleast 8 characters long," +
                            " contain atleast one upper case and one number")
                    fontSize = 10.px
                }

                validator = {form ->
                    val passwordsMatch = form[User::password].toString() == form[User::password2].toString()

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

                add(loginRedirectButton)
            }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        registerButton = Button(tr("Register")){
            onClick {
                this@RegisterPanel.processRegister()
            }
            marginTop = 20.px
        }
        loginRedirectButton = Button(tr("Login")) {
            onClick {
                this@RegisterPanel.hide()
                RoutingManager.redirect(View.LOGIN)
            }
        }
    }

    /**
     * Initializes used text fields.
     */
    private fun textFieldsInitialization() {
        firstNameText = Text(label = tr("First name"), maxlength = 50)
        lastNameText = Text(label = tr("Last name"), maxlength = 50)
        emailText = Text(label = tr("E-mail"), maxlength = 50)
        passwordText = Password(label = tr("Password")) {
            maxlength = 64
            onInput {
                clearRegisterFormPanelValidation(registerFormPanel)
            }
        }
        passwordText2 = Password(label = tr("Password again")) {
            maxlength = 64
            onInput {
                clearRegisterFormPanelValidation(registerFormPanel)
            }
        }
    }

    /**
     * Helper function for clearing validator of the form panel for registration.
     */
    private fun clearRegisterFormPanelValidation(formPanel: FormPanel<User>) {
        formPanel.clearValidation()
    }

    /**
     * Checks validation criteria of the given password.
     */
    private fun clearRegisterFormPanelValidation(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                    password.any { it.isDigit() }
    }

    /**
     * Checks if the given email string has valid syntax of an email.
     */
    private fun checkEmailSyntaxValid(email: String): Boolean {
        return email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
    }

    /**
     * Sends the user info from the register form panel to the backend.
     */
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