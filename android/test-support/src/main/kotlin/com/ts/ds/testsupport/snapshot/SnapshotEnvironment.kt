package com.ts.ds.testsupport.snapshot

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RoborazziOptions
import org.robolectric.RuntimeEnvironment
import java.io.File

public object SnapshotEnvironment {
    public const val SNAPSHOT_DIR: String = "src/test/snapshots"
    public const val WIDTH_DP: Int = 360
    public const val HEIGHT_DP: Int = 2000
    public const val DENSITY_QUALIFIER: String = "xhdpi"
    public const val CHANGE_THRESHOLD: Float = 0.001f
    public const val LTR_LOCALE: String = "en-rUS"
    public const val RTL_LOCALE: String = "ar-rEG"

    public val roborazziOptions: RoborazziOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(changeThreshold = CHANGE_THRESHOLD),
    )

    public fun qualifiers(variant: SnapshotVariant): String = listOf(
        if (variant.rtl) RTL_LOCALE else LTR_LOCALE,
        if (variant.rtl) "ldrtl" else "ldltr",
        "w${WIDTH_DP}dp",
        "h${HEIGHT_DP}dp",
        if (variant.dark) "night" else "notnight",
        DENSITY_QUALIFIER,
    ).joinToString("-")

    public fun apply(variant: SnapshotVariant) {
        RuntimeEnvironment.setQualifiers(qualifiers(variant))
        RuntimeEnvironment.setFontScale(variant.fontScale)
        val application = ApplicationProvider.getApplicationContext<Application>()
        application.applicationInfo.flags = application.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
    }

    public fun file(testClass: Class<*>, name: String, variant: SnapshotVariant): File =
        File(SNAPSHOT_DIR, "${testClass.simpleName}/${name}_${variant.name}_sdk${Build.VERSION.SDK_INT}.png")
}
