package com.example.studylinx

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterActivityInstrumentedTest {

    @get:Rule
    val rule = createAndroidComposeRule<RegisterActivity>()

    private fun SemanticsNodeInteractionsProvider.editTextField(tag: String): SemanticsNodeInteraction {
        return onNode(hasTestTag(tag) and hasSetTextAction(), useUnmergedTree = true)
    }

    @Before
    fun setup() = Intents.init()

    @After
    fun tearDown() = Intents.release()

    @Test
    fun passwordMismatch_doesNotNavigateToLogin() {
        rule.waitForIdle()

        rule.editTextField("reg_email").performClick()
        rule.editTextField("reg_email").performTextInput("test@gmail.com")

        rule.editTextField("reg_password").performClick()
        rule.editTextField("reg_password").performTextInput("123456")

        rule.editTextField("reg_confirm").performClick()
        rule.editTextField("reg_confirm").performTextInput("654321")

        rule.onNodeWithTag("reg_signup", useUnmergedTree = true).performClick()

        Intents.intended(hasComponent(LoginActivity::class.java.name), times(0))
    }

    @Test
    fun blankEmailWithMatchingPasswords_doesNotNavigateToLogin() {
        rule.waitForIdle()

        rule.editTextField("reg_password").performClick()
        rule.editTextField("reg_password").performTextInput("123456")

        rule.editTextField("reg_confirm").performClick()
        rule.editTextField("reg_confirm").performTextInput("123456")

        rule.onNodeWithTag("reg_signup", useUnmergedTree = true).performClick()

        Intents.intended(hasComponent(LoginActivity::class.java.name), times(0))
    }
    @Test
    fun registerScreen_renders() {
        rule.onNodeWithTag("reg_title", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("reg_signup", useUnmergedTree = true).assertExists()
    }

    @Test
    fun debug_printRegisterSemanticsTree() {
        rule.onRoot(useUnmergedTree = true).printToLog("REG_TREE")
    }
    @Test
    fun backArrow_navigatesToLogin() {
        rule.onNodeWithTag("reg_back", useUnmergedTree = true).performClick()
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }





}
