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
import java.util.HashSet;
import java.util.List;
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
import com.stashwalker.containers.ConcurrentBoundedSet;
import com.stashwalker.containers.DoubleBuffer;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.utils.SignTextExtractor;
import com.stashwalker.finders.Finder;
import com.stashwalker.mixininterfaces.IBossBarHudMixin;
import com.stashwalker.models.FinderResult;
import com.stashwalker.utils.DaemonThreadFactory;
import com.stashwalker.utils.FinderUtil;

import java.awt.Color;

@Environment(EnvType.CLIENT)
public class StashwalkerModClient implements ClientModInitializer {

    private DoubleListBuffer<Entity> entityBuffer = new DoubleListBuffer<>();
    private DoubleListBuffer<Pair<BlockPos, Color>> blockPositionsBuffer = new DoubleListBuffer<>();
    private DoubleListBuffer<BlockEntity> signsBuffer = new DoubleListBuffer<>();
    private ConcurrentBoundedSet<Integer> signsCache = new ConcurrentBoundedSet<>(5000);
    private ExecutorService blockThreadPool = Executors.newFixedThreadPool(1, new DaemonThreadFactory());
    private ExecutorService entityThreadPool = Executors.newFixedThreadPool(1, new DaemonThreadFactory());
    private ExecutorService chunkLoadThreadPool = Executors.newFixedThreadPool(5, new DaemonThreadFactory());

    private KeyBinding keyBindingEntityTracers;
    private KeyBinding keyBindingBlockTracers;
    private KeyBinding keyBindingNewChunks;
    private KeyBinding keyBindingSignReader;
    private boolean entityTracersWasPressed;
    private boolean blockTracersWasPressed;
    private boolean newChunksWasPressed;
    private boolean signReaderWasPressed;
    private Finder finder = new Finder();
    private boolean wasInGame = false;
    private long lastTime = 0;

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

        handleKeyInputs();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 200) {

            if (
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.SIGN_READER) 
                || Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.BLOCK_TRACERS)
            ) {

                this.blockThreadPool.submit(() -> {

                    World world = Constants.MC_CLIENT_INSTANCE.player.getWorld();

                    int playerChunkPosX = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().x;
                    int playerChunkPosZ = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().z;

                    int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
                    int xStart = playerChunkPosX - playerRenderDistance;
                    int xEnd = playerChunkPosX + playerRenderDistance + 1;
                    int zStart = playerChunkPosZ - playerRenderDistance;
                    int zEnd = playerChunkPosZ + playerRenderDistance + 1;

                    FinderResult finderResult = new FinderResult();
                    List<BlockEntity> signsTemp = new ArrayList<>();
                    List<Pair<BlockPos, Color>> positionsTemp = new ArrayList<>();

                    for (int x = xStart; x < xEnd; x++) {

                        for (int z = zStart; z < zEnd; z++) {

                            Chunk chunk = FinderUtil.getChunkEarly(x, z);

                            if (chunk != null) {

                                Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                                if (blockPositions != null) {

                                    for (BlockPos pos : blockPositions) {

                                        BlockEntity blockEntity = chunk.getBlockEntity(pos);

                                        // Check for signs
                                        if (blockEntity instanceof SignBlockEntity) {

                                            signsTemp.add(blockEntity);
                                        }

                                        // Check for interesting Blocks
                                        if (this.finder.isInterestingBlockPosition(pos, chunk, x, z)) {

                                            String key = Constants.BLOCK_KEY_START
                                                    + Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos).getBlock()
                                                            .getName().getString().replace(" ", "_");
                                            Color color = new Color(
                                                    Constants.CONFIG_MANAGER.getConfig().getBlockColors().get(key),
                                                    true);
                                            positionsTemp.add(new Pair<BlockPos, Color>(pos, color));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Update the buffer when all the chunks have been searched
                    this.signsBuffer.updateBuffer(signsTemp);
                    this.blockPositionsBuffer.updateBuffer(positionsTemp);
                });
            }

            if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.ENTITY_TRACERS)) {

                this.entityThreadPool.submit(() -> {

                    List<Entity> entities = this.finder.findEntities();

                    this.entityBuffer.updateBuffer(entities);
                });
            }

            lastTime = currentTime;
        }
    }

    private void onClientChunkLoadEvent (ClientWorld world, WorldChunk chunk) {

        this.chunkLoadThreadPool.submit(() -> {

            if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.NEW_CHUNKS)) {

                if (this.finder.isNewChunk(chunk)) {

                    Constants.CHUNK_SET.add(chunk.getPos());
                }
            }

            if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.BLOCK_TRACERS)) {

                if (this.finder.solidBlocksNearBuildLimit(chunk)) {

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
                    Constants.MESSAGE_BUFFER.updateBuffer(styledText);
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
        
        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.BLOCK_TRACERS)) {

            List<Pair<BlockPos, Color>> blockpositions = this.blockPositionsBuffer.readBuffer();
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

            Text styledText = Constants.MESSAGE_BUFFER.readBuffer();
            if (styledText != null) {

                Constants.RENDERER.sendClientSideMessage(styledText);
                Constants.MESSAGE_BUFFER.updateBuffer(null);
            }
        }

        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.ENTITY_TRACERS)) {

            List<Entity> entities = entityBuffer.readBuffer();
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

                    Constants.RENDERER.drawLine(context, entityPos, 255, 0, 0, 255, true);
                }
            }
        }

        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.SIGN_READER)) {

            List<BlockEntity> signs = this.signsBuffer.readBuffer();
            if (signs != null) {

                for (BlockEntity sign : signs) {

                    if (!this.signsCache.contains(sign.getPos().toShortString().hashCode())) {

                        String signText = SignTextExtractor.getSignText((SignBlockEntity) sign);
                        if (!signText.isEmpty() && !signText.equals("<----\n---->")) {

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
                            Constants.RENDERER.sendClientSideMessage(styledText);
                            this.signsCache.add(sign.getPos().toShortString().hashCode());
                        }
                    }
                }
            }
        }

        if (Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.NEW_CHUNKS)) {

            RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
            if (World.OVERWORLD.equals(dimensionKey)) {

                Constants.RENDERER
                    .drawChunkSquare(
                        context,
                        Constants.CHUNK_SET,
                        63,
                        16,
                        255,
                        0,
                        0,
                        255
                    );
            } else if (World.NETHER.equals(dimensionKey)) {

                Constants.RENDERER
                    .drawChunkSquare(
                        context,
                        Constants.CHUNK_SET,
                        -64,
                        16,
                        255,
                        255,
                        255,
                        255
                    );
            }
        }
    }

    private HudRenderCallback onHubRenderEvent() {
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

                Constants.CHUNK_SET.clear();
                this.signsCache.clear();
                this.entityBuffer.updateBuffer(Collections.emptyList());
                this.signsBuffer.updateBuffer(Collections.emptyList());
                this.blockPositionsBuffer.updateBuffer(Collections.emptyList());

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
                boolean entityTracers = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.ENTITY_TRACERS);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.ENTITY_TRACERS, entityTracers);
                Constants.RENDERER.sendClientSideMessage(FinderUtil.createStyledTextForFeature(Constants.ENTITY_TRACERS, entityTracers));
            }

            entityTracersWasPressed = true;
        } else {

            entityTracersWasPressed = false;
        }

        if (keyBindingBlockTracers.isPressed()) {

            if (!blockTracersWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean blockTracers = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.BLOCK_TRACERS);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.BLOCK_TRACERS, blockTracers);
                Constants.RENDERER.sendClientSideMessage(FinderUtil.createStyledTextForFeature(Constants.BLOCK_TRACERS, blockTracers));
            }

            blockTracersWasPressed = true;
        } else {

            blockTracersWasPressed = false;
        }

        if (keyBindingNewChunks.isPressed()) {

            if (!newChunksWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean newChunks = !Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().get(Constants.NEW_CHUNKS);
                Constants.CONFIG_MANAGER.getConfig().getFeatureSettings().put(Constants.NEW_CHUNKS, newChunks);
                Constants.CHUNK_SET.clear();

                Constants.RENDERER.sendClientSideMessage(FinderUtil.createStyledTextForFeature(Constants.NEW_CHUNKS, newChunks));
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
                this.signsCache.clear();

                Constants.RENDERER.sendClientSideMessage(FinderUtil.createStyledTextForFeature(Constants.SIGN_READER, signReader));
            }

            signReaderWasPressed = true;
        } else {

            signReaderWasPressed = false;
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
    }
}
