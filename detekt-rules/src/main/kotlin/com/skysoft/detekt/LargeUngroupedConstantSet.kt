package com.skysoft.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty

class LargeUngroupedConstantSet(config: Config) : Rule(
    config,
    "Large groups of loose constants should be grouped by concept.",
) {
    private val threshold: Int = config.valueOrDefault(THRESHOLD_CONFIG_KEY, DEFAULT_THRESHOLD)

    override fun visitKtFile(file: KtFile) {
        inspectScope(
            scopeName = file.name,
            entity = Entity.atPackageOrFirstDecl(file),
            declarations = file.declarations,
        )
        super.visitKtFile(file)
    }

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        val scopeName = classOrObject.name ?: return super.visitClassOrObject(classOrObject)
        if (!classOrObject.isNamedConstantGroup()) {
            inspectScope(
                scopeName = scopeName,
                entity = Entity.atName(classOrObject),
                declarations = classOrObject.declarations,
            )
        }
        super.visitClassOrObject(classOrObject)
    }

    private fun inspectScope(scopeName: String, entity: Entity, declarations: List<KtDeclaration>) {
        val constants = declarations.filterIsInstance<KtProperty>().filter { property -> property.isConstantLike() }
        if (constants.size < threshold) return
        report(
            Finding(
                entity,
                "Scope '$scopeName' has ${constants.size} loose constants. " +
                    "Group related values into named concept objects.",
            ),
        )
    }

    private fun KtClassOrObject.isNamedConstantGroup(): Boolean {
        if (this !is KtObjectDeclaration) return false
        if (isGenericConstantGroupName(name)) return false
        val directDeclarations = declarations
        return directDeclarations.isNotEmpty() && directDeclarations.all { declaration ->
            declaration is KtProperty && declaration.isConstantLike()
        }
    }

    private fun KtProperty.isConstantLike(): Boolean {
        if (isVar) return false
        val name = name ?: return false
        if (!hasModifier(KtTokens.CONST_KEYWORD) && !CONSTANT_NAME_PATTERN.matches(name)) return false
        return initializer?.isDataTableOrParserInitializer() != true
    }

    private fun isGenericConstantGroupName(name: String?): Boolean =
        name == null || name in GENERIC_CONSTANT_GROUP_NAMES

    private fun String.isKnownDataOrParserFactory(): Boolean =
        DATA_OR_PARSER_FACTORIES.any { factory -> startsWith(factory) }

    private fun KtExpression.isDataTableOrParserInitializer(): Boolean {
        val text = text.trimStart()
        return text.isKnownDataOrParserFactory() || DATA_OR_PARSER_DERIVED_PATTERNS.any { pattern -> pattern in text }
    }

    private companion object {
        const val THRESHOLD_CONFIG_KEY = "threshold"
        const val DEFAULT_THRESHOLD = 15

        val CONSTANT_NAME_PATTERN = Regex("""[A-Z][A-Z0-9_]*""")

        val GENERIC_CONSTANT_GROUP_NAMES = setOf(
            "CommonConstants",
            "Constants",
            "GeneralConstants",
            "MiscConstants",
        )

        val DATA_OR_PARSER_FACTORIES = listOf(
            "arrayOf(",
            "buildList(",
            "buildMap(",
            "buildSet(",
            "listOf(",
            "mapOf(",
            "mutableListOf(",
            "mutableMapOf(",
            "mutableSetOf(",
            "Regex(",
            "setOf(",
        )

        val DATA_OR_PARSER_DERIVED_PATTERNS = listOf(
            ".associate",
            ".entries",
            ".flatMap",
            ".map",
        )
    }
}
