package com.smartcall.guard.data.repository

import com.smartcall.guard.data.dao.RuleDao
import com.smartcall.guard.data.database.AppDatabase
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RuleRepository @Inject constructor(
    private val ruleDao: RuleDao,
    private val appDatabase: AppDatabase
) {

    fun getAllRules(): Flow<List<RuleEntity>> = ruleDao.getAllRules()

    fun getRulesByType(type: RuleType): Flow<List<RuleEntity>> = ruleDao.getRulesByType(type)

    fun getActiveRulesByType(type: RuleType): Flow<List<RuleEntity>> = ruleDao.getActiveRulesByType(type)

    fun getRuleById(id: String): Flow<RuleEntity?> = ruleDao.getRuleById(id)

    suspend fun getRuleByIdSync(id: String): RuleEntity? = ruleDao.getRuleByIdSync(id)

    fun getActiveWhitelistsSync(): List<RuleEntity> {
        return appDatabase.ruleDao().getActiveRulesByTypeSync(RuleType.WHITELIST)
    }

    fun getActiveBlacklistsSync(): List<RuleEntity> {
        val exact = appDatabase.ruleDao().getActiveRulesByTypeSync(RuleType.BLACKLIST_EXACT)
        val prefix = appDatabase.ruleDao().getActiveRulesByTypeSync(RuleType.BLACKLIST_PREFIX)
        val regex = appDatabase.ruleDao().getActiveRulesByTypeSync(RuleType.BLACKLIST_REGEX)
        return exact + prefix + regex
    }

    suspend fun insertRule(rule: RuleEntity) = ruleDao.insert(rule)

    suspend fun insertRules(rules: List<RuleEntity>) = ruleDao.insertAll(rules)

    suspend fun updateRule(rule: RuleEntity) = ruleDao.update(rule)

    suspend fun deleteRule(id: String) = ruleDao.deleteById(id)
}
