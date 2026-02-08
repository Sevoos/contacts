package net.sevoos.generation.asyncservice

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

class AsyncServiceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val platform: String
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(
            "net.sevoos.generation.annotations.AsynchronousService"
        )

        val (valid, _) = symbols.partition { it is KSClassDeclaration && it.validate() }

        valid.forEach { cls ->
            processClass(cls as KSClassDeclaration)
        }

        return emptyList()
    }

    private fun processClass(classDecl: KSClassDeclaration) {
        val pkg = classDecl.packageName.asString()
        val name = classDecl.simpleName.asString()
        val base = name.removeSuffix("AsynchronousService")

        val ctor = classDecl.primaryConstructor
            ?: return logger.error("$name must have primary constructor")

        val ctorParams = ctor.parameters.map { p ->
            ConstructorParam(
                p.name!!.asString(),
                p.type
            )
        }

        val functions = classDecl.getAllFunctions()
            .filter { it.modifiers.contains(Modifier.SUSPEND) && it.isMemberFunction() && it.isPublic() }
            .map { f ->
                FunctionDescriptor(
                    f.simpleName.asString(),
                    f.parameters.map { Param(it.name!!.asString(), it.type.resolve()) },
                    f.returnType!!.resolve()
                )
            }

        val functionsList = functions.toList()
        when (platform.lowercase()) {
            "jvm" -> generateJvm(pkg, base, name, ctorParams, functionsList)
            "js"  -> generateJs(pkg, base, name, ctorParams, functionsList)
            else -> {
                generateJvm(pkg, base, name, ctorParams, functionsList)
                generateJs(pkg, base, name, ctorParams, functionsList)
            }
        }
    }

    // ---------------- SHARED CORE ----------------

    private fun writeHeader(
        w: OutputStreamWriter,
        pkg: String,
        imports: Set<String>
    ) {
        w.appendLine("package $pkg")
        w.appendLine()
        imports.sorted().forEach { w.appendLine("import $it") }
        w.appendLine()
    }

    private fun collectImports(
        imports: MutableSet<String>,
        pkg: String,
        ctorParams: List<ConstructorParam>,
        functions: List<FunctionDescriptor>
    ) {
        ctorParams.forEach { TypeRenderer.render(it.typeRef, pkg, imports) }
        functions.forEach { f ->
            TypeRenderer.render(f.returnType, pkg, imports)
            f.params.forEach { p -> TypeRenderer.render(p.type, pkg, imports) }
        }
    }

    private fun paramDecls(
        params: List<Param>,
        pkg: String,
        imports: MutableSet<String>
    ): String =
        params.joinToString(", ") {
            "${it.name}: ${TypeRenderer.render(it.type, pkg, imports)}"
        }

    private fun ctorDecls(
        params: List<ConstructorParam>,
        pkg: String,
        imports: MutableSet<String>
    ): String =
        params.joinToString(", ") {
            "${it.name}: ${TypeRenderer.render(it.typeRef, pkg, imports)}"
        }

    // ---------------- JVM ----------------

    private fun generateJvm(
        pkg: String,
        base: String,
        original: String,
        ctorParams: List<ConstructorParam>,
        functions: List<FunctionDescriptor>
    ) {
        val className = "${base}Service"

        val imports = mutableSetOf(
            "kotlinx.coroutines.runBlocking"
        )
        collectImports(imports, pkg, ctorParams, functions)

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            pkg,
            className
        )

        OutputStreamWriter(file, "UTF-8").use { w ->
            writeHeader(w, pkg, imports)

            w.append("class $className(")
            w.append(ctorDecls(ctorParams, pkg, imports))
            w.appendLine(") {")
            w.appendLine()
            w.appendLine("    private val asynchronousService = $original(${ctorParams.joinToString { it.name }})")
            w.appendLine()

            for (f in functions) {
                val returnStr = TypeRenderer.render(f.returnType, pkg, imports)

                w.appendLine("    fun ${f.name}(${paramDecls(f.params, pkg, imports)}): $returnStr = runBlocking {")
                w.appendLine("        asynchronousService.${f.name}(${f.params.joinToString { it.name }})")
                w.appendLine("    }")
                w.appendLine()
            }

            w.appendLine("}")
        }
    }

    // ---------------- JS ----------------

    private fun generateJs(
        pkg: String,
        base: String,
        original: String,
        ctorParams: List<ConstructorParam>,
        functions: List<FunctionDescriptor>
    ) {
        val className = "${base}FrontendService"

        val imports = mutableSetOf(
            "kotlin.js.Promise",
            "kotlinx.coroutines.CoroutineScope",
            "kotlinx.coroutines.Dispatchers",
            "kotlinx.coroutines.promise",
            "kotlinx.coroutines.cancel"
        )
        collectImports(imports, pkg, ctorParams, functions)

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            pkg,
            className
        )

        OutputStreamWriter(file, "UTF-8").use { w ->
            writeHeader(w, pkg, imports)

            w.appendLine("@JsExport")
            w.append("class $className(")
            w.append(ctorDecls(ctorParams, pkg, imports))
            w.appendLine(") {")
            w.appendLine()
            w.appendLine("    private val asynchronousService = $original(${ctorParams.joinToString { it.name }})")
            w.appendLine("    private val scope = CoroutineScope(Dispatchers.Default)")
            w.appendLine()

            for (f in functions) {
                val returnStr = TypeRenderer.render(f.returnType, pkg, imports)

                w.appendLine("    fun ${f.name}(${paramDecls(f.params, pkg, imports)}): Promise<$returnStr> =")
                w.appendLine("        scope.promise { asynchronousService.${f.name}(${f.params.joinToString { it.name }}) }")
                w.appendLine()
            }

            w.appendLine("    fun close() {")
            w.appendLine("        try {")
            w.appendLine("            scope.cancel()")
            w.appendLine("        } catch (_: Throwable) {")
            w.appendLine()
            w.appendLine("        }")
            w.appendLine("    }")
            w.appendLine()
            w.appendLine("}")
        }
    }

    // ---------------- helpers ----------------

    private data class ConstructorParam(
        val name: String,
        val typeRef: KSTypeReference
    )

    private data class Param(
        val name: String,
        val type: KSType
    )

    private data class FunctionDescriptor(
        val name: String,
        val params: List<Param>,
        val returnType: KSType
    )

    private fun KSFunctionDeclaration.isMemberFunction() =
        parentDeclaration != null

    private fun KSFunctionDeclaration.isPublic(): Boolean =
        modifiers.contains(Modifier.PUBLIC) ||
                (!modifiers.contains(Modifier.PRIVATE) && !modifiers.contains(Modifier.INTERNAL))
}
