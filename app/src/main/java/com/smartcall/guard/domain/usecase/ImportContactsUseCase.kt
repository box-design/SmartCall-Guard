package com.smartcall.guard.domain.usecase

import android.content.ContentResolver
import android.provider.ContactsContract
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import com.smartcall.guard.data.repository.RuleRepository
import com.smartcall.guard.utils.NumberNormalizer
import javax.inject.Inject

data class ImportResult(
    val importedCount: Int,
    val duplicateCount: Int
)

class ImportContactsUseCase @Inject constructor(
    private val contentResolver: ContentResolver,
    private val ruleRepository: RuleRepository
) {
    suspend fun execute(): ImportResult {
        val phones = mutableListOf<String>()

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null, null, null
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex) ?: continue
                phones.add(number)
            }
        }

        val normalizedPhones = phones
            .map { NumberNormalizer.normalize(it) }
            .filter { it.isNotBlank() }
            .distinct()

        val existingWhitelists = ruleRepository.getActiveWhitelistsSync()
        val existingValues = existingWhitelists
            .map { NumberNormalizer.normalize(it.value) }
            .toSet()

        val duplicates = normalizedPhones.count { it in existingValues }
        val newPhones = normalizedPhones.filter { it !in existingValues }

        if (newPhones.isNotEmpty()) {
            val rules = newPhones.map { phone ->
                RuleEntity(
                    type = RuleType.WHITELIST,
                    value = phone,
                    note = "来自通讯录导入"
                )
            }
            ruleRepository.insertRules(rules)
        }

        return ImportResult(
            importedCount = newPhones.size,
            duplicateCount = duplicates
        )
    }
}
