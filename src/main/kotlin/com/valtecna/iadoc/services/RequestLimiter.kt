package com.valtecna.iadoc.services

import com.intellij.openapi.components.service
import com.valtecna.iadoc.settings.DocProSettingsState
import com.valtecna.iadoc.settings.Plan
import java.time.LocalDate

class RequestLimiter {
    fun allow(): Boolean {
        val s = service<DocProSettingsState>()
        s.resetIfNewDay(LocalDate.now().toEpochDay())
        return if (s.plan == Plan.FREE) s.dailyCount < s.freeDailyLimit else true
    }

    fun record() {
        val s = service<DocProSettingsState>()
        s.resetIfNewDay(LocalDate.now().toEpochDay())
        s.dailyCount += 1
    }
}

