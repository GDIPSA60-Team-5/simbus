// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}
subprojects {
    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            version.set((libs.versions.ktlint.get()))
            reporters {
                reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
            }
        }
    }

    plugins.withId("io.gitlab.arturbosch.detekt") {
        extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            config = files("$rootDir/detekt.yml")
            buildUponDefaultConfig = true
            reports {
                xml.required.set(true)
                xml.outputLocation.set(file("build/reports/detekt/detekt.xml"))
            }
        }
    }
}
