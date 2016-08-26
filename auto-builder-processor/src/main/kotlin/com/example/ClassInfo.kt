package com.example

import com.squareup.javapoet.ClassName
import javax.lang.model.element.Element

data class ClassInfo(
    val packageName: String,
    val className: String,
    val builderClassName: ClassName,
    val element: Element
)