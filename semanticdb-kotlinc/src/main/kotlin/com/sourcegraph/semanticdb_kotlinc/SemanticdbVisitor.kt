package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class SemanticdbVisitor(
    sourceroot: Path,
    private val resolver: DescriptorResolver,
    private val file: KtFile,
    private val lineMap: LineMap,
    private val globals: GlobalSymbolsCache,
    private val locals: LocalSymbolsCache = LocalSymbolsCache()
): KtTreeVisitorVoid() {
    private val emitter = SemanticdbTextDocumentEmitter(sourceroot, file, lineMap)

    private data class SymbolDescriptorPair(val symbol: Symbol, val descriptor: DeclarationDescriptor)

    fun build(): Semanticdb.TextDocument {
        super.visitKtFile(file)
        return emitter.buildSemanticdbTextDocument()
    }

    private fun Sequence<SymbolDescriptorPair>?.emitAll(element: PsiElement, role: Role): List<Symbol>? = this?.onEach { (symbol, descriptor) ->
        emitter.emitSemanticdbData(symbol, descriptor, element, role)
    }?.map { it.symbol }?.toList()

    private fun Sequence<Symbol>.with(descriptor: DeclarationDescriptor) = this.map { SymbolDescriptorPair(it, descriptor) }

    override fun visitKtElement(element: KtElement) {
        try {
            super.visitKtElement(element)
        } catch (e: VisitorException) {
            throw e
        } catch (e: Exception) {
            throw VisitorException("exception throw when visiting ${element::class} in ${file.virtualFilePath}: (${lineMap.lineNumber(element)}, ${lineMap.startCharacter(element)})", e)
        }
    }

    override fun visitClass(klass: KtClass) {
        val desc = resolver.fromDeclaration(klass).single()
        var symbols = globals[desc, locals].with(desc).emitAll(klass, Role.DEFINITION)
        if (!klass.hasExplicitPrimaryConstructor()) {
            resolver.syntheticConstructor(klass)?.apply {
                symbols = globals[this, locals].with(this).emitAll(klass, Role.DEFINITION)
            }
        }
        super.visitClass(klass)
    }

    override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor) {
        val desc = resolver.fromDeclaration(constructor).single()
        // if the constructor is not denoted by the 'constructor' keyword, we want to link it to the class ident
        val symbols = if (!constructor.hasConstructorKeyword()) {
            globals[desc, locals].with(desc).emitAll(constructor.containingClass()!!, Role.DEFINITION)
        } else {
            globals[desc, locals].with(desc).emitAll(constructor.getConstructorKeyword()!!, Role.DEFINITION)
        }
        println("PRIMARY CONSTRUCTOR ${constructor.identifyingElement?.parent ?: constructor.containingClass()} ${desc.name} $symbols")
        super.visitPrimaryConstructor(constructor)
    }

    override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {
        val desc = resolver.fromDeclaration(constructor).single()
        val symbols = globals[desc, locals].with(desc).emitAll(constructor.getConstructorKeyword(), Role.DEFINITION)
        super.visitSecondaryConstructor(constructor)
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        val desc = resolver.fromDeclaration(function).single()
        val symbols = globals[desc, locals].with(desc).emitAll(function, Role.DEFINITION)
        super.visitNamedFunction(function)
    }

    override fun visitProperty(property: KtProperty) {
        val desc = resolver.fromDeclaration(property).single()
        val symbols = globals[desc, locals].with(desc).emitAll(property, Role.DEFINITION)
        super.visitProperty(property)
    }

    override fun visitParameter(parameter: KtParameter) {
        val symbols = resolver.fromDeclaration(parameter).flatMap { desc ->
            globals[desc, locals].with(desc)
        }.emitAll(parameter, Role.DEFINITION)
        println("NAMED PARAM $parameter $symbols")
        super.visitParameter(parameter)
    }

    override fun visitTypeParameter(parameter: KtTypeParameter) {
        val desc = resolver.fromDeclaration(parameter).single()
        val symbols = globals[desc, locals].with(desc).emitAll(parameter, Role.DEFINITION)
        super.visitTypeParameter(parameter)
    }

    override fun visitTypeAlias(typeAlias: KtTypeAlias) {
        val desc = resolver.fromDeclaration(typeAlias).single()
        val symbols = globals[desc, locals].with(desc).emitAll(typeAlias, Role.DEFINITION)
        super.visitTypeAlias(typeAlias)
    }

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        val desc = resolver.fromDeclaration(accessor).single()
        val symbols = globals[desc, locals].with(desc).emitAll(accessor, Role.DEFINITION)
        super.visitPropertyAccessor(accessor)
    }

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
        val desc = resolver.fromReference(expression) ?: run {
            println("NULL DESCRIPTOR FROM NAME EXPRESSION $expression ${expression.javaClass}")
            super.visitSimpleNameExpression(expression)
            return
        }
        val symbols = globals[desc, locals].with(desc).emitAll(expression, Role.REFERENCE)
        super.visitSimpleNameExpression(expression)
    }
}

class VisitorException(msg: String, throwable: Throwable): Exception(msg, throwable)