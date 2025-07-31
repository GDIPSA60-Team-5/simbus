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
    }
}

rootProject.name = "BusAppKotlin"
include(":app", ":core", ":feature:login")

file("feature").listFiles()
    ?.filter { it.isDirectory }
    ?.forEach { dir ->
        val moduleName = ":feature:${dir.name}"
        include(moduleName)
        project(moduleName).projectDir = dir
    }