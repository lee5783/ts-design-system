package com.tinysol.designsystem.views.sample.shell

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tinysol.designsystem.core.logic.TSToastQueue
import com.tinysol.designsystem.core.logic.TSToastTimeouts

class DemoSession(application: Application) : AndroidViewModel(application) {
    val toasts = TSToastQueue(TSToastTimeouts.default)
    val toastUndoTaps = MutableLiveData(0)
}
