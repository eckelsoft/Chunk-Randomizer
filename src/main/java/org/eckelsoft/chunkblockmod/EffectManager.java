package org.eckelsoft.chunkblockmod;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EffectManager {
    private static final List<RegistryEntry<StatusEffect>> VALID_EFFECTS = new ArrayList<>();
    private static final Map<UUID, RegistryEntry<StatusEffect>> ACTIVE_CHUNK_EFFECTS = new HashMap<>();

    static {
        for (RegistryEntry<StatusEffect> effect : Registries.STATUS_EFFECT.getIndexedEntries()) {
            if (!effect.equals(StatusEffects.BLINDNESS) &&
                    !effect.equals(StatusEffects.DARKNESS) &&
                    !effect.equals(StatusEffects.NAUSEA) &&
                    !effect.value().isInstant()) {
                VALID_EFFECTS.add(effect);
            }
        }
    }

    public static void applyChunkEffect(ServerPlayerEntity player, ChunkPos chunkPos) {
        if (VALID_EFFECTS.isEmpty()) return;

        long seed = (long) chunkPos.x * 49632L + (long) chunkPos.z * 32517L;
        Random chunkRandom = new Random(seed);

        RegistryEntry<StatusEffect> effect = VALID_EFFECTS.get(chunkRandom.nextInt(VALID_EFFECTS.size()));
        clearChunkEffect(player);

        int amplifier = chunkRandom.nextInt(5);
        player.addStatusEffect(new StatusEffectInstance(effect, StatusEffectInstance.INFINITE, amplifier));
        ACTIVE_CHUNK_EFFECTS.put(player.getUuid(), effect);

        if (ModState.getDebugLevel() == 3) {
            player.sendMessage(Text.literal("[LOG] Chunk Effect: " + effect.value().getName().getString() + " (Duration: infinite, Amp: " + amplifier + ")").formatted(Formatting.LIGHT_PURPLE), false);
        }
    }

    public static void clearChunkEffect(ServerPlayerEntity player) {
        RegistryEntry<StatusEffect> effect = ACTIVE_CHUNK_EFFECTS.remove(player.getUuid());
        if (effect != null) {
            player.removeStatusEffect(effect);
        }
    }

    public static void clearAllChunkEffects(ServerWorld world) {
        world.getPlayers().forEach(EffectManager::clearChunkEffect);
    }
}
