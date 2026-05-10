package com.smartcall.guard.domain.usecase

import android.content.ContentResolver
import android.provider.ContactsContract
import com.smartcall.guard.data.entity.BlockMode
import com.smartcall.guard.data.entity.BlockReason
import com.smartcall.guard.data.entity.LocationRule
import com.smartcall.guard.data.entity.RuleType
import com.smartcall.guard.data.repository.BlockLogRepository
import com.smartcall.guard.data.repository.LocationRepository
import com.smartcall.guard.data.repository.RuleRepository
import com.smartcall.guard.data.repository.SegmentRepository
import com.smartcall.guard.data.repository.SettingsRepository
import com.smartcall.guard.utils.EmergencyNumberChecker
import com.smartcall.guard.utils.LocationHelper
import com.smartcall.guard.utils.NumberNormalizer
import com.smartcall.guard.utils.PatternCompiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class EvaluateResult(
    val shouldBlock: Boolean,
    val reason: String = "",
    val blockReason: BlockReason? = null,
    val matchedRule: String? = null,
    val displayLocation: String? = null
)

@Singleton
class EvaluateCallUseCase @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val settingsRepository: SettingsRepository,
    private val segmentRepository: SegmentRepository,
    private val locationRepository: LocationRepository,
    private val blockLogRepository: BlockLogRepository,
    private val contentResolver: ContentResolver
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var compiledPatterns: List<PatternCompiler.CompiledPattern> = emptyList()
    private var cachedContactNumbers: Set<String> = emptySet()

    fun precompilePatterns() {
        val allBlacklists = ruleRepository.getActiveBlacklistsSync()
        compiledPatterns = PatternCompiler.compile(allBlacklists)
        loadContactsCache()
    }

    private fun loadContactsCache() {
        try {
            val numbers = mutableSetOf<String>()
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )
            cursor?.use {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val number = it.getString(numberIndex) ?: continue
                    numbers.add(NumberNormalizer.normalize(number))
                }
            }
            cachedContactNumbers = numbers
        } catch (_: Exception) {}
    }

    fun executeSync(phoneNumber: String): EvaluateResult {
        val startTime = System.currentTimeMillis()
        val normalized = NumberNormalizer.normalize(phoneNumber)

        if (EmergencyNumberChecker.isEmergency(normalized)) {
            return EvaluateResult(shouldBlock = false, reason = "emergency")
        }

        fun checkTimeout(): Boolean {
            return System.currentTimeMillis() - startTime > 100
        }

        val settings = settingsRepository.getSettingsSync() ?: return EvaluateResult(shouldBlock = false)

        val whitelists = ruleRepository.getActiveWhitelistsSync()
        for (wl in whitelists) {
            if (checkTimeout()) return EvaluateResult(shouldBlock = false)
            if (isRuleMatch(wl, normalized)) {
                return EvaluateResult(shouldBlock = false, reason = "whitelist")
            }
        }

        if (settings.whitelistContacts && normalized in cachedContactNumbers) {
            return EvaluateResult(shouldBlock = false, reason = "contact_whitelist")
        }

        if (settings.blockMode == BlockMode.WHITELIST_ONLY) {
            return EvaluateResult(
                shouldBlock = true,
                reason = "whitelist_only_mode",
                blockReason = BlockReason.STRICT_MODE
            )
        }

        if (settings.nightModeEnabled && isInNightModeTime(settings.nightModeStart, settings.nightModeEnd)) {
            return EvaluateResult(
                shouldBlock = true,
                reason = "night_mode",
                blockReason = BlockReason.NIGHT_MODE
            )
        }

        if (settings.repeatCallPassEnabled && isRepeatCall(normalized, settings.repeatCallPassMinutes)) {
            return EvaluateResult(shouldBlock = false, reason = "repeat_call_pass")
        }

        val blacklists = ruleRepository.getActiveBlacklistsSync()
        val segmentRules = blacklists.filter { it.type == RuleType.BLACKLIST_SEGMENT }
        val regexBlacklists = blacklists.filter { it.type == RuleType.BLACKLIST_REGEX }
        val nonRegexBlacklists = blacklists.filter { it.type != RuleType.BLACKLIST_REGEX && it.type != RuleType.BLACKLIST_SEGMENT }

        for (bl in nonRegexBlacklists) {
            if (checkTimeout()) return EvaluateResult(shouldBlock = false)
            if (isRuleMatch(bl, normalized)) {
                return EvaluateResult(
                    shouldBlock = true,
                    reason = bl.note ?: "blacklist: ${bl.value}",
                    blockReason = BlockReason.BLACKLIST,
                    matchedRule = bl.value
                )
            }
        }

        for (seg in segmentRules) {
            if (checkTimeout()) return EvaluateResult(shouldBlock = false)
            if (normalized.startsWith(NumberNormalizer.normalize(seg.value))) {
                return EvaluateResult(
                    shouldBlock = true,
                    reason = "segment: ${seg.value}",
                    blockReason = BlockReason.SEGMENT_BLOCK,
                    matchedRule = seg.value
                )
            }
        }

        val matched = PatternCompiler.match(compiledPatterns, normalized)
        if (matched != null) {
            return EvaluateResult(
                shouldBlock = true,
                reason = "regex: ${matched.originalValue}",
                blockReason = BlockReason.BLACKLIST,
                matchedRule = matched.originalValue
            )
        }

        if (settings.blockMode == BlockMode.STRICT) {
            return EvaluateResult(
                shouldBlock = true,
                reason = "strict_mode",
                blockReason = BlockReason.STRICT_MODE
            )
        }

        if (checkTimeout()) return EvaluateResult(shouldBlock = false)
        if (settings.locationRule != LocationRule.OFF) {
            val segment = try {
                segmentRepository.lookup(normalized)
            } catch (e: Exception) { null }

            if (segment != null) {
                val displayLocation = "${segment.province}${segment.city}"

                when (settings.locationRule) {
                    LocationRule.SAME_CITY -> {
                        val currentCity = locationRepository.getCurrentCitySync()
                        if (currentCity != null && !LocationHelper.isSameCity(segment.city, currentCity)) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "location_mismatch:city",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.SAME_PROVINCE -> {
                        val currentProvince = locationRepository.getCurrentProvinceSync()
                        if (currentProvince != null && !LocationHelper.isSameProvince(segment.province, currentProvince)) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "location_mismatch:province",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.BLOCK_OVERSEAS -> {
                        if (segment.province.contains("海外") || segment.province.contains("国外")) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "overseas",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.RADIUS -> {
                        val currentProvince = locationRepository.getCurrentProvinceSync()
                        if (currentProvince != null && !LocationHelper.isSameProvince(segment.province, currentProvince)) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "radius_mismatch",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.OFF -> {}
                }
            } else {
                if (settings.blockUnknownSegment) {
                    return EvaluateResult(
                        shouldBlock = true,
                        reason = "unknown_segment",
                        blockReason = BlockReason.UNKNOWN_SEGMENT
                    )
                }
            }
        }

        if (checkTimeout()) return EvaluateResult(shouldBlock = false)
        return EvaluateResult(shouldBlock = false)
    }

    private fun isRuleMatch(rule: com.smartcall.guard.data.entity.RuleEntity, normalized: String): Boolean {
        return when (rule.type) {
            RuleType.BLACKLIST_EXACT, RuleType.WHITELIST -> {
                val ruleNormalized = NumberNormalizer.normalize(rule.value)
                ruleNormalized == normalized
            }
            RuleType.BLACKLIST_PREFIX -> {
                val ruleNormalized = NumberNormalizer.normalize(rule.value)
                normalized.startsWith(ruleNormalized)
            }
            RuleType.BLACKLIST_SEGMENT -> {
                val ruleNormalized = NumberNormalizer.normalize(rule.value)
                normalized.startsWith(ruleNormalized)
            }
            else -> false
        }
    }

    private fun isInNightModeTime(startStr: String, endStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = Calendar.getInstance()
            val start = Calendar.getInstance().apply {
                val parsed = sdf.parse(startStr)
                if (parsed != null) {
                    val cal = Calendar.getInstance().apply { time = parsed }
                    set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
            val end = Calendar.getInstance().apply {
                val parsed = sdf.parse(endStr)
                if (parsed != null) {
                    val cal = Calendar.getInstance().apply { time = parsed }
                    set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            if (start.before(end)) {
                !now.before(start) && !now.after(end)
            } else {
                !now.before(start) || !now.after(end)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isRepeatCall(normalized: String, minutes: Int): Boolean {
        return try {
            val cutoff = System.currentTimeMillis() - (minutes * 60 * 1000L)
            val recentLogs = blockLogRepository.getRecentBlockedNumbersSync(cutoff)
            normalized in recentLogs
        } catch (_: Exception) {
            false
        }
    }

    fun logBlockedCall(phoneNumber: String, reason: String, matchedRule: String?, displayLocation: String?, blockReason: BlockReason?) {
        scope.launch {
            try {
                val log = com.smartcall.guard.data.entity.BlockLogEntity(
                    phoneNumber = phoneNumber,
                    normalizedNumber = NumberNormalizer.normalize(phoneNumber),
                    displayLocation = displayLocation,
                    matchedRule = matchedRule ?: reason,
                    blockReason = blockReason ?: BlockReason.BLACKLIST
                )
                blockLogRepository.insertLogSync(log)
            } catch (_: Exception) {}
        }
    }
}
