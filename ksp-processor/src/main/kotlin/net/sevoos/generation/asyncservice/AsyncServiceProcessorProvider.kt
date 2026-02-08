package net.sevoos.generation.asyncservice

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import net.sevoos.generation.getPlatform

class AsyncServiceProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AsyncServiceProcessor(environment.codeGenerator, environment.logger, getPlatform(environment))
    }
}
