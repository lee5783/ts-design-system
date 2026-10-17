package com.ts.ds.core.logic

import android.os.Handler
import android.os.Looper

public fun interface TSToastScheduler {
    public fun schedule(delayMillis: Long, action: () -> Unit): Cancellable

    public fun interface Cancellable {
        public fun cancel()
    }

    public companion object {
        public val Main: TSToastScheduler by lazy { handler(Handler(Looper.getMainLooper())) }

        public fun handler(handler: Handler): TSToastScheduler = TSToastScheduler { delayMillis, action ->
            val runnable = Runnable(action)
            handler.postDelayed(runnable, delayMillis)
            Cancellable { handler.removeCallbacks(runnable) }
        }
    }
}
