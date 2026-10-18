package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.lazy.LazyListScope
import com.tinysol.designsystem.compose.components.checkbox.TSCheckbox
import com.tinysol.designsystem.sample.support.R as DemoR

val CheckboxPageSpec = DemoTogglePageSpec(
    page = "checkbox",
    stateSampleRes = DemoR.string.demo_sample_email_updates,
    firstSampleRes = DemoR.string.demo_sample_remember_me,
    secondSampleRes = DemoR.string.demo_sample_accept_terms,
    wrappingSampleRes = DemoR.string.demo_sample_long_consent,
    onRes = DemoR.string.demo_component_checked,
    offRes = DemoR.string.demo_component_unchecked,
    control = { checked, label, onCheckedChange, modifier, labelHidden, enabled ->
        TSCheckbox(checked, label, onCheckedChange, modifier, labelHidden = labelHidden, enabled = enabled)
    },
)

fun LazyListScope.checkboxPage() {
    togglePage(CheckboxPageSpec)
}
