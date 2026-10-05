// Top-level build file where you can add configuration options common to all sub-projects/modules.
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

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}
