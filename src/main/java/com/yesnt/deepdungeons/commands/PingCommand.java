package com.yesnt.deepdungeons.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.dimension.ModDimensions;
import com.yesnt.deepdungeons.dimension.TestChunkGenerator;
import com.yesnt.deepdungeons.dimension.VoidChunkGenerator;
import com.yesnt.deepdungeons.dimension.tools.DynamicDimensionManager;
import com.yesnt.utils.NanoID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.commands.Commands.*;

public class PingCommand {
    public PingCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                root
                    .then(testLiteral)
                    .then(createDungeon)
                    .then(listDimensions)
                    .then(deleteDimension)
                    .then(teleportTo)
        );

    }
    LiteralArgumentBuilder<CommandSourceStack> root = literal(DeepDungeons.MODID);
    LiteralArgumentBuilder<CommandSourceStack> testLiteral = literal("test")
            .executes(c -> {
                ServerPlayer player = c.getSource().getPlayer();
                assert player != null;
                c.getSource().sendSuccess(Component.literal("Buenos Dias!"), true);
                player.sendSystemMessage(Component.literal("Buenos Dias"));
                player.setGameMode(player.gameMode.getGameModeForPlayer() == GameType.CREATIVE? GameType.SURVIVAL : GameType.CREATIVE);
                return 1;
            });
    LiteralArgumentBuilder<CommandSourceStack> createDungeon = literal("createDungeon")
            .executes(c -> {
                ServerPlayer player = c.getSource().getPlayer();
                assert player != null;
                MinecraftServer server = player.getServer();
                assert server != null;

                RegistryAccess registry = server.registryAccess();
                Holder<DimensionType> type = registry.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(ModDimensions.DUNGEON_TYPE);
                ResourceKey<Level> levelKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(DeepDungeons.MODID, "dungeon_" + NanoID.randomNanoId()));

                DynamicDimensionManager.getOrCreateLevel(server, levelKey, ((minecraftServer, levelStemResourceKey) -> {
                    return new LevelStem(type, new VoidChunkGenerator(registry));
                }));

                player.sendSystemMessage(Component.literal("dimensionCreated"), true);
                return 0;
            }

    );

    LiteralArgumentBuilder<CommandSourceStack> listDimensions = literal("listDimensions")
            .executes(c -> {
                ServerPlayer player = c.getSource().getPlayer();
                assert player != null;
                MinecraftServer server = player.getServer();
                assert server != null;

                String out = getAllDimensions(server);
                c.getSource().sendSuccess(Component.literal(out.toString()), true);
                return 0;
            });

    SuggestionProvider<CommandSourceStack> provider = (commandContext, suggestionsBuilder) -> {
        String out = getAllDimensions(commandContext.getSource().getServer());
        String[] dims = out.split("\n");
        for (String dim : dims) {
            suggestionsBuilder.suggest(dim);
        }
        return suggestionsBuilder.buildFuture();
    };

    LiteralArgumentBuilder<CommandSourceStack> deleteDimension = literal("deleteDimension")
            .then(
                    argument("id", string()).suggests(provider)
                            .executes(c -> {
                                String id = getString(c, "id");
                                DynamicDimensionManager.markDimensionForUnregistration(c.getSource().getServer(), ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(DeepDungeons.MODID, id)));
                                DynamicDimensionManager.unregisterScheduledDimensions(c.getSource().getServer());
                                return 0;
                            })
            );
    LiteralArgumentBuilder<CommandSourceStack> teleportTo = literal("teleportTo")
            .then(
                    argument("id", string()).suggests(provider)
                            .executes(c -> {
                                ServerPlayer player = c.getSource().getPlayer();
                                @SuppressWarnings("deprecation") // forgeGetWorldMap is deprecated because it's a forge-internal-use-only method
                                final Map<ResourceKey<Level>, ServerLevel> map = c.getSource().getServer().forgeGetWorldMap();

                                // if the level already exists, return it
                                final ServerLevel existingLevel = map.get(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(DeepDungeons.MODID, getString(c, "id"))));
                                assert player != null;
                                player.teleportTo(existingLevel, player.getX(), player.getY(), player.getZ(), player.getXRot(), player.getYRot());
                                return 0;
                    })
            );


    private String getAllDimensions(MinecraftServer server) {
        @SuppressWarnings("deprecation") // forgeGetWorldMap is deprecated because it's a forge-internal-use-only method
        final Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();
        StringBuilder out = new StringBuilder();
        for (Map.Entry<ResourceKey<Level>, ServerLevel> entry : map.entrySet()) {
            ResourceLocation key = entry.getKey().location();
            if(key.getNamespace().equals(DeepDungeons.MODID)) out.append(key.toString().split(":")[1]).append("\n");
        }
        return out.toString();
    }
}
