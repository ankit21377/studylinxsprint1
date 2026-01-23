
package com.example.studylinx.viewmodel

import com.example.studylinx.model.University

data class UniversityUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val countryId: String = "",              // ✅ NEW
    val all: List<University> = emptyList()
) {
    val filtered: List<University>
        get() {
            val base = if (countryId.isBlank()) all
            else all.filter { it.countryId.equals(countryId, ignoreCase = true) }

            return if (query.isBlank()) base
            else base.filter { u ->
                u.name.contains(query, true) ||
                        u.city.contains(query, true) ||
                        u.country.contains(query, true)
            }
        }
}
