package net.sevoos.generation

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

fun getPlatform(environment: SymbolProcessorEnvironment): String = environment.platforms.single().platformName