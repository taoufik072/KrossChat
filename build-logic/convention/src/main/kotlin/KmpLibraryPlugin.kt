import com.taoufikcode.krosschat.convention.configureKotlinMultiplatform
import com.taoufikcode.krosschat.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused") // implementationClass in /convention/build.gradle.kts
class KmpLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureKotlinMultiplatform()
            dependencies {
                "commonMainImplementation"(
                    dependency = libs.findLibrary("kotlinx-serialization-json").get()
                )
                "commonTestImplementation"(dependency = libs.findLibrary("kotlin-test").get())
            }
        }
    }
}