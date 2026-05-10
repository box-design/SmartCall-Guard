package com.smartcall.guard.domain.usecase

import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import com.smartcall.guard.data.repository.RuleRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class ImportExportResult(
    val importedCount: Int = 0,
    val skippedCount: Int = 0,
    val exportedCount: Int = 0
)

class ImportExportUseCase @Inject constructor(
    private val ruleRepository: RuleRepository
) {

    suspend fun exportRulesAsJson(types: List<RuleType>? = null): String {
        val allRules = ruleRepository.getAllRules().first()

        val filtered = if (types != null) allRules.filter { it.type in types } else allRules

        val jsonArray = JSONArray()
        filtered.forEach { rule ->
            val json = JSONObject().apply {
                put("type", rule.type.name)
                put("value", rule.value)
                rule.note?.let { put("note", it) }
                rule.tag?.let { put("tag", it) }
                put("isActive", rule.isActive)
            }
            jsonArray.put(json)
        }

        val root = JSONObject().apply {
            put("version", 1)
            put("rules", jsonArray)
        }
        return root.toString(2)
    }

    suspend fun importFromJson(json: String): ImportExportResult {
        var importedCount = 0
        var skippedCount = 0

        try {
            val root = JSONObject(json)
            val rulesArray = root.getJSONArray("rules")
            for (i in 0 until rulesArray.length()) {
                val item = rulesArray.getJSONObject(i)
                try {
                    val type = RuleType.valueOf(item.getString("type"))
                    val value = item.getString("value")
                    val note = item.optString("note", null)
                    val tag = item.optString("tag", null)
                    val isActive = item.optBoolean("isActive", true)

                    if (value.isNotBlank()) {
                        ruleRepository.insertRule(
                            RuleEntity(type = type, value = value, note = note, tag = tag, isActive = isActive)
                        )
                        importedCount++
                    } else {
                        skippedCount++
                    }
                } catch (_: Exception) {
                    skippedCount++
                }
            }
        } catch (_: Exception) {
            skippedCount++
        }

        return ImportExportResult(importedCount = importedCount, skippedCount = skippedCount)
    }

    suspend fun importFromPlainText(text: String, type: RuleType = RuleType.BLACKLIST_EXACT): ImportExportResult {
        var importedCount = 0
        var skippedCount = 0

        text.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank()) {
                try {
                    ruleRepository.insertRule(
                        RuleEntity(type = type, value = trimmed)
                    )
                    importedCount++
                } catch (_: Exception) {
                    skippedCount++
                }
            }
        }

        return ImportExportResult(importedCount = importedCount, skippedCount = skippedCount)
    }
}
