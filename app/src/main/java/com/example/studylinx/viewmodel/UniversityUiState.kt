package com.example.studylinx.viewmodel

import com.example.studylinx.model.University

data class UniversityUiState(
    val loading: Boolean = true,
    val error: String? = null,

    val query: String = "",
    val countryId: String = "",          // ✅ filter (optional)
    val all: List<University> = emptyList()
) {
    val filtered: List<University>
        get() {
            val q = query.trim().lowercase()
            val country = countryId.trim().lowercase()

            return all.asSequence()
                .filter { u ->
                    if (country.isBlank()) true else u.countryId.lowercase() == country
                }
                .filter { u ->
                    if (q.isBlank()) true
                    else (
                            u.name.lowercase().contains(q) ||
                                    u.city.lowercase().contains(q) ||
                                    u.country.lowercase().contains(q) ||
                                    u.courses.any { it.lowercase().contains(q) }
                            )
                }
                .sortedBy { it.name.lowercase() }
                .toList()
        }
}
