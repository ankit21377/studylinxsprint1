package com.example.studylinx

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityInstrumentedTest {

    @get:Rule
    val rule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun loginScreen_displaysEmailField() {
        rule.onNodeWithTag("email").assertExists()
    }
    @Test
    fun loginScreen_displaysPasswordField() {
        rule.onNodeWithTag("password").assertExists()
    }
    @Test
    fun loginScreen_displaysLoginButton() {
        rule.onNodeWithTag("login").assertExists()
    }
    @Test
    fun loginScreen_allowsTypingEmailAndPassword() {
        rule.onNodeWithTag("email").performTextInput("test@example.com")
        rule.onNodeWithTag("password").performTextInput("12345678")
        rule.onNodeWithTag("email").assertTextContains("test@example.com")
    }
    @Test
    fun clickingLoginWithEmptyInputs_staysOnLoginScreen() {
        rule.onNodeWithTag("login").performClick()
        rule.onNodeWithTag("email").assertExists()
    }


}
