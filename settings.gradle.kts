// Suppress harmless IntelliJ KSP disposal race condition on AWT EventQueue in headless Gradle runs
try {
    val existingHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val isAwtKspDisposalRace = thread.name.startsWith("AWT-EventQueue") &&
            throwable is NullPointerException &&
            throwable.stackTrace.any { element ->
                element.className.contains("BinaryFileTypeDecompilers") || 
                element.className.contains("FileDocumentManager") ||
                element.className.contains("ksp.com.intellij")
            }
        if (!isAwtKspDisposalRace) {
            existingHandler?.uncaughtException(thread, throwable) ?: throwable.printStackTrace()
        }
    }
} catch (_: Throwable) {}

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

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "Noosh"

include(":app")
