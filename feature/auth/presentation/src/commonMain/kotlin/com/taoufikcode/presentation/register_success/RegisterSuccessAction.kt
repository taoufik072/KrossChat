package com.taoufikcode.presentation.register_success

sealed interface RegisterSuccessAction {
    data object OnLoginClick: RegisterSuccessAction
    data object OnResendVerificationEmail: RegisterSuccessAction

}