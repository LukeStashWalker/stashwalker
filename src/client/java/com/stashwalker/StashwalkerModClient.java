package com.stashwalker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
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
import com.stashwalker.features.ChunkLoadProcessor;
import com.stashwalker.features.Feature;
import com.stashwalker.features.Processor;
import com.stashwalker.features.Renderable;
import com.stashwalker.mixininterfaces.IBossBarHudMixin;
import com.stashwalker.utils.DaemonThreadFactory;
import com.stashwalker.utils.RenderUtil;

@Environment(EnvType.CLIENT)
public class StashwalkerModClient implements ClientModInitializer {

    private static final int SCAN_INTERVAL = 200;
    private final ExecutorService processThreadPool = Executors.newFixedThreadPool(5, new DaemonThreadFactory());
    private final ExecutorService chunkLoadThreadPool = Executors.newFixedThreadPool(5, new DaemonThreadFactory());

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
        ClientChunkEvents.CHUNK_LOAD.register(this::onClientChunkLoadEvent);
        WorldRenderEvents.LAST.register(this::onWorldRenderEventLast);
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
        if (currentTime - lastTime >= SCAN_INTERVAL) {

            // Clear all when switching between worlds
            RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
            if (previousWorld != null && !dimensionKey.equals(previousWorld)) {

                Constants.FEATURES.forEach(f -> f.clear());
            }
            previousWorld = dimensionKey;

            this.processThreadPool.submit(() -> {

                Constants.FEATURES.forEach(f -> {

                    if (f instanceof Processor) {

                        ((Processor) f).process();
                    }
                });
            });

            lastTime = currentTime;
        }
    }

    private void onClientChunkLoadEvent (ClientWorld world, WorldChunk chunk) {

        this.chunkLoadThreadPool.submit(() -> {

            Constants.FEATURES.forEach(f -> {

                if (f instanceof ChunkLoadProcessor) {

                    ((ChunkLoadProcessor) f).processLoadedChunk(chunk);
                }
            });

        });
    }

    // Event callback method to render lines in the world
    private void onWorldRenderEventLast (WorldRenderContext context) {

        PlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;
        if (player == null) {

            return;
        }

        Constants.MESSAGES_BUFFER.forEach(m -> {

            RenderUtil.sendClientSideMessage(m);
        });
        Constants.MESSAGES_BUFFER.clear();

        Constants.FEATURES.forEach(f -> {

            if (f instanceof Renderable) {

                ((Renderable) f).render(context);
            }
        });
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
                                            + Constants.FEATURES.stream().map(f -> f.getFeatureName()).collect(Collectors.joining(", ")) + "]");

                            String featuresText = " [" + Constants.FEATURES.stream()
                                .filter(f -> f.isEnabled())
                                .map(f -> f.getFeatureName())
                                .collect(Collectors.joining(", ")) + "]";


                            int y = 2;
                            BossBarHud bossBarHud = Constants.MC_CLIENT_INSTANCE.inGameHud.getBossBarHud();
                            if (bossBarHud instanceof IBossBarHudMixin) {

                                IBossBarHudMixin bossBarHudMixin = (IBossBarHudMixin) bossBarHud;
                                Map<UUID, ClientBossBar> bossBars = bossBarHudMixin.getBossBars();
                                // Split the HUD text so it doesn't overlap with the Boss bar HUD
                                if (bossBarHudMixin != null && bossBars != null && bossBars.size() > 0) {

                                    int bossBarHudWidth = bossBarHudMixin.getWidth();

                                    RenderUtil.renderHUDText(drawContext, modName, (screenWidth / 2) - (modNameWidth + (bossBarHudWidth / 2) + 2), y, 0xFFFFFFFF);
                                    RenderUtil.renderHUDText(drawContext, featuresText, (screenWidth / 2) + (bossBarHudWidth / 2) + 2, y, 0xFFFFFFFF);
                                } else {

                                    int x = (screenWidth / 2) - ((modNameWidth + featuresWidth) / 2);
                                    RenderUtil.renderHUDText(drawContext, modName + featuresText, x, y, 0xFFFFFFFF);
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

                // clearAll();
                Constants.FEATURES.forEach(f -> f.clear());

                wasInGame = false;
            }

            // Check if the player is in a world
            if (client.world != null) {
                wasInGame = true;
            }
        };
    }

    private void handleKeyInputsHelper (String featureName) {

        Constants.FEATURES.forEach(f -> {

            if (f.getFeatureName().equals(featureName)) {

                boolean state = !f.isEnabled();
                f.setEnabled(state);
                f.clear();

                Constants.MESSAGES_BUFFER
                        .add(this.createStyledTextForFeature(featureName, state));
            }
        });
    }

    private void handleKeyInputs () {

        if (keyBindingEntityTracers.isPressed()) {

            if (!entityTracersWasPressed) {

                this.handleKeyInputsHelper(Feature.FEATURE_NAME_ENTITY_TRACER);
            }

            entityTracersWasPressed = true;
        } else {

            entityTracersWasPressed = false;
        }

        if (keyBindingBlockTracers.isPressed()) {

            if (!blockTracersWasPressed) {

                this.handleKeyInputsHelper(Feature.FEATURE_NAME_BLOCK_TRACER);
            }

            blockTracersWasPressed = true;
        } else {

            blockTracersWasPressed = false;
        }

        if (keyBindingNewChunks.isPressed()) {

            if (!newChunksWasPressed) {

                this.handleKeyInputsHelper(Feature.FEATURE_NAME_NEW_CHUNKS);
            }

            newChunksWasPressed = true;
        } else {

            newChunksWasPressed = false;
        }

        if (keyBindingSignReader.isPressed()) {

            if (!signReaderWasPressed) {

                this.handleKeyInputsHelper(Feature.FEATURE_NAME_SIGN_READER);
            }

            signReaderWasPressed = true;
        } else {

            signReaderWasPressed = false;
        }

        if (keyBindingAlteredDungeons.isPressed()) {

            if (!alteredDungeonsWasPressed) {

                this.handleKeyInputsHelper(Feature.FEATURE_NAME_ALTERED_DUNGEONS);
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
    
    public Text createStyledTextForFeature (String featureName, boolean featureToggle) {

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
