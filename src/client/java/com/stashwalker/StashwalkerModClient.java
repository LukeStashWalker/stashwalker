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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.chunk.Chunk;
import java.util.List;

import net.minecraft.entity.Entity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import com.stashwalker.config.ConfigManager;
import com.stashwalker.constants.Constants;
import com.stashwalker.utils.SignTextExtractor;
import com.stashwalker.finders.Finder;
import com.stashwalker.utils.DoubleBuffer;
import com.stashwalker.rendering.Renderer;
import com.stashwalker.utils.DaemonThreadFactory;
import com.stashwalker.utils.MaxSizeSet;
import com.stashwalker.utils.Pair;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class StashwalkerModClient implements ClientModInitializer {

    private long lastTime = 0;

    private DoubleBuffer<Pair<BlockPos, Color>> blockPositionsBuffer = new DoubleBuffer<>();
    private DoubleBuffer<Entity> entityBuffer = new DoubleBuffer<>();
    private DoubleBuffer<Chunk> chunkBuffer = new DoubleBuffer<>();
    private MaxSizeSet<Integer> signsSet = new MaxSizeSet<>(5000);
    private ExecutorService threadPool = Executors.newFixedThreadPool(3, new DaemonThreadFactory());
    private KeyBinding keyBindingEntityTracers;
    private KeyBinding keyBindingBlockTracers;
    private KeyBinding keyBindingNewChunks;
    private KeyBinding keyBindingSignReader;
    private boolean entityTracersWasPressed;
    private boolean blockTracersWasPressed;
    private boolean newChunksWasPressed;
    private boolean signReaderWasPressed;
    private Renderer renderer = new Renderer();
    private Finder finder = new Finder();
    private ConfigManager configManager = new ConfigManager();
    private Map<String, Boolean> configData = new HashMap<>();

    @Override
    public void onInitializeClient () {

        this.loadConfig(); // Load the saved state
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveConfig)); // Save config on shutdown

        // Register a rendering event
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTickStart);
        WorldRenderEvents.LAST.register(this::onRenderWorld);

        keyBindingEntityTracers = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.entity_tracers",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_6,
                "category.stashwalker.keys"
        ));
        keyBindingBlockTracers = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.block_tracers",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_7,
                "category.stashwalker.keys"
        ));
        keyBindingNewChunks = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.new_chunks",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_8,
                "category.stashwalker.keys"
        ));
        keyBindingSignReader = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stashwalker.sign_reader",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_9,
                "category.stashwalker.keys"
        ));

        HudRenderCallback.EVENT.register(((drawContext, tickCounter) -> {

            if (!Constants.MC_CLIENT_INSTANCE.inGameHud.getDebugHud().shouldShowDebugHud()) {

                FabricLoader.getInstance().getModContainer("stashwalker")
                        .ifPresent(m -> {

                            String version = m.getMetadata().getVersion().getFriendlyString();
                            String modName = "Stashwalker v" + version;
                            // Get the window's width
                            int screenWidth = Constants.MC_CLIENT_INSTANCE.getWindow().getScaledWidth();

                            // Calculate the x-position to center the text
                            int textWidth = Constants.MC_CLIENT_INSTANCE.textRenderer
                                    .getWidth(modName + " ["
                                            + Constants.FEATURE_NAMES.stream().collect(Collectors.joining(", ")) + "]");

                            String text = modName + " [" + Constants.FEATURE_NAMES.stream()
                                    .filter(n -> this.configData.get(n)).collect(Collectors.joining(", ")) + "]";

                            int x = (screenWidth / 2) - (textWidth / 2);

                            int y = 2;

                            // Render the text
                            this.renderer.renderHUDText(drawContext, text, x, y, 0xFFFFFFFF);
                        });
            }
        }));
    }

    private void onClientTickStart (MinecraftClient client) {

        if (keyBindingEntityTracers.isPressed()) {

            if (!entityTracersWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean entityTracers = !this.configData.get(Constants.ENTITY_TRACERS);
                this.configData.put(Constants.ENTITY_TRACERS, entityTracers);
                this.renderer.sendClientSideMessage(this.createStyledTextForFeature(Constants.ENTITY_TRACERS, entityTracers));
            }

            entityTracersWasPressed = true;
        } else {

            entityTracersWasPressed = false;
        }

        if (keyBindingBlockTracers.isPressed()) {

            if (!blockTracersWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean blockTracers = !this.configData.get(Constants.BLOCK_TRACERS);
                this.configData.put(Constants.BLOCK_TRACERS, blockTracers);
                this.renderer.sendClientSideMessage(this.createStyledTextForFeature(Constants.BLOCK_TRACERS, blockTracers));
            }

            blockTracersWasPressed = true;
        } else {

            blockTracersWasPressed = false;
        }

        if (keyBindingNewChunks.isPressed()) {

            if (!newChunksWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean newChunks = !this.configData.get(Constants.NEW_CHUNKS);
                this.configData.put(Constants.NEW_CHUNKS, newChunks);

                this.renderer.sendClientSideMessage(this.createStyledTextForFeature(Constants.NEW_CHUNKS, newChunks));
            }

            newChunksWasPressed = true;
        } else {

            newChunksWasPressed = false;
        }

        if (keyBindingSignReader.isPressed()) {

            if (!signReaderWasPressed) {

                // Toggle the boolean when the key is pressed
                boolean signReader = !this.configData.get(Constants.SIGN_READER);
                this.configData.put(Constants.SIGN_READER, signReader);
                this.signsSet.clear();

                this.renderer.sendClientSideMessage(this.createStyledTextForFeature(Constants.SIGN_READER, signReader));
            }

            signReaderWasPressed = true;
        } else {

            signReaderWasPressed = false;
        }


        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 20) {

            if (this.configData.get(Constants.BLOCK_TRACERS)) {

                threadPool.submit(() -> {

                    this.blockPositionsBuffer.updateBuffer(this.finder.findBlockPositions(client.player));
                });
            }

            if (this.configData.get(Constants.ENTITY_TRACERS)) {

                threadPool.submit(() -> {

                    this.entityBuffer.updateBuffer(this.finder.findEntities(client.player));
                });
            }

            if (this.configData.get(Constants.NEW_CHUNKS)) {

                threadPool.submit(() -> {

                    this.chunkBuffer.updateBuffer(this.finder.findNewChunks(client.player));
                });
            }

            if (this.configData.get(Constants.SIGN_READER)) {

                threadPool.submit(() -> {

                    if (this.configData.get(Constants.SIGN_READER)) {

                        List<BlockEntity> signs = this.finder.findSigns(client.player);
                        for (BlockEntity sign : signs) {

                            if (!this.signsSet.contains(sign.hashCode())) {

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
                                    this.renderer.sendClientSideMessage(styledText);
                                    this.signsSet.add(sign.hashCode());
                                }
                            }
                        }
                    }
                });
            }

            lastTime = currentTime;
        }
    }

    // Event callback method to render lines in the world
    private void onRenderWorld (WorldRenderContext context) {

        PlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;
        if (player == null) {

            return;
        }

        if (this.configData.get(Constants.BLOCK_TRACERS)) {

            List<Pair<BlockPos, Color>> blockPairs = this.blockPositionsBuffer.readBuffer();
            if (!blockPairs.isEmpty()) {

                // MatrixStack matrixStack = context.matrixStack(); // Use matrixStack() method

                for (Pair<BlockPos, Color> pair : blockPairs) {

                    BlockPos blockPos = pair.getKey();
                    Color color = pair.getValue();
                    Vec3d newBlockPos = new Vec3d(
                            blockPos.getX() + 0.5D,
                            blockPos.getY() + 0.5D,
                            blockPos.getZ() + 0.5D
                    );

                    this.renderer.drawLine(context, newBlockPos, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                }
            }
        }

        if (this.configData.get(Constants.ENTITY_TRACERS)) {

            List<Entity> entities = this.entityBuffer.readBuffer();
            if (!entities.isEmpty()) {

                for (Entity entity : entities) {

                    Vec3d entityPos = new Vec3d(
                            entity.getPos().getX(),
                            entity.getPos().getY() + 0.5D,
                            entity.getPos().getZ()
                    );

                    this.renderer.drawLine(context, entityPos, 255, 0, 0, 255);
                }
            }
        }

        if (this.configData.get(Constants.NEW_CHUNKS)) {

            this.renderer
            .drawChunkSquare(
                context, 
                this.chunkBuffer.readBuffer(), 
                63, 
                32, 
                255, 
                0, 
                0, 
                255
            );
        }
    }

    private void saveConfig () {

        configManager.saveConfig(this.configData);
    }

    private void loadConfig () {

        this.configData = configManager.loadConfig();
        Constants.FEATURE_NAMES.forEach(n -> {
            
            if (!this.configData.containsKey(n)) {

                this.configData.put(n, true);
            }
        });
    }

    private Text createStyledTextForFeature (String featureName, boolean featureToggle) {


                return Text.empty()
                        .append(Text.literal("[")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal("Stashwalker, ")
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                        .append(Text.literal(featureName)
                                .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                        .append(Text.literal("]:")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal(featureToggle ? " enabled" : " disabled")
                                .setStyle(Style.EMPTY.withColor(featureToggle ? Formatting.GREEN : Formatting.RED)));
    }

}
