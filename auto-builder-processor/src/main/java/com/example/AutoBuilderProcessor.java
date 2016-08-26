package com.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

@AutoService(Processor.class)
public class AutoBuilderProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new HashSet<>();
        annotations.add(AutoBuilder.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(AutoBuilder.class)) {
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            ClassName builderClassName = ClassName.get(packageName, className + "Builder");

            messager.printMessage(WARNING, "Processing " + className);

            List<MethodSpec> methodSpecs = new ArrayList<>();
            List<FieldSpec> fieldSpecs = new ArrayList<>();


            for (Element el : element.getEnclosedElements()) {
                if (el.getKind() == ElementKind.FIELD) {
                    String fieldName = el.getSimpleName().toString();
                    messager.printMessage(WARNING, "Processing " + fieldName);

                    TypeName fieldTypeName = TypeName.get(el.asType());

                    FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, fieldName).build();
                    fieldSpecs.add(fieldSpec);

                    MethodSpec fieldBuilderMethod = MethodSpec.methodBuilder(fieldName)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(builderClassName)
                            .addParameter(fieldTypeName, fieldName)
                            .addStatement("this.$L = $L;", fieldName, fieldName)
                            .addStatement("return this")
                            .build();

                    methodSpecs.add(fieldBuilderMethod);
                }
            }

            MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.get(element.asType()))
                    .addStatement("return new $T(this)", element)
                    .build();

            TypeSpec builderClass = TypeSpec.classBuilder(builderClassName.simpleName())
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addFields(fieldSpecs)
                    .addMethods(methodSpecs)
                    .addMethod(buildMethod)
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, builderClass)
                    .build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(ERROR, "Error while processing " + element.getSimpleName());
            }
        }

        return false;
    }
}
