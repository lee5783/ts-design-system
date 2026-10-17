import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure

class DsPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.vanniktech.maven.publish")

            group = DsBuildConstants.GROUP
            version = dsVersion()

            val githubRepository = providers.environmentVariable("GITHUB_REPOSITORY")
                .orElse(providers.gradleProperty("ds.githubRepository"))
                .orElse("unconfigured/ts-design-system")
                .get()
            val repositoryUrl = "https://github.com/$githubRepository"
            val ownerUrl = "https://github.com/${githubRepository.substringBefore('/')}"

            extensions.configure<MavenPublishBaseExtension> {
                configureBasedOnAppliedPlugins(sourcesJar = true, javadocJar = false)
                coordinates(DsBuildConstants.GROUP, project.name, project.version.toString())
                pom {
                    name.set("TS Design System ${project.name}")
                    description.set("TinySolution design system for Android: ${project.name}")
                    url.set(repositoryUrl)
                    inceptionYear.set(DsBuildConstants.INCEPTION_YEAR)
                    licenses {
                        license {
                            name.set(dsLicenseName())
                            url.set("$repositoryUrl/blob/master/LICENSE")
                            distribution.set("repo")
                        }
                    }
                    organization {
                        name.set(providers.gradleProperty("ds.pom.organizationName"))
                        url.set(ownerUrl)
                    }
                    developers {
                        developer {
                            id.set(providers.gradleProperty("ds.pom.developerId"))
                            name.set(providers.gradleProperty("ds.pom.developerName"))
                            url.set(ownerUrl)
                        }
                    }
                    scm {
                        url.set(repositoryUrl)
                        connection.set("scm:git:$repositoryUrl.git")
                        developerConnection.set("scm:git:ssh://git@github.com/$githubRepository.git")
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("$repositoryUrl/issues")
                    }
                }
            }

            extensions.configure<PublishingExtension> {
                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri("https://maven.pkg.github.com/$githubRepository")
                        credentials {
                            username = providers.environmentVariable("GITHUB_ACTOR").orNull
                            password = providers.environmentVariable("GITHUB_TOKEN").orNull
                        }
                    }
                }
            }
        }
    }
}
