package com.smartcall.guard.data.repository

import android.content.Context
import com.smartcall.guard.data.SegmentDatabaseHelper
import com.smartcall.guard.data.entity.Segment
import com.smartcall.guard.utils.NumberNormalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SegmentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val helper = SegmentDatabaseHelper()

    init {
        helper.copyDatabaseIfNeeded(context)
    }

    fun lookup(number: String): Segment? {
        val normalized = NumberNormalizer.normalize(number)
        val prefix7 = normalized.take(7)
        return helper.querySegment(prefix7) ?: run {
            val prefix3 = normalized.take(3)
            helper.querySegment(prefix3)
        }
    }
}
