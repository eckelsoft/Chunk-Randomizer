package org.eckelsoft.chunkblockmod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobSpawnManager {
    private static final List<EntityType<?>> DYNAMIC_MOB_POOL = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        for (EntityType<?> type : Registries.ENTITY_TYPE) {
            if (type != EntityType.ENDER_DRAGON &&
                    type != EntityType.MARKER &&
                    type != EntityType.AREA_EFFECT_CLOUD &&
                    type != EntityType.FISHING_BOBBER) {

                if (type.getSpawnGroup() != SpawnGroup.MISC) {
                    DYNAMIC_MOB_POOL.add(type);
                }
            }
        }
        DYNAMIC_MOB_POOL.add(EntityType.WITHER);
        DYNAMIC_MOB_POOL.add(EntityType.LIGHTNING_BOLT);
    }

    public static void triggerSurpriseSpawn(ServerWorld world, ServerPlayerEntity player) {
        if (DYNAMIC_MOB_POOL.isEmpty()) return;

        int amount = RANDOM.nextInt(ModState.getMaxMobs()) + 1;
        BlockPos playerPos = player.getBlockPos();

        for (int i = 0; i < amount; i++) {
            EntityType<?> type = DYNAMIC_MOB_POOL.get(RANDOM.nextInt(DYNAMIC_MOB_POOL.size()));
            BlockPos spawnPos = playerPos.add(RANDOM.nextInt(10) - 5, 1, RANDOM.nextInt(10) - 5);

            try {
                type.spawn(world, spawnPos, SpawnReason.EVENT);
            } catch (Exception e) {
                // ignore
            }
        }

        if (ModState.getDebugLevel() == 1) {
            player.sendMessage(Text.literal("§6" + amount + " mobs have spawned!").formatted(Formatting.GOLD), false);
        }
    }
}