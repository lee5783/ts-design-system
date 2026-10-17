package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R

public object TSBadgeCountFormatter {
    public const val DEFAULT_MAX_COUNT: Int = 99

    public fun format(
        count: Int,
        maxCount: Int = DEFAULT_MAX_COUNT,
        overflow: (maxCount: Int) -> String = { "$it+" },
    ): String? {
        require(maxCount > 0) { "maxCount must be positive: $maxCount" }
        return when {
            count <= 0 -> null
            count > maxCount -> overflow(maxCount)
            else -> count.toString()
        }
    }

    public fun format(
        context: Context,
        count: Int,
        maxCount: Int = DEFAULT_MAX_COUNT,
    ): String? = format(count, maxCount) { context.getString(R.string.ts_badge_count_overflow, it) }
}
