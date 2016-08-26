package com.example

import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.FIELD
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.WARNING

@AutoService(Processor::class)
open class AutoBuilderProcessor : AbstractProcessor() {
    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var elementUtils: Elements

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer!!
        messager = processingEnv.messager!!
        elementUtils = processingEnv.elementUtils!!
    }

    override fun getSupportedAnnotationTypes() = setOf(AutoBuilder::class.java.canonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, env: RoundEnvironment): Boolean {
        env.getElementsAnnotatedWith(AutoBuilder::class.java)
            .map { getClassInfo(it) }
            .forEach { generateBuilderClass(it) }

        return false
    }

    fun getPackageName(element: Element) = elementUtils.getPackageOf(element).qualifiedName.toString()

    private fun getClassInfo(element: Element): ClassInfo {
        val packageName = getPackageName(element)
        val className = element.simpleName.toString()

        return ClassInfo(
            packageName = packageName,
            className = className,
            builderClassName = ClassName.get(packageName, "${className}Builder"),
            element = element
        )
    }

    private fun generateBuilderClass(info: ClassInfo) = with(info) {
        val builderClass = buildBuilderClass(info)

        buildFile(packageName, builderClass).run {
            try {
                writeTo(filer)
            } catch (e: IOException) {
                messager.printMessage(ERROR, "Error while processing " + className)
            }
        }
    }

    private fun buildBuilderClass(info: ClassInfo): TypeSpec = with(info) {
        messager.printMessage(WARNING, "Processing ${info.className}")

        return buildClass(builderClassName.simpleName()) {
            addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            element.enclosedElements
                .filter { it.kind == FIELD }
                .map { getFieldInfo(it, info) }
                .forEach { processField(it) }

            addMethod("build") {
                addModifiers(Modifier.PUBLIC)
                returns(TypeName.get(element.asType()))
                addStatement("return new \$T(this)", element)
            }
        }
    }

    private fun getFieldInfo(element: Element, info: ClassInfo): FieldInfo {
        return FieldInfo(
            fieldName = element.simpleName.toString(),
            fieldTypeName = TypeName.get(element.asType()),
            builderClassName = info.builderClassName
        )
    }

    private fun TypeSpec.Builder.processField(info: FieldInfo) = with(info) {
        messager.printMessage(WARNING, "Processing $fieldName")

        addField(fieldTypeName, fieldName)

        addMethod(fieldName) {
            addModifiers(Modifier.PUBLIC)
            returns(builderClassName)
            addParameter(fieldTypeName, fieldName)
            addStatement("this.\$L = \$L", fieldName, fieldName)
            addStatement("return this")
        }
    }
}
