package com.ts.ds.demo.compose.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ts.ds.compose.components.iconbutton.TSIconButton
import com.ts.ds.compose.components.textfield.TSTextField
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.input.TSTextInputTraits
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.sample.SampleData

object TextFieldPageTags {
    const val PLAYGROUND_FIELD = "demo.text-field.playground.field"
    const val PLAYGROUND_ERROR = "demo.text-field.playground.error"
    const val PLAYGROUND_DISABLED = "demo.text-field.playground.disabled"
    const val PLAYGROUND_READ_ONLY = "demo.text-field.playground.read-only"
    const val PLAYGROUND_LEADING_ICON = "demo.text-field.playground.leading-icon"
    const val PLAYGROUND_SECURE = "demo.text-field.playground.secure"
    const val PLAYGROUND_MULTILINE = "demo.text-field.playground.multiline"
}

private object TextFieldSample {
    const val EMAIL = "ada@example.com"
    const val SHORT_PASSWORD = "abc"
    const val NAME = "Ada Lovelace"
    const val ACCOUNT_ID = "TS-2048-7731"
    const val SEARCH = "Analytical"
}

fun LazyListScope.textFieldPage() {
    componentSection("text-field.states", DemoR.string.demo_component_states) { TextFieldStates() }
    componentSection("text-field.with-icons", DemoR.string.demo_component_with_icons) { TextFieldSearch() }
    componentSection("text-field.password", DemoR.string.demo_component_password) { TextFieldPassword() }
    componentSection("text-field.multiline", DemoR.string.demo_component_multiline) { TextFieldMultiline() }
    componentSection("text-field.keyboard-types", DemoR.string.demo_component_keyboard_types) { TextFieldKeyboardTypes() }
    componentSection("text-field.playground", DemoR.string.demo_component_playground) { TextFieldPlayground() }
}

@Composable
private fun TextFieldStates() {
    TSTextField(
        value = "",
        label = stringResource(DemoR.string.demo_component_default),
        onValueChange = {},
        placeholder = stringResource(DemoR.string.demo_sample_email_placeholder),
        hint = stringResource(DemoR.string.demo_sample_email_hint),
    )
    TSTextField(TextFieldSample.EMAIL, stringResource(DemoR.string.demo_component_filled), onValueChange = {})
    TSTextField(
        value = TextFieldSample.SHORT_PASSWORD,
        label = stringResource(DemoR.string.demo_component_error),
        onValueChange = {},
        hint = stringResource(DemoR.string.demo_sample_password_hint),
        error = stringResource(DemoR.string.demo_sample_password_error),
    )
    TSTextField(
        value = TextFieldSample.NAME,
        label = stringResource(DemoR.string.demo_component_disabled),
        onValueChange = {},
        hint = stringResource(DemoR.string.demo_sample_managed),
        enabled = false,
    )
    TSTextField(
        value = TextFieldSample.ACCOUNT_ID,
        label = stringResource(DemoR.string.demo_component_read_only),
        onValueChange = {},
        hint = stringResource(DemoR.string.demo_sample_copy_hint),
        readOnly = true,
    )
}

@Composable
private fun TextFieldSearch() {
    var search by rememberSaveable { mutableStateOf(TextFieldSample.SEARCH) }
    TSTextField(
        value = search,
        label = stringResource(DemoR.string.demo_sample_search),
        onValueChange = { search = it },
        placeholder = stringResource(DemoR.string.demo_sample_search_placeholder),
        leadingIcon = TSIcon.Search,
        inputTraits = TSTextInputTraits.Search,
        trailing = if (search.isEmpty()) {
            null
        } else {
            { TSIconButton(TSIcon.Close, contentDescription = stringResource(DemoR.string.demo_sample_clear), onClick = { search = "" }) }
        },
    )
}

@Composable
private fun TextFieldPassword() {
    var password by rememberSaveable { mutableStateOf("") }
    TSTextField(
        value = password,
        label = stringResource(DemoR.string.demo_sample_password),
        onValueChange = { password = it },
        hint = stringResource(DemoR.string.demo_sample_password_hint),
        secure = true,
        inputTraits = TSTextInputTraits.NewPassword,
    )
}

@Composable
private fun TextFieldMultiline() {
    val pangram = stringResource(SampleData.pangramRes)
    var bio by rememberSaveable { mutableStateOf(pangram) }
    TSTextField(
        value = bio,
        label = stringResource(DemoR.string.demo_sample_bio),
        onValueChange = { bio = it },
        placeholder = stringResource(DemoR.string.demo_sample_bio_placeholder),
        hint = stringResource(DemoR.string.demo_sample_bio_hint),
        multiline = true,
    )
}

@Composable
private fun TextFieldKeyboardTypes() {
    listOf(
        DemoR.string.demo_sample_email to TSTextInputTraits.Email,
        DemoR.string.demo_sample_phone to TSTextInputTraits.Phone,
        DemoR.string.demo_sample_amount to TSTextInputTraits.Decimal,
        DemoR.string.demo_sample_website to TSTextInputTraits.Url,
        DemoR.string.demo_sample_code to TSTextInputTraits.OneTimeCode,
    ).forEach { (label, traits) ->
        var value by rememberSaveable(label) { mutableStateOf("") }
        TSTextField(value = value, label = stringResource(label), onValueChange = { value = it }, inputTraits = traits)
    }
}

@Composable
private fun TextFieldPlayground() {
    var text by rememberSaveable { mutableStateOf("") }
    var hasError by rememberSaveable { mutableStateOf(false) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var readOnly by rememberSaveable { mutableStateOf(false) }
    var leadingIcon by rememberSaveable { mutableStateOf(false) }
    var secure by rememberSaveable { mutableStateOf(false) }
    var multiline by rememberSaveable { mutableStateOf(false) }

    ToggleRow(
        label = stringResource(DemoR.string.demo_component_error),
        checked = hasError,
        onCheckedChange = { hasError = it },
        modifier = Modifier.testTag(TextFieldPageTags.PLAYGROUND_ERROR),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(TextFieldPageTags.PLAYGROUND_DISABLED),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_read_only),
        checked = readOnly,
        onCheckedChange = { readOnly = it },
        modifier = Modifier.testTag(TextFieldPageTags.PLAYGROUND_READ_ONLY),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_leading_icon),
        checked = leadingIcon,
        onCheckedChange = { leadingIcon = it },
        modifier = Modifier.testTag(TextFieldPageTags.PLAYGROUND_LEADING_ICON),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_secure),
        checked = secure,
        onCheckedChange = { secure = it },
        modifier = Modifier.testTag(TextFieldPageTags.PLAYGROUND_SECURE),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_multiline),
        checked = multiline,
        onCheckedChange = { multiline = it },
        modifier = Modifier.testTag(TextFieldPageTags.PLAYGROUND_MULTILINE),
    )
    TSTextField(
        value = text,
        label = stringResource(DemoR.string.demo_sample_email),
        onValueChange = { text = it },
        modifier = Modifier.testTag(TextFieldPageTags.PLAYGROUND_FIELD),
        placeholder = stringResource(DemoR.string.demo_sample_email_placeholder),
        hint = stringResource(DemoR.string.demo_sample_email_hint),
        error = if (hasError) stringResource(DemoR.string.demo_sample_email_error) else null,
        leadingIcon = if (leadingIcon) TSIcon.Mail else null,
        secure = secure,
        multiline = multiline,
        readOnly = readOnly,
        inputTraits = TSTextInputTraits.Email,
        enabled = !disabled,
    )
}
