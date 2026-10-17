plugins {
    `java-platform`
    alias(libs.plugins.ds.publishing)
}

dependencies {
    constraints {
        api(project(":tokens"))
        api(project(":core"))
        api(project(":compose"))
        api(project(":views"))
    }
}
