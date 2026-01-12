package com.example.screenshots.model

import java.time.LocalDate

data class ScreenshotItem(
    val uri: String,
    val takenAtMillis: Long,
    val displayName: String
)

data class DayGroup(
    val date: LocalDate,
    val items: List<ScreenshotItem>
)
