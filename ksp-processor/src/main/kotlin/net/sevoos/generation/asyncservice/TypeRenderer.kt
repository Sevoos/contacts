package net.sevoos.generation.asyncservice

import com.google.devtools.ksp.symbol.*

object TypeRenderer {

    fun render(
        type: KSType,
        currentPackage: String,
        imports: MutableSet<String>
    ): String {
        val decl = type.declaration
        val pkg = decl.packageName.asString()
        val simple = decl.simpleName.asString()

        if (pkg != currentPackage) {
            imports.add("$pkg.$simple")
        }

        val rendered = if (type.arguments.isEmpty()) {
            simple
        } else {
            val args = type.arguments.joinToString(", ") {
                renderProjection(it, currentPackage, imports)
            }
            "$simple<$args>"
        }

        return if (type.isMarkedNullable) "$rendered?" else rendered
    }

    fun render(
        typeRef: KSTypeReference,
        currentPackage: String,
        imports: MutableSet<String>
    ): String {
        return render(typeRef.resolve(), currentPackage, imports)
    }

    private fun renderProjection(
        projection: KSTypeArgument,
        currentPackage: String,
        imports: MutableSet<String>
    ): String {
        val variance = projection.variance
        val type = projection.type?.resolve()

        return when {
            type == null -> "*"      // star projection
            variance == Variance.COVARIANT ->
                "out ${render(type, currentPackage, imports)}"
            variance == Variance.CONTRAVARIANT ->
                "in ${render(type, currentPackage, imports)}"
            else -> render(type, currentPackage, imports)
        }
    }
}
