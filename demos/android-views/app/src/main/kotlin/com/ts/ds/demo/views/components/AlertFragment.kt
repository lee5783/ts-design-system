package com.ts.ds.demo.views.components

import android.view.View
import android.widget.LinearLayout
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSAlertTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.alert.TSAlertView
import com.ts.ds.views.components.button.TSButtonView

class AlertFragment : DemoPageFragment(R.layout.page_alert) {
    override val page: DemoPageId = DemoPageId.Alert

    private var playgroundTone = TSAlertTone.Info
    private var playgroundTitle = true
    private var playgroundDismissible = true
    private var playgroundAction = false
    private var playgroundDismissed by pageState.boolean(false)

    private val TSAlertTone.labelRes: Int
        get() = when (this) {
            TSAlertTone.Info -> DemoR.string.demo_sample_information
            TSAlertTone.Success -> DemoR.string.demo_sample_success
            TSAlertTone.Warning -> DemoR.string.demo_sample_warning
            TSAlertTone.Danger -> DemoR.string.demo_sample_error
        }

    override fun bindRows(rows: LinearLayout) {
        bindTones(rows.findViewById(R.id.alert_tones))
        bindContents(rows.findViewById(R.id.alert_contents))
        bindPlayground(rows.findViewById(R.id.alert_playground))
    }

    private fun alert(message: CharSequence, configure: TSAlertView.() -> Unit = {}): TSAlertView =
        TSAlertView(requireContext()).apply {
            this.message = message
            configure()
        }

    private fun bindTones(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_tones)
        val saved = SampleData.message("saved")
        section.addSpaced(
            alert(getString(DemoR.string.demo_sample_sign_in_body)) {
                title = getString(DemoR.string.demo_sample_sign_in_title)
                tag = TONE_INFO
            },
            matchWidth = true,
        )
        section.addSpaced(
            alert(getString(saved.bodyRes)) {
                tone = TSAlertTone.Success
                title = getString(saved.titleRes)
            },
            matchWidth = true,
        )
        section.addSpaced(
            alert(getString(DemoR.string.demo_sample_storage_body)) {
                tone = TSAlertTone.Warning
                title = getString(DemoR.string.demo_sample_storage_title)
            },
            matchWidth = true,
        )
    }

    private fun bindContents(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_contents)
        val saved = SampleData.message("saved")
        val offline = SampleData.message("offline")
        val deleted = SampleData.message("deleted")
        section.addSpaced(alert(getString(saved.bodyRes)), matchWidth = true)
        section.addSpaced(
            alert(getString(offline.bodyRes)) {
                tone = TSAlertTone.Warning
                title = getString(offline.titleRes)
                actionView = TSButtonView(context).apply {
                    text = getString(requireNotNull(offline.actionRes))
                    variant = TSButtonVariant.Link
                }
            },
            matchWidth = true,
        )
        section.addSpaced(
            alert(getString(deleted.bodyRes)) {
                tone = TSAlertTone.Success
                title = getString(deleted.titleRes)
                icon = TSIcon.Trash
            },
            matchWidth = true,
        )
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        lateinit var alert: TSAlertView
        lateinit var show: TSButtonView
        lateinit var counter: DemoTapCounter
        val offline = SampleData.message("offline")
        val retry = TSButtonView(requireContext()).apply {
            text = getString(requireNotNull(offline.actionRes))
            variant = TSButtonVariant.Link
            tag = PLAYGROUND_RETRY
            setOnClickListener { counter.increment() }
        }
        section.addCaption(getString(DemoR.string.demo_component_tone))
        section.addChoiceGroup(TSAlertTone.entries, { getString(it.labelRes) }, playgroundTone, { "demo.alert.playground.tone.${it.name.lowercase()}" }) {
            playgroundTone = it
            configurePlayground(alert, retry, show)
        }
        section.addToggle(DemoR.string.demo_component_title, checked = true, tag = PLAYGROUND_TITLE) {
            playgroundTitle = it
            configurePlayground(alert, retry, show)
        }
        section.addToggle(DemoR.string.demo_component_dismissible, checked = true, tag = PLAYGROUND_DISMISSIBLE) {
            playgroundDismissible = it
            configurePlayground(alert, retry, show)
        }
        section.addToggle(DemoR.string.demo_component_action, tag = PLAYGROUND_ACTION) {
            playgroundAction = it
            configurePlayground(alert, retry, show)
        }
        alert = section.addSpaced(alert(getString(offline.bodyRes)) { tag = PLAYGROUND_ALERT }, matchWidth = true) as TSAlertView
        show = section.addSpaced(
            TSButtonView(requireContext()).apply {
                text = getString(DemoR.string.demo_component_show_again)
                variant = TSButtonVariant.Secondary
                tag = PLAYGROUND_SHOW
                setOnClickListener {
                    playgroundDismissed = false
                    alert.visibility = View.VISIBLE
                    isEnabled = false
                }
            },
        ) as TSButtonView
        counter = section.addTapCounter(PLAYGROUND_COUNT)
        if (playgroundDismissed) alert.visibility = View.GONE
        configurePlayground(alert, retry, show)
    }

    private fun configurePlayground(alert: TSAlertView, retry: TSButtonView, show: TSButtonView) {
        val offline = SampleData.message("offline")
        alert.tone = playgroundTone
        alert.title = if (playgroundTitle) getString(offline.titleRes) else null
        alert.actionView = if (playgroundAction) retry else null
        alert.onDismiss = if (playgroundDismissible) {
            {
                playgroundDismissed = true
                alert.visibility = View.GONE
                show.isEnabled = true
            }
        } else {
            null
        }
        show.isEnabled = alert.visibility != View.VISIBLE
    }

    companion object {
        const val TONE_INFO = "demo.alert.tone.info"
        const val PLAYGROUND_TITLE = "demo.alert.playground.title"
        const val PLAYGROUND_DISMISSIBLE = "demo.alert.playground.dismissible"
        const val PLAYGROUND_ACTION = "demo.alert.playground.action"
        const val PLAYGROUND_ALERT = "demo.alert.playground.alert"
        const val PLAYGROUND_SHOW = "demo.alert.playground.show"
        const val PLAYGROUND_RETRY = "demo.alert.playground.retry"
        const val PLAYGROUND_COUNT = "demo.alert.playground.count"
    }
}
