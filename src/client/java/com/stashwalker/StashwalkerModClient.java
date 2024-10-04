package com.stashwalker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;

import org.lwjgl.glfw.GLFW;
import com.stashwalker.constants.Constants;
import com.stashwalker.containers.Pair;
import com.stashwalker.utils.SignTextExtractor;
import com.stashwalker.mixininterfaces.IBossBarHudMixin;
import com.stashwalker.utils.DaemonThreadFactory;
import com.stashwalker.utils.FinderUtil;

import java.awt.Color;

@Environment(EnvType.CLIENT)
public class StashwalkerModClient implements ClientModInitializer {

    private ExecutorService blockThreadPool = Executors.newFixedThreadPool(1, new DaemonThreadFactory());
    private ExecutorService entityThreadPool = Executors.newFixedThreadPool(1, new DaemonThreadFactory());
    private ExecutorService chunkLoadThreadPool = Executors.newFixedThreadPool(5, new DaemonThreadFactory());

    private KeyBinding keyBindingEntityTracers;
    private KeyBinding keyBindingBlockTracers;
    private KeyBinding keyBindingNewChunks;
    private KeyBinding keyBindingSignReader;
    private KeyBinding keyBindingAlteredDungeons;
    private boolean entityTracersWasPressed;
    private boolean blockTracersWasPressed;
    private boolean newChunksWasPressed;
    private boolean signReaderWasPressed;
    private boolean alteredDungeonsWasPressed;
    private boolean wasInGame = false;
    private long lastTime = 0;
    private RegistryKey<World> previousWorld = null;

    @Override
    public void onInitializeClient () {

        Constants.CONFIG_MANAGER.loadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Constants.CONFIG_MANAGER.saveConfig()));

        registerKeyBindings();

        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTickStartEvent);
        WorldRenderEvents.LAST.register(this::onWorldRenderEventLast);
        ClientChunkEvents.CHUNK_LOAD.register(this::onClientChunkLoadEvent);
        HudRenderCallback.EVENT.register(onHubRenderEvent());
        ClientTickEvents.END_CLIENT_TICK.register(onClientTickEndEvent());
    }

    private void onClientTickStartEvent (MinecraftClient client) {

        PlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;
        if (player == null) {

            return;
        }

        handleKeyInputs();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 200) {

            // Clear all when switching between worlds
            RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
            if (previousWorld != null && !dimensionKey.equals(previousWorld)) {

                this.clearAll();
            }
            previousWorld = dimensionKey;

            this.blockThreadPool.submit(() -> {

                int playerChunkPosX = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().x;
                int playerChunkPosZ = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().z;

                int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
                int xStart = playerChunkPosX - playerRenderDistance;
                int xEnd = playerChunkPosX + playerRenderDistance + 1;
                int zStart = playerChunkPosZ - playerRenderDistance;
                int zEnd = playerChunkPosZ + playerRenderDistance + 1;

                List<Pair<BlockPos, Color>> positionsTemp = new ArrayList<>();
                List<Pair<BlockPos, Color>> dungeonsTemp = new ArrayList<>();
                for (int x = xStart; x < xEnd; x++) {

                    for (int z = zStart; z < zEnd; z++) {

                        Chunk chunk = FinderUtil.getChunkEarly(x, z);

                        if (chunk != null) {

                            Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                            if (blockPositions != null) {

                                for (BlockPos pos : blockPositions) {

                                    BlockEntity blockEntity = chunk.getBlockEntity(pos);

                                    // Check for signs
                                    if (
                                        Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.SIGN_READER)
                                        && blockEntity instanceof SignBlockEntity
                                    ) {

                                        // Get the player's current position
                                        BlockPos playerPos = Constants.MC_CLIENT_INSTANCE.player.getBlockPos();

                                        // Calculate the distance between the player's position and the sign's
                                        // position
                                        double squaredDistance = blockEntity.getPos().getSquaredDistance(playerPos);

                                        String signText = SignTextExtractor.getSignText((SignBlockEntity) blockEntity);
                                        if (
                                            !Constants.DISPLAYED_SIGNS_CACHE.contains(blockEntity.getPos().toShortString().hashCode())
                                            && !signText.isEmpty()
                                            && squaredDistance > 5 * 5
                                            && !signText.equals("<----\n---->")
                                        ) {

                                            Text styledText = Text.empty()
                                                    .append(Text.literal("[")
                                                            .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                                    .append(Text.literal("Stashwalker, ")
                                                            .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                                                    .append(Text.literal("signReader")
                                                            .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                                                    .append(Text.literal("]:\n")
                                                            .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                                    .append(Text.literal(signText)
                                                            .setStyle(Style.EMPTY.withColor(Formatting.AQUA)));

                                            Constants.MESSAGES_BUFFER.add(styledText);

                                            Constants.DISPLAYED_SIGNS_CACHE
                                                    .add(blockEntity.getPos().toShortString().hashCode());
                                        }
                                    }

                                    // Check for interesting Blocks
                                    if (
                                        Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_BLOCK_TRACERS)
                                        && FinderUtil.isInterestingBlockPosition(pos, x, z)
                                    ) {

                                        String key = Constants.BLOCK_KEY_START
                                                + Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos).getBlock()
                                                        .getName().getString().replace(" ", "_");
                                        Color color = new Color(
                                                Constants.CONFIG_MANAGER.getConfig().getBlockColors().get(key),
                                                true);
                                        positionsTemp.add(new Pair<BlockPos, Color>(pos, color));
                                    }

                                    if (
                                        Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_ALTERED_DUNGEONS)
                                        && World.OVERWORLD.equals(dimensionKey)  
                                    ) {


                                        Pair<BlockPos, List<BlockPos>> result = FinderUtil.getAlteredDungeonsBlocksWithPillars(pos, x, z);
                                        if (result.getValue().size() > 0) {

                                            result.getValue().forEach(r -> dungeonsTemp.add(new Pair<BlockPos,Color>(r, Color.GRAY)));
                                            dungeonsTemp.add(new Pair<BlockPos, Color>(result.getKey(), Color.BLUE));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Constants.INTERESTING_BLOCKS_BUFFER.updateBuffer(positionsTemp);
                Constants.ALTERED_DUNGEONS_BUFFER.updateBuffer(dungeonsTemp);
            });

            if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_ENTITY_TRACERS)) {

                this.entityThreadPool.submit(() -> {

                    List<Entity> entities = FinderUtil.findEntities();

                    Constants.ENTITY_BUFFER.updateBuffer(entities);
                });
            }

            lastTime = currentTime;
        }
    }

    private void onClientChunkLoadEvent (ClientWorld world, WorldChunk chunk) {

        this.chunkLoadThreadPool.submit(() -> {

            if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_NEW_CHUNKS)) {

                if (FinderUtil.isNewChunk(chunk)) {

                    Constants.CHUNK_SET.add(chunk.getPos());
                }
            }

            if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_BLOCK_TRACERS)) {

                if (FinderUtil.hasSolidBlocksNearBuildLimit(chunk)) {

                    Text styledText = Text.empty()
                            .append(Text.literal("[")
                                    .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.literal("Stashwalker, ")
                                    .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                            .append(Text.literal("blockEntities")
                                    .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                            .append(Text.literal("]:\n")
                                    .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.literal("Solid blocks found near (old) build limit")
                                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
                    Constants.MESSAGES_BUFFER.add(styledText);
                }
            }
        });
    }

    // Event callback method to render lines in the world
    private void onWorldRenderEventLast (WorldRenderContext context) {

        PlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;
        if (player == null) {

            return;
        }

        ListIterator<Text> iterator = Constants.MESSAGES_BUFFER.listIterator();
        while (iterator.hasNext()) {

            Constants.RENDERER.sendClientSideMessage(iterator.next());
            iterator.remove();
        }
        
        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_BLOCK_TRACERS)) {

            List<Pair<BlockPos, Color>> blockpositions = Constants.INTERESTING_BLOCKS_BUFFER.readBuffer();
            if (blockpositions != null) {

                for (Pair<BlockPos, Color> pair : blockpositions) {

                    BlockPos blockPos = pair.getKey();
                    Color color = pair.getValue();
                    Vec3d newBlockPos = new Vec3d(
                            blockPos.getX() + 0.5D,
                            blockPos.getY() + 0.5D,
                            blockPos.getZ() + 0.5D);

                    Constants.RENDERER.drawLine(context, newBlockPos, color.getRed(), color.getGreen(), color.getBlue(),
                            color.getAlpha(), false);
                }
            }
        }

        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_ENTITY_TRACERS)) {

            List<Entity> entities = Constants.ENTITY_BUFFER.readBuffer();
            if (!entities.isEmpty()) {

                for (Entity entity : entities) {

                    Vec3d entityPos;
                    if (entity instanceof ItemFrameEntity) {

                        entityPos = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY(),
                                entity.getPos().getZ());
                    } else {

                        entityPos = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY() + 0.5D,
                                entity.getPos().getZ());
                    }

                    Color color = Color.RED;
                    Constants.RENDERER.drawLine(context, entityPos, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), true);
                }
            }
        }

        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_NEW_CHUNKS)) {

            Color color = Color.RED;

            Constants.RENDERER
                    .drawChunkSquare(
                            context,
                            Constants.CHUNK_SET,
                            63,
                            16,
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            color.getAlpha()
                    );
        }

        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_ALTERED_DUNGEONS)) {

            List<Pair<BlockPos, Color>> blockpositions = Constants.ALTERED_DUNGEONS_BUFFER.readBuffer();
            if (blockpositions != null) {

                for (Pair<BlockPos, Color> pair : blockpositions) {

                    BlockPos blockPos = pair.getKey();
                    Color color = pair.getValue();
                    Vec3d newBlockPos = new Vec3d(
                            blockPos.getX() + 0.5D,
                            blockPos.getY() + 0.5D,
                            blockPos.getZ() + 0.5D);
                    
                    if (color.equals(Color.BLUE)) {

                        Constants.RENDERER.drawLine(context, newBlockPos, color.getRed(), color.getGreen(), color.getBlue(),
                            color.getAlpha(), false);
                    } else if (color.equals(Color.GRAY)) {

                        Vec3d cameraPos = Constants.MC_CLIENT_INSTANCE.gameRenderer.getCamera().getPos();
                        newBlockPos = newBlockPos.subtract(cameraPos);
                        Constants.RENDERER.drawBlockSquare(context, newBlockPos, color.getRed(), color.getGreen(), color.getBlue(),
                            color.getAlpha(), false);
                    }
                }
            }
        }
    }

    private HudRenderCallback onHubRenderEvent () {

        return (drawContext, tickCounter) -> {

            if (!Constants.MC_CLIENT_INSTANCE.inGameHud.getDebugHud().shouldShowDebugHud()) {

                FabricLoader.getInstance().getModContainer("stashwalker")
                        .ifPresent(m -> {

                            String version = m.getMetadata().getVersion().getFriendlyString();
                            String shortVersion = (version.split("-").length > 1 ? version.split("-")[0] : "Unknown") + (version.contains("SNAPSHOT") ? "-SNAPSHOT" : "");
                            String modName = "Stashwalker v" + shortVersion;
                            // Get the window's width
                            int screenWidth = Constants.MC_CLIENT_INSTANCE.getWindow().getScaledWidth();

                            // Calculate the x-position to center the text
                            int modNameWidth = Constants.MC_CLIENT_INSTANCE.textRenderer
                                    .getWidth(modName);
                            int featuresWidth = Constants.MC_CLIENT_INSTANCE.textRenderer
                                    .getWidth(" ["
                                            + Constants.FEATURE_NAMES.stream().collect(Collectors.joining(", ")) + "]");

                            String featuresText = " [" + Constants.FEATURE_NAMES.stream()
                                    .filter(n -> (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(n))).collect(Collectors.joining(", ")) + "]";


                            int y = 2;
                            BossBarHud bossBarHud = Constants.MC_CLIENT_INSTANCE.inGameHud.getBossBarHud();
                            if (bossBarHud instanceof IBossBarHudMixin) {

                                IBossBarHudMixin bossBarHudMixin = (IBossBarHudMixin) bossBarHud;
                                Map<UUID, ClientBossBar> bossBars = bossBarHudMixin.getBossBars();
                                // Split the HUD text so it doesn't overlap with the Boss bar HUD
                                if (bossBarHudMixin != null && bossBars != null && bossBars.size() > 0) {

                                    Constants.RENDERER.renderHUDText(drawContext, modName, (screenWidth / 4) - (modNameWidth / 2), y, 0xFFFFFFFF);
                                    Constants.RENDERER.renderHUDText(drawContext, featuresText, ((screenWidth / 4) * 3) - (featuresWidth / 2), y, 0xFFFFFFFF);
                                } else {

                                    int x = (screenWidth / 2) - ((modNameWidth + featuresWidth) / 2);
                                    Constants.RENDERER.renderHUDText(drawContext, modName + featuresText, x, y, 0xFFFFFFFF);
                                }

                            }
                        });
            }
        };
    }

    private EndTick onClientTickEndEvent () {

        return client -> {

            // Check if the player was in the game and is now in the title screen
            if (wasInGame && client.currentScreen instanceof TitleScreen) {

                clearAll();

                wasInGame = false;
            }

            // Check if the player is in a world
            if (client.world != null) {
                wasInGame = true;
            }
        };
    }

    private void handleKeyInputs () {

        if (keyBindingEntityTracers.isPressed()) {

            if (!entityTracersWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean entityTracers = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_ENTITY_TRACERS);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.FEATURE_ENTITY_TRACERS, entityTracers);
                Constants.MESSAGES_BUFFER.add(FinderUtil.createStyledTextForFeature(Constants.FEATURE_ENTITY_TRACERS, entityTracers));
                Constants.ENTITY_BUFFER.updateBuffer(Collections.emptyList());
            }

            entityTracersWasPressed = true;
        } else {

            entityTracersWasPressed = false;
        }

        if (keyBindingBlockTracers.isPressed()) {

            if (!blockTracersWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean blockTracers = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_BLOCK_TRACERS);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.FEATURE_BLOCK_TRACERS, blockTracers);
                Constants.MESSAGES_BUFFER.add(FinderUtil.createStyledTextForFeature(Constants.FEATURE_BLOCK_TRACERS, blockTracers));
                Constants.INTERESTING_BLOCKS_BUFFER.updateBuffer(Collections.emptyList());
            }

            blockTracersWasPressed = true;
        } else {

            blockTracersWasPressed = false;
        }

        if (keyBindingNewChunks.isPressed()) {

            if (!newChunksWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean newChunks = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_NEW_CHUNKS);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.FEATURE_NEW_CHUNKS, newChunks);
                Constants.CHUNK_SET.clear();
                Constants.MESSAGES_BUFFER.add(FinderUtil.createStyledTextForFeature(Constants.FEATURE_NEW_CHUNKS, newChunks));
            }

            newChunksWasPressed = true;
        } else {

            newChunksWasPressed = false;
        }

        if (keyBindingSignReader.isPressed()) {

            if (!signReaderWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean signReader = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.SIGN_READER);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.SIGN_READER, signReader);
                Constants.DISPLAYED_SIGNS_CACHE.clear();
                Constants.SIGNS_BUFFER.updateBuffer(Collections.emptyList());
                Constants.MESSAGES_BUFFER.add(FinderUtil.createStyledTextForFeature(Constants.SIGN_READER, signReader));
            }

            signReaderWasPressed = true;
        } else {

            signReaderWasPressed = false;
        }

        if (keyBindingAlteredDungeons.isPressed()) {

            if (!alteredDungeonsWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean alteredDungeons = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.FEATURE_ALTERED_DUNGEONS);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.FEATURE_ALTERED_DUNGEONS, alteredDungeons);
                Constants.MESSAGES_BUFFER.add(FinderUtil.createStyledTextForFeature(Constants.FEATURE_ALTERED_DUNGEONS, alteredDungeons));
                Constants.ALTERED_DUNGEONS_BUFFER.updateBuffer(Collections.emptyList());
            }

            alteredDungeonsWasPressed = true;
        } else {

            alteredDungeonsWasPressed = false;
        }
    }

    private void registerKeyBindings () {

        this.keyBindingEntityTracers = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.entity_tracers",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_6,
                "category.stashwalker.keys"
        ));
        this.keyBindingBlockTracers = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.block_tracers",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_7,
                "category.stashwalker.keys"
        ));
        this.keyBindingNewChunks = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.new_chunks",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_8,
                "category.stashwalker.keys"
        ));
        this.keyBindingSignReader = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.sign_reader",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_9,
                "category.stashwalker.keys"
        ));
        this.keyBindingAlteredDungeons = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.altered_dungeons",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_0,
                "category.stashwalker.keys"
        ));
    }

    private void clearAll () {

        Constants.MESSAGES_BUFFER.clear();;
        Constants.CHUNK_SET.clear();
        Constants.SIGNS_BUFFER.updateBuffer(Collections.emptyList());
        Constants.DISPLAYED_SIGNS_CACHE.clear();
        Constants.INTERESTING_BLOCKS_BUFFER.updateBuffer(Collections.emptyList());
        Constants.ENTITY_BUFFER.updateBuffer(Collections.emptyList());
        Constants.ALTERED_DUNGEONS_BUFFER.updateBuffer(Collections.emptyList());
    }
}
