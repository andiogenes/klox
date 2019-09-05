package com.andiogenes.tool

import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: generate_ast <output directory>")
        exitProcess(1)
    }
    val outputDir = args[0]
    defineAst(
        outputDir, "Expr", listOf(
            "Assign   : Token name, Expr value",
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right",
            "Variable : Token name"
        )
    )

    defineAst(
        outputDir, "Stmt", listOf(
            "Block      : List<Stmt?> statements",
            "Expression : Expr expression",
            "Print      : Expr expression",
            "Var        : Token name, Expr? initializer"
        )
    )
}

private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("package com.andiogenes.lox")
    writer.println()

    writer.println()
    writer.println("sealed class $baseName {")

    defineVisitor(writer, baseName, types)
    writer.println()

    // The AST classes.
    for (type in types) {
        val className = type.split(":")[0].trim()
        val fields = type.split(":")[1].trim()
        defineType(writer, baseName, className, fields)
    }
    writer.println()

    // The base accept() method.
    writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")
    writer.println("}")

    writer.close()
}

private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println("    interface Visitor<out R> {")
    for (type in types) {
        val typeName = type.split(":")[0].trim()
        writer.println("        fun visit$typeName$baseName(${baseName.toLowerCase()}: $typeName): R")
    }
    writer.println("    }")
}

private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
    val formattedParameters = fieldList.split(", ").map {
        val decl = it.split(" ")
        val result = "val ${decl[1]}: ${translateType(decl[0])}"
        result
    }.foldRight("", { elem: String, acc: String ->
        val result = if (acc == "") elem else "$elem, $acc"
        result
    })

    writer.println("    data class $className($formattedParameters): $baseName() {")
    // Visitor pattern.
    writer.println("        override fun <R> accept(visitor: Visitor<R>): R {")
    writer.println("            return visitor.visit$className$baseName(this)")
    writer.println("        }")
    writer.println("    }")
}

private fun translateType(javaType: String): String = when (javaType) {
    "Object" -> "Any?"
    else -> javaType
}