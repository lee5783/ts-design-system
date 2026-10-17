package com.ts.ds.core.input

import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TSTextInputTraitsTest {
    @Test
    fun defaultIsPlainSentenceText() {
        val traits = TSTextInputTraits.Default
        assertEquals(TSKeyboardType.Text, traits.keyboardType)
        assertEquals(TSImeAction.Default, traits.imeAction)
        assertEquals(TSCapitalization.Sentences, traits.capitalization)
        assertTrue(traits.autoCorrect)
        assertTrue(traits.autofillHints.isEmpty())
    }

    @Test
    fun presetsMatchTheIosTraits() {
        val expectations = listOf(
            Triple(TSTextInputTraits.Name, TSKeyboardType.Text, listOf(View.AUTOFILL_HINT_NAME)),
            Triple(TSTextInputTraits.Email, TSKeyboardType.Email, listOf(View.AUTOFILL_HINT_EMAIL_ADDRESS)),
            Triple(TSTextInputTraits.Username, TSKeyboardType.Text, listOf(View.AUTOFILL_HINT_USERNAME)),
            Triple(TSTextInputTraits.Password, TSKeyboardType.Text, listOf(View.AUTOFILL_HINT_PASSWORD)),
            Triple(TSTextInputTraits.NewPassword, TSKeyboardType.Text, listOf("newPassword")),
            Triple(TSTextInputTraits.OneTimeCode, TSKeyboardType.Number, listOf("smsOTPCode")),
            Triple(TSTextInputTraits.Phone, TSKeyboardType.Phone, listOf(View.AUTOFILL_HINT_PHONE)),
            Triple(TSTextInputTraits.Number, TSKeyboardType.Number, emptyList()),
            Triple(TSTextInputTraits.Decimal, TSKeyboardType.Decimal, emptyList()),
            Triple(TSTextInputTraits.Url, TSKeyboardType.Uri, emptyList()),
            Triple(TSTextInputTraits.Search, TSKeyboardType.Text, emptyList()),
        )
        expectations.forEach { (traits, keyboard, hints) ->
            assertEquals(keyboard, traits.keyboardType)
            assertEquals(hints, traits.autofillHints)
            assertFalse(traits.autoCorrect)
        }
        assertEquals(TSCapitalization.Words, TSTextInputTraits.Name.capitalization)
        assertEquals(TSImeAction.Search, TSTextInputTraits.Search.imeAction)
        assertEquals(TSCapitalization.None, TSTextInputTraits.Email.capitalization)
    }
}
