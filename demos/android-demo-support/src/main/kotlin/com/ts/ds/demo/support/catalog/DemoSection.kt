package com.ts.ds.demo.support.catalog

import com.ts.ds.demo.support.R

public enum class DemoSectionId(
    public val id: String,
    public val titleRes: Int,
) {
    Foundations("foundations", R.string.demo_section_foundations),
    Actions("actions", R.string.demo_section_actions),
    Forms("forms", R.string.demo_section_forms),
    Navigation("navigation", R.string.demo_section_navigation),
    Content("content", R.string.demo_section_content),
    Feedback("feedback", R.string.demo_section_feedback),
}

public data class DemoSection(
    val id: DemoSectionId,
    val pages: List<DemoPageId>,
)
