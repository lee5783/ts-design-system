package com.tinysol.designsystem.core.style

public fun interface TSStyleSpec<in C, out S> {
    public fun resolve(configuration: C, state: TSControlState): S
}

public fun <C, S> TSStyleSpec<C, S>.resolve(configuration: C): S = resolve(configuration, TSControlState.Default)
