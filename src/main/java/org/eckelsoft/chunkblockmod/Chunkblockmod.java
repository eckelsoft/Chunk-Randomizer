package org.eckelsoft.chunkblockmod;

import net.minecraft.block.*;
import net.minecraft.command.argument.*;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class Chunkblockmod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getWorlds().forEach(ChunkReplacementManager::tick));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("rc")
                    .then(CommandManager.literal("start")
                            .executes(c -> {
                                ModState.setActive(true, c.getSource().getServer());
                                c.getSource().sendFeedback(() -> Text.literal("Chunk Randomizer started."), false);
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("stop")
                            .executes(c -> {
                                ModState.setActive(false, c.getSource().getServer());
                                c.getSource().sendFeedback(() -> Text.literal("Chunk Randomizer stopped."), false);
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("fluids")
                            .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(c -> {
                                        boolean enabled = BoolArgumentType.getBool(c, "enabled");
                                        ModState.setReplaceFluids(enabled);
                                        c.getSource().sendFeedback(() -> Text.literal("Replace Fluids: " + enabled), false);
                                        return 1;
                                    })
                            )
                    )

                    .then(CommandManager.literal("effects")
                            .executes(c -> {
                                boolean enabled = !ModState.areEffectsEnabled();
                                ModState.setEffectsEnabled(enabled);
                                if (!enabled) {
                                    c.getSource().getServer().getWorlds().forEach(EffectManager::clearAllChunkEffects);
                                }
                                c.getSource().sendFeedback(
                                        () -> Text.literal("Effects: " + ModState.areEffectsEnabled()), false
                                );
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("exclude")
                            .executes(c -> {
                                c.getSource().sendFeedback(() -> Text.literal("USE: /rc exclude add <block> OR: /rc exclude clear"), false);
                                return 1;
                            })
                            .then(CommandManager.literal("add")
                                    .then(CommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                            .executes(c -> {
                                                Block block = BlockStateArgumentType.getBlockState(c, "block").getBlockState().getBlock();
                                                ModState.addToBlacklist(block);
                                                c.getSource().sendFeedback(() -> Text.literal("§6[ChunkMod] §f" + block.getName().getString() + " will be skipped from now on."), true);
                                                return 1;
                                            })
                                    )
                            )
                            .then(CommandManager.literal("clear")
                                    .executes(c -> {
                                        ModState.clearCustomBlacklist();
                                        ChunkReplacementManager.reloadBlocks(c.getSource().getWorld());
                                        c.getSource().sendFeedback(() -> Text.literal("Custom blacklist cleared."), true);
                                        return 1;
                                    })
                            )
                    )

                    .then(CommandManager.literal("debug")
                            .executes(c -> {
                                ModState.setDebugLevel(0);
                                c.getSource().sendFeedback(() -> Text.literal("Debug mode: OFF"), false);
                                return 1;
                            })
                            .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 3))
                                    .executes(c -> {
                                        int level = IntegerArgumentType.getInteger(c, "level");
                                        ModState.setDebugLevel(level);
                                        String msg;
                                        switch (level) {
                                            case 1 -> msg = "ON (General)";
                                            case 2 -> msg = "ON (Chunk Blocktype only)";
                                            case 3 -> msg = "ON (Effects/Buffs only)";
                                            default -> msg = "OFF";
                                        }
                                        c.getSource().sendFeedback(
                                                () -> Text.literal("Debug Level set to: " + msg), false
                                        );
                                        return 1;
                                    })
                            )
                    )
            );
        });
    }
}
