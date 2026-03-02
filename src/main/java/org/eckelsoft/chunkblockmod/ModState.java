package org.eckelsoft.chunkblockmod;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.*;

public class ModState {
    private static boolean active = false;
    private static int debugLevel = 1;
    private static boolean timerVisible = true;
    private static boolean effectsEnabled = true;
    private static int spawnInterval = 16;
    private static int maxMobs = 8;
    private static boolean replaceFluids = false;

    private static long startTime = 0;
    private static long elapsedBeforeStop = 0;
    private static String timerPrefix = "";
    private static String timerSuffix = "";

    public static boolean isActive() { return active; }
    public static void setActive(boolean active, net.minecraft.server.MinecraftServer server) {
        if (active && !ModState.active) {
            if (getElapsedTime() == 0 && server != null) {
                server.getPlayerManager().getPlayerList().forEach(player -> {
                    net.minecraft.item.ItemStack boots = new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_BOOTS);
                    boots.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                            net.minecraft.text.Text.literal("Chunk Walker").formatted(net.minecraft.util.Formatting.AQUA));

                    player.getInventory().insertStack(boots);
                });
            }
            startTime = System.currentTimeMillis();
        } else if (!active && ModState.active) {
            elapsedBeforeStop += (System.currentTimeMillis() - startTime);
        }
        ModState.active = active;
    }

    public static int getDebugLevel() { return debugLevel; }
    public static void setDebugLevel(int level) { debugLevel = level; }
    public static boolean isTimerVisible() { return timerVisible; }
    public static void setTimerVisible(boolean visible) { ModState.timerVisible = visible; }
    public static boolean areEffectsEnabled() { return effectsEnabled; }
    public static void setEffectsEnabled(boolean enabled) { effectsEnabled = enabled; }
    public static int getSpawnInterval() { return spawnInterval; }
    public static void setSpawnInterval(int interval) { spawnInterval = interval; }
    public static int getMaxMobs() { return maxMobs; }
    public static void setMaxMobs(int max) { maxMobs = max; }
    public static boolean shouldReplaceFluids() { return replaceFluids; }
    public static void setReplaceFluids(boolean replace) { replaceFluids = replace; }
    public static void resetTimer() { startTime = System.currentTimeMillis(); elapsedBeforeStop = 0; }
    public static long getElapsedTime() {
        if (!active) return elapsedBeforeStop;
        return elapsedBeforeStop + (System.currentTimeMillis() - startTime);
    }
    public static String getTimerPrefix() { return timerPrefix; }
    public static void setTimerPrefix(String prefix) { timerPrefix = prefix; }
    public static String getTimerSuffix() { return timerSuffix; }
    public static void setTimerSuffix(String suffix) { timerSuffix = suffix; }

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