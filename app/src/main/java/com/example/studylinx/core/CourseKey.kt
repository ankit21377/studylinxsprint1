package com.example.studylinx.core

object CourseKey {
    fun keyOf(courseName: String): String {
        return courseName
            .trim()
            .lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "_")
            .trim('_')
    }
}