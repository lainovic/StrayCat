pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        val artifactoryTomtomgroupComUrl: String by extra
        val artifactoryTomtomgroupComUsername: String by extra
        val artifactoryTomtomgroupComPassword: String by extra
        maven {
            name = "TomTom Artifactory"
            url = uri(artifactoryTomtomgroupComUrl)
            credentials {
                username = artifactoryTomtomgroupComUsername
                password = artifactoryTomtomgroupComPassword
            }
        }
    }
}

rootProject.name = "Stray Cat"
include(":app")
