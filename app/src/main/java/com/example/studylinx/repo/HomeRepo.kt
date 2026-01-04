package com.example.studylinx.data.repo

import com.example.studylinx.model.Event
import com.example.studylinx.model.HomeSummary


interface HomeRepo {
    fun getHomeSummary(
        userId: String,
        callback: (Boolean, String, HomeSummary?) -> Unit
    )

    fun saveHomeSummary(
        userId: String,
        summary: HomeSummary,
        callback: (Boolean, String) -> Unit
    )

    fun getEvents(
        userId: String,
        callback: (Boolean, String, List<Event>?) -> Unit
    )

    fun addEvent(
        userId: String,
        event: Event,
        callback: (Boolean, String) -> Unit
    )

    fun deleteEvent(
        userId: String,
        eventId: String,
        callback: (Boolean, String) -> Unit
    )
}