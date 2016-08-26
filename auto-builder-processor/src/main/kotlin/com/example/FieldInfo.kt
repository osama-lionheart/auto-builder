package com.example

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName

data class FieldInfo(
    val fieldName: String,
    val fieldTypeName: TypeName,
    val builderClassName: ClassName
)