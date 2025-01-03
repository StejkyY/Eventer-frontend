package eventer.project.layout

import eventer.project.*
import eventer.project.helpers.AgendaIconButton
import eventer.project.helpers.UnsavedChangesConfirm
import eventer.project.models.User
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.Button
import io.kvision.html.Label
import io.kvision.html.Span
import io.kvision.html.span
import io.kvision.i18n.tr
import io.kvision.modal.Confirm
import io.kvision.panel.*
import io.kvision.toast.ToastContainer
import io.kvision.toast.ToastContainerPosition
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

enum class Changing {
    EMAIL, PASSWORD
}

@Serializable
data class PasswordState(val currentPassword: String? = null, val newPassword: String? = null)

class MyProfilePanel(val state: AgendaAppState) : SimplePanel(className = "basic-form-panel") {
    private lateinit var changeEmailButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var backButton: AgendaIconButton
    private lateinit var emailText: Text
    private lateinit var firstNameText: Text
    private lateinit var lastNameText: Text
    private lateinit var saveButton: Button
    private lateinit var closeButton: Button

    private lateinit var currentPasswordText: Text
    private lateinit var newPasswordText: Text
    private var changing: Changing? = null
    private lateinit var passwordCriteriaText: Span

    private val profileFormPanel: FormPanel<User>
    private val passwordFormPanel: FormPanel<PasswordState>
    private val unsavedChangesConfirmWindow: UnsavedChangesConfirm = UnsavedChangesConfirm()
    private val toastContainer = ToastContainer(ToastContainerPosition.TOPRIGHT)

    init {
        buttonsInitialization()
        textFieldsInitialization()

        passwordFormPanel = formPanel {
            add(PasswordState::currentPassword, currentPasswordText, required = true)
            add(
                PasswordState::newPassword, newPasswordText, required = true,
                validatorMessage = { tr("Password does not meet criteria.") }) {
                var result = false
                if(it.getValue() != null) {
                    result = it.getValue()!!.length >= 8 &&
                            it.getValue()?.any{it.isUpperCase()}!! &&
                                    it.getValue()?.any{it.isDigit()}!!
                }
                result
            }

            onChange {
                saveButton.disabled = false
            }
        }

        vPanel {
            gridPanel (
                templateColumns = "1fr 1fr 1fr",
                alignItems = AlignItems.CENTER,
                justifyItems = JustifyItems.CENTER
            ){
                gridColumnGap = 50
                add(backButton)
                add(Label(tr("My profile"), className = "main-label") {
                    width = 150.px
                })
                add(saveButton)
                paddingBottom = 20.px
            }
            hPanel(className = "separator-line") {}
        }

        profileFormPanel = formPanel {
            marginTop = 20.px
            add(User::firstName, firstNameText)
            add(User::lastName, lastNameText)
            add(User::email, emailText)
            add(passwordFormPanel)
            add(passwordCriteriaText)

            hPanel (spacing = 10) {
                marginTop = 10.px
                add(changeEmailButton)
                add(changePasswordButton)
            }
            vPanel (spacing = 15) {
                add(deleteAccountButton)
                add(closeButton)
                closeButton.hide()
            }
            if(state.profile != null) {
                setData(state.profile)
            }
            currentPasswordText.hide()
            newPasswordText.hide()
            onChange {
                saveButton.disabled = false
            }
        }
    }

    /**
     * Initializes used buttons.
     */
    private fun buttonsInitialization() {
        changeEmailButton = Button(tr("Change email"), className = "profile-change-button"){
            onClick {
                change(Changing.EMAIL)
            }
        }
        changePasswordButton = Button(tr("Change password"), className = "profile-change-button"){
            onClick {
                change(Changing.PASSWORD)
            }
        }
        deleteAccountButton = Button(tr("Delete account")){
            marginTop = 50.px
            onClick {
                deleteAccount()
            }
        }
        saveButton = Button(tr("Save")) {
            disabled = true
            onClick {
                save()
            }
        }

        backButton = AgendaIconButton("fas fa-arrow-left") {
            setAttribute("aria-label", "Go to previous window")
            onClick {
                if(saveButton.disabled) {
                    ConduitManager.showPreviousPage()
                } else {
                    unsavedChangesConfirmWindow.show(
                        tr("You have unsaved changes, are you sure you want to leave?"),
                        yesCallback = {ConduitManager.showPreviousPage()}
                    )
                }
            }
        }

        closeButton = Button(tr("Close")) {
            marginTop = 50.px
            onClick {
                closeChanges()
            }
        }
    }

    /**
     * Initializes used text input fields.
     */
    private fun textFieldsInitialization() {
        passwordCriteriaText = span(className = "small-label") {
            +tr("Password needs to be atleast 8 characters long," +
                    " contain atleast one upper case and one number")
        }
        passwordCriteriaText.hide()
        emailText = Text(label = tr("E-mail"), maxlength = 50) {
            disabled = true
        }
        firstNameText = Text(label = tr("First name"), maxlength = 50)
        lastNameText = Text(label = tr("Last name"), maxlength = 50)
        currentPasswordText = Password(label = tr("Current password")) {
            maxlength = 64
        }
        newPasswordText = Password(label = tr("New password")) {
            maxlength = 64
        }
    }

    /**
     * Adjusts the panel layout based on whether the user is changing email or password.
     */
    private fun change(changing: Changing) {
        deleteAccountButton.hide()
        firstNameText.hide()
        lastNameText.hide()
        closeButton.show()
        if (changing == Changing.EMAIL) {
            emailText.disabled = false
            emailText.show()
            currentPasswordText.hide()
            newPasswordText.hide()
            passwordCriteriaText.hide()
        } else {
            emailText.hide()
            currentPasswordText.show()
            newPasswordText.show()
            passwordCriteriaText.show()
        }
    }

    /**
     * Changes back the panel layout to show the currently authenticated user info.
     */
    private fun closeChanges() {
        deleteAccountButton.show()
        firstNameText.show()
        lastNameText.show()
        closeButton.hide()
        emailText.disabled = true
        emailText.show()
        currentPasswordText.hide()
        newPasswordText.hide()
        passwordCriteriaText.hide()
        changing = null
    }

    /**
     * Saves the changed data about the currently authenticated user profile.
     */
    private fun save() {
        saveButton.disabled = true
        AppScope.launch {
            try {
                if (profileFormPanel.validate() && passwordFormPanel.validate()) {
                    if(passwordFormPanel[PasswordState::currentPassword] != null &&
                        passwordFormPanel[PasswordState::newPassword] != null) {
                        if(!ConduitManager.updatePassword(passwordFormPanel[PasswordState::currentPassword]!!,
                                passwordFormPanel[PasswordState::newPassword]!!)
                        ) {
                            currentPasswordText.validatorError = tr("Password is not correct.")
                            return@launch
                        }
                    }
                    ConduitManager.updateProfile(profileFormPanel.getData())
                    toastContainer.showToast(tr("Profile updated succesfully."), color = BsColor.SUCCESSBG)
                }
            }catch (e: Exception) {
                console.log(e)
                toastContainer.showToast(tr("Something went wrong."), color = BsColor.DANGERBG)
            }
        }
    }

    /**
     * Deletes currently authenticated user's account.
     */
    private fun deleteAccount() {
        Confirm.show(tr("Are you sure?"), tr("Do you want to delete your account?")) {
            AppScope.launch {
                ConduitManager.deleteProfile()
            }
        }
    }
}