package com.skysoft.utils.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

class SkysoftCommandRegistry(
    private val dispatcher: CommandDispatcher<FabricClientCommandSource>,
    private val rootName: String = "skysoft",
    private val rootAliases: List<String> = listOf("ss", "soft"),
) {
    private var rootCommand: Command<FabricClientCommandSource>? = null
    private val childBuilders = mutableListOf<() -> ArgumentBuilder<FabricClientCommandSource, *>>()
    private val aliases = mutableListOf<LiteralArgumentBuilder<FabricClientCommandSource>>()

    fun root(command: Command<FabricClientCommandSource>) {
        rootCommand = command
    }

    fun child(builder: () -> ArgumentBuilder<FabricClientCommandSource, *>) {
        childBuilders += builder
    }

    fun child(
        name: String,
        alias: String? = null,
        builder: (String) -> LiteralArgumentBuilder<FabricClientCommandSource>,
    ) {
        childBuilders += { builder(name) }
        alias?.let { aliases += builder(it) }
    }

    fun register() {
        (listOf(rootName) + rootAliases).forEach { name ->
            val root = literal(name)
            rootCommand?.let(root::executes)
            childBuilders.forEach { root.then(it()) }
            dispatcher.register(root)
        }
        aliases.forEach(dispatcher::register)
    }

    companion object {
        fun literal(name: String): LiteralArgumentBuilder<FabricClientCommandSource> =
            LiteralArgumentBuilder.literal(name)

        fun stringArgument(name: String): RequiredArgumentBuilder<FabricClientCommandSource, String> =
            RequiredArgumentBuilder.argument(name, StringArgumentType.greedyString())

    }
}
