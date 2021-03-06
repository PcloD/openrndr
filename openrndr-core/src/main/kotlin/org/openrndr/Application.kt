package org.openrndr

import org.openrndr.math.Vector2
import kotlin.concurrent.thread

/**
 * PresentationMode describes modes of frame presentation
 */
enum class PresentationMode {
    /**
     * automatic presentation mode, frames are presented at highest rate possible
     */
    AUTOMATIC,
    /**
     * manual presentation mode, presentation only takes place after requesting redraw
     */
    MANUAL,
}

/**
 * Application interface
 */
abstract class Application {
    companion object {
        fun run(program: Program, configuration: Configuration) {
            val c = applicationClass(configuration)
            val application = c.declaredConstructors[0].newInstance(program, configuration) as Application
            application.setup()
            application.loop()
        }

        fun runAsync(program: Program, configuration: Configuration) {
            val c = applicationClass(configuration)
            val application = c.declaredConstructors[0].newInstance(program, configuration) as Application
            thread {
                application.setup()
                application.loop()
            }
        }

        private fun applicationClass(configuration: Configuration): Class<*> {
            return if (!configuration.headless)
                Application::class.java.classLoader.loadClass("org.openrndr.internal.gl3.ApplicationGLFWGL3")
            else
                Application::class.java.classLoader.loadClass("org.openrndr.internal.gl3.ApplicationEGLGL3")
        }
    }

    abstract fun requestDraw()

    abstract fun exit()
    abstract fun setup()

    abstract fun loop()
    abstract var clipboardContents: String?
    abstract var windowTitle: String

    abstract var windowPosition: Vector2
    abstract var cursorPosition: Vector2
    abstract var cursorVisible: Boolean
    abstract val seconds: Double

    abstract var presentationMode: PresentationMode
}

/**
 * Runs [program] as an application using [configuration].
 */
fun application(program: Program, configuration: Configuration = Configuration()) {
    Application.run(program, configuration)
}


/**
 * Resolves resource named [name] relative to [class] as a [String] based URL.
 */
fun resourceUrl(name: String, `class`: Class<*> = Application::class.java): String {
    val resource = `class`.getResource(name)
    if (resource == null) {
        throw RuntimeException("resource $name not found")
    } else {
        return `class`.getResource(name).toExternalForm()
    }
}