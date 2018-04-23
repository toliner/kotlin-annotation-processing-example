package toliner.example.annotationprocess

import org.yanex.takenoko.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind.*

const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

// kotlinをjava8向けにコンパイルするので対象のJavaのバージョンは8。
@SupportedSourceVersion(SourceVersion.RELEASE_8)
// 処理するアノテーションのクラスを完全名で指定する。
@SupportedAnnotationTypes("toliner.example.annotationprocess.ExampleAnnotation")
// 受け取るOptionのkey値を指定する。
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ExampleProcessor: AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (annotations == null || roundEnv == null) return false
        // 対象のクラスを取得する。
        val annotatedElements = roundEnv.getElementsAnnotatedWith(ExampleAnnotation::class.java)
        if (annotatedElements.isEmpty()) {
            return false
        }
        // build.gradleで指定した、生成先のディレクトリを取得する。
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }

        // takenokoのDSLを用いてkotlinのクラスのElementを生成する。
        val generatedKtFile = kotlinFile("example.generated") {
            for (element in annotatedElements) {
                val typeElement = element.toTypeElementOrNull() ?: continue
                property("message") {
                    initializer(stringLiteral(typeElement.getAnnotation(ExampleAnnotation::class.java).message))
                }
            }
        }

        // 実際にFileを生成する
        File(kaptKotlinGeneratedDir, "exampleGenerated.kt").apply {
            parentFile.mkdirs()
            writeText(generatedKtFile.accept(PrettyPrinter(PrettyPrinterConfiguration())))
        }

        return true
    }

    // ElementをTypeElementにキャストする
    private fun Element.toTypeElementOrNull(): TypeElement? {
        if (this !is TypeElement) {
            processingEnv.messager.printMessage(ERROR, "Invalid element type, class expected", this)
            return null
        }

        return this
    }
}
