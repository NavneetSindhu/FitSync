package com.minimize.maximus.util

import com.minimize.maximus.BuildConfig

object DebugUtils {
    /**
     * Gating flag for developer and test utilities.
     */
    val isDebug: Boolean
        get() = BuildConfig.DEBUG
}
