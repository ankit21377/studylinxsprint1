package com.example.studylinx.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.studylinx.data.repo.HomeRepo
import com.example.studylinx.data.repo.HomeRepoImpl
import com.example.studylinx.model.Event
import com.example.studylinx.model.HomeSummary

class HomeViewModel(
        private val repo: HomeRepo = HomeRepoImpl()
    ) : ViewModel() {

        var summary by mutableStateOf(HomeSummary())
            private set

        var events by mutableStateOf<List<Event>>(emptyList())
            private set

        fun loadHome(userId: String) {
            repo.getHomeSummary(userId) { ok, _, data ->
                if (ok && data != null) summary = data
            }

            repo.getEvents(userId) { ok, _, list ->
                if (ok && list != null) events = list
            }
        }
    }
