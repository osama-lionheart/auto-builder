package com.example

import com.squareup.javapoet.*

fun buildFile(packageName: String, typeSpec: TypeSpec): JavaFile {
    return JavaFile.builder(packageName, typeSpec).build()
}

fun buildClass(name: String, block: TypeSpec.Builder.() -> Unit): TypeSpec {
    return TypeSpec.classBuilder(name).apply {
        block()
    }.build()
}

fun TypeSpec.Builder.addMethod(name: String, block: MethodSpec.Builder.() -> Unit) {
    addMethod(MethodSpec.methodBuilder(name).apply {
        block()
    }.build())
}

fun TypeSpec.Builder.addField(typeName: TypeName, name: String) {
    addField(FieldSpec.builder(typeName, name).build())
}

fun TypeSpec.Builder.addField(typeName: TypeName, name: String, block: FieldSpec.Builder.() -> Unit) {
    addField(FieldSpec.builder(typeName, name).apply {
        block()
    }.build())
}