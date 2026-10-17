package com.ts.ds.demo.views.shell

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ts.ds.core.logic.TSToastQueue
import com.ts.ds.core.logic.TSToastTimeouts

class DemoSession(application: Application) : AndroidViewModel(application) {
    val toasts = TSToastQueue(TSToastTimeouts.timeoutFor(application))
    val toastUndoTaps = MutableLiveData(0)
}
