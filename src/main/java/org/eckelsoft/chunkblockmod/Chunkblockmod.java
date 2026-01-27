package org.eckelsoft.chunkblockmod;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Chunkblockmod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long timeMs = ModState.getElapsedTime();
            long sec = (timeMs / 1000) % 60;
            long min = (timeMs / (1000 * 60)) % 60;
            long hr = (timeMs / (1000 * 60 * 60));
            String timeStr = String.format("%02d:%02d:%02d", hr, min, sec);

            String prefix = ModState.getTimerPrefix().isEmpty() ? "" : ModState.getTimerPrefix() + " ";
            String suffix = ModState.getTimerSuffix().isEmpty() ? "" : " " + ModState.getTimerSuffix();
            String fullDisplay = prefix + timeStr + suffix;

            Text message = !ModState.isActive() ?
                    (timeMs == 0 ?
                            Text.literal("Type \"/rc start\" to begin. Config: \"/rc effects|timer|spawnmonster\"").formatted(Formatting.YELLOW) :
                            Text.literal("Paused: " + fullDisplay).formatted(Formatting.RED)) :
                    Text.literal(fullDisplay).formatted(Formatting.GREEN);

            if (ModState.isTimerVisible()) {
                server.getPlayerManager().getPlayerList().forEach(p -> p.sendMessage(message, true));
            }
            server.getWorlds().forEach(ChunkReplacementManager::tick);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("rc")
                    .then(CommandManager.literal("start")
                            .executes(c -> {
                                ModState.setActive(true, c.getSource().getServer());
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("stop")
                            .executes(c -> {
                                ModState.setActive(false, c.getSource().getServer());
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

                    .then(CommandManager.literal("reset")
                            .executes(c -> {
                                ModState.resetTimer();
                                c.getSource().sendFeedback(() -> Text.literal("Timer reset to 00:00:00"), false);
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("effects")
                            .executes(c -> {
                                ModState.setEffectsEnabled(!ModState.areEffectsEnabled());
                                c.getSource().sendFeedback(
                                        () -> Text.literal("Effects: " + ModState.areEffectsEnabled()), false
                                );
                                return 1;
                            })
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
                                            case 1 -> msg = "ON (Mobs only)";
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

                    .then(CommandManager.literal("spawnmonster")
                            .then(CommandManager.literal("chunks")
                                    .then(CommandManager.argument("val", IntegerArgumentType.integer(1))
                                            .executes(c -> {
                                                ModState.setSpawnInterval(
                                                        IntegerArgumentType.getInteger(c, "val")
                                                );
                                                return 1;
                                            })
                                    )
                            )
                            .then(CommandManager.literal("max")
                                    .then(CommandManager.argument("val", IntegerArgumentType.integer(1, 64))
                                            .executes(c -> {
                                                ModState.setMaxMobs(
                                                        IntegerArgumentType.getInteger(c, "val")
                                                );
                                                return 1;
                                            })
                                    )
                            )
                    )

                    .then(CommandManager.literal("timer")
                            .then(CommandManager.literal("prefix")
                                    .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                            .executes(c -> {
                                                ModState.setTimerPrefix(
                                                        StringArgumentType.getString(c, "text")
                                                );
                                                return 1;
                                            })
                                    )
                            )
                            .then(CommandManager.literal("suffix")
                                    .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                            .executes(c -> {
                                                ModState.setTimerSuffix(
                                                        StringArgumentType.getString(c, "text")
                                                );
                                                return 1;
                                            })
                                    )
                            )
                            .executes(c -> {
                                ModState.setTimerVisible(!ModState.isTimerVisible());
                                return 1;
                            })
                    )
            );
        });
    }
}