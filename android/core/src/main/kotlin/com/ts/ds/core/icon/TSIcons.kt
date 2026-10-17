package com.ts.ds.core.icon

public val TSIcon.dsName: String
    get() = name.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), "-").lowercase()

public object TSIcons {
    private val byDsName: Map<String, TSIcon> = TSIcon.entries.associateBy { it.dsName }

    public fun fromDsName(name: String): TSIcon? = byDsName[name.trim().lowercase()]
}
