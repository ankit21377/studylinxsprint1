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

}
