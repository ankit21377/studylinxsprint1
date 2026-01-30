package com.example.studylinx

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class DashboardActivityInstrumentedTest {

    @get:Rule
    val rule = createAndroidComposeRule<DashboardActivity>()

    @Test
    fun dashboard_renders() {
        rule.onNodeWithTag("dashboard").assertExists()
    }

    @Test
    fun bottomNav_itemsExist() {
        rule.onNodeWithTag("nav_home", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("nav_search", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("nav_notification", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("nav_profile", useUnmergedTree = true).assertExists()
    }

    @Test
    fun clickingNav_switchesScreens() {
        // Home is default
        rule.onNodeWithTag("home_screen", useUnmergedTree = true).assertExists()

        rule.onNodeWithTag("nav_search", useUnmergedTree = true).performClick()
        rule.onNodeWithTag("search_screen", useUnmergedTree = true).assertExists()

        rule.onNodeWithTag("nav_notification", useUnmergedTree = true).performClick()
        rule.onNodeWithTag("notification_screen", useUnmergedTree = true).assertExists()

        rule.onNodeWithTag("nav_profile", useUnmergedTree = true).performClick()
        rule.onNodeWithTag("profile_screen", useUnmergedTree = true).assertExists()

        rule.onNodeWithTag("nav_home", useUnmergedTree = true).performClick()
        rule.onNodeWithTag("home_screen", useUnmergedTree = true).assertExists()
    }
}
