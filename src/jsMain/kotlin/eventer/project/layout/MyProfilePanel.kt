package eventer.project.layout

import eventer.project.*
import eventer.project.components.AgendaIconButton
import eventer.project.components.UnsavedChangesConfirm
import eventer.project.models.User
import eventer.project.state.AgendaAppState
import eventer.project.web.ConduitManager
import eventer.project.web.View
import io.kvision.core.*
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.*
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

class MyProfilePanel(val state: AgendaAppState) : SimplePanel() {
    private val changeEmailButton: Button
    private val changePasswordButton: Button
    private val deleteAccountButton: Button
    private val backButton: AgendaIconButton
    private val emailText: Text
    private val firstNameText: Text
    private val lastNameText: Text
    private val saveButton: Button
    private val closeButton: Button

    private val currentPasswordText: Text
    private val newPasswordText: Text
    private var changing: Changing? = null

    private val profileFormPanel: FormPanel<User>
    private val passwordFormPanel: FormPanel<PasswordState>
    private val unsavedChangesConfirmWindow: UnsavedChangesConfirm = UnsavedChangesConfirm()
    private val toastContainer = ToastContainer(ToastContainerPosition.TOPRIGHT)

    init {
        width = 400.px
        margin = 20.px
        marginLeft = auto
        marginRight = auto
        padding = 20.px
        border = Border(2.px, BorderStyle.SOLID, Color.name(Col.SILVER))

        emailText = Text(label = io.kvision.i18n.tr("E-mail")) {
            disabled = true
        }
        firstNameText = Text(label = io.kvision.i18n.tr("First name"))
        lastNameText = Text(label = io.kvision.i18n.tr("Last name"))
        currentPasswordText = Password(label = io.kvision.i18n.tr("Current password"))
        newPasswordText = Password(label = io.kvision.i18n.tr("New password"))
        changeEmailButton = Button(io.kvision.i18n.tr("Change email")){
            width = 180.px
            onClick {
                change(Changing.EMAIL)
            }
        }
        changePasswordButton = Button(io.kvision.i18n.tr("Change password")){
            width = 180.px
            onClick {
                change(Changing.PASSWORD)
            }
        }
        deleteAccountButton = Button(io.kvision.i18n.tr("Delete account")){
            marginTop = 50.px
            onClick {
                deleteAccount()
            }
        }
        saveButton = Button(io.kvision.i18n.tr("Save")) {
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
                    unsavedChangesConfirmWindow.show(io.kvision.i18n.tr("You have unsaved changes, are you sure you want to leave?"),
                        yesCallback = { ConduitManager.showPreviousPage()})
                }
            }
        }

        closeButton = Button(io.kvision.i18n.tr("Close")) {
            marginTop = 50.px
            onClick {
                closeChanges()
            }
        }

        passwordFormPanel = formPanel {
            add(PasswordState::currentPassword, currentPasswordText)
            add(
                PasswordState::newPassword, newPasswordText,
                validatorMessage = { io.kvision.i18n.tr("Password does not meet criteria.") }) {
                var result = false
                if(it.getValue() != null) {
                    result = it.getValue()!!.length >= 8 && it.getValue()?.any { it.isUpperCase() }!! && it.getValue()?.any { it.isDigit() }!!
                }
                result
            }

            onChange {
                saveButton.disabled = false
            }
        }

        vPanel {
            gridPanel (templateColumns = "1fr 1fr 1fr", alignItems = AlignItems.CENTER, justifyItems = JustifyItems.CENTER)  {
                gridColumnGap = 50
                add(backButton)
                add(Label(io.kvision.i18n.tr("My profile")) {
                    fontSize = 28.px
                    width = 150.px
                })
                add(saveButton)
                paddingBottom = 20.px
            }
            hPanel {
                marginLeft = 0.px
                marginRight = 0.px
                border = Border(1.px, BorderStyle.SOLID, Color.name(Col.SILVER))
                width = 100.perc
            }
        }



        profileFormPanel = formPanel {
            marginTop = 20.px
            add(
                    User::firstName,
                    firstNameText
                )
            add(
                    User::lastName,
                    lastNameText
            )
            add(
                User::email,
                emailText)

            add(passwordFormPanel)

            hPanel (spacing = 10) {
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
        } else {
            emailText.hide()
            currentPasswordText.show()
            newPasswordText.show()
        }
    }

    private fun closeChanges() {
        deleteAccountButton.show()
        firstNameText.show()
        lastNameText.show()
        closeButton.hide()
        emailText.disabled = true
        emailText.show()
        currentPasswordText.hide()
        newPasswordText.hide()
        changing = null
    }

    private fun save() {
        saveButton.disabled = true
        AppScope.launch {
            try {
                if(passwordFormPanel[PasswordState::currentPassword] != null &&
                    passwordFormPanel[PasswordState::newPassword] != null) {
                    if(!ConduitManager.updatePassword(passwordFormPanel[PasswordState::currentPassword]!!,
                            passwordFormPanel[PasswordState::newPassword]!!)
                    ) {
                        currentPasswordText.validatorError = io.kvision.i18n.tr("Password is not correct.")
                        return@launch
                    }
                }
                if(profileFormPanel.validate() && passwordFormPanel.validate()) {
                    ConduitManager.updateProfile(profileFormPanel.getData())
                    toastContainer.showToast(io.kvision.i18n.tr("Profile updated succesfully."), color = BsColor.SUCCESSBG)
                }
            }catch (e: Exception) {
                console.log(e)
                toastContainer.showToast(io.kvision.i18n.tr("Something went wrong."), color = BsColor.DANGERBG)
            }
        }
    }

    private fun deleteAccount() {
        Confirm.show("Are you sure?", "Do you want to delete your account?") {
            AppScope.launch {
                ConduitManager.deleteProfile()
            }
        }
    }
}