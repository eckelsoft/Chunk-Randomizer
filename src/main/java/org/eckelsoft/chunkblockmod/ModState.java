package org.eckelsoft.chunkblockmod;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.*;

public class ModState {
    private static boolean active = false;
    private static int debugLevel = 1;
    private static boolean effectsEnabled = false;
    private static boolean replaceFluids = false;
    private static boolean chunkWalkerBootsGranted = false;

    public static boolean isActive() { return active; }
    public static void setActive(boolean active, net.minecraft.server.MinecraftServer server) {
        if (!active && ModState.active && server != null) {
            server.getWorlds().forEach(EffectManager::clearAllChunkEffects);
        }
        if (active && !chunkWalkerBootsGranted && server != null) {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                net.minecraft.item.ItemStack boots = new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_BOOTS);
                boots.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                        net.minecraft.text.Text.literal("Chunk Walker").formatted(net.minecraft.util.Formatting.AQUA));

                player.getInventory().insertStack(boots);
            });
            chunkWalkerBootsGranted = true;
        }
        ModState.active = active;
    }

    public static int getDebugLevel() { return debugLevel; }
    public static void setDebugLevel(int level) { debugLevel = level; }
    public static boolean areEffectsEnabled() { return effectsEnabled; }
    public static void setEffectsEnabled(boolean enabled) { effectsEnabled = enabled; }
    public static boolean shouldReplaceFluids() { return replaceFluids; }
    public static void setReplaceFluids(boolean replace) { replaceFluids = replace; }

    // Discord user request - manually exclude replace-blocks
    private static final Set<Block> customBlacklist = new HashSet<>();
    public static void addToBlacklist(Block block) {
        customBlacklist.add(block);
    }
    public static void clearCustomBlacklist() {
        customBlacklist.clear();
    }
    public static Set<Block> getCustomBlacklist() {
        return customBlacklist;
    }
}
