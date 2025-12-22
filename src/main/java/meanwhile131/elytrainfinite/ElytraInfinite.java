package meanwhile131.elytrainfinite;

import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.ClientModInitializer;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import meanwhile131.elytrainfinite.mixin.LivingEntityInvoker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

enum FlyState {
    NOT_FLYING,
    PITCHING_DOWN,
    GLIDING_DOWN
}

public class ElytraInfinite
        implements ClientModInitializer, ClientTickEvents.StartWorldTick, ClientTickEvents.EndTick, UseItemCallback {
    public static final String MOD_ID = "elytra-infinite";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig CONFIG;
    private FlyState state = FlyState.NOT_FLYING;
    private static KeyMapping toggleKeybind;
    private float pitch;
    private double lowest_y;

    @Override
    public void onInitializeClient() {
        ModConfig.HANDLER.load();
        CONFIG = ModConfig.HANDLER.instance();
        KeyMapping.Category keybindCategory = KeyMapping.Category.register(Identifier.parse("elytrainfinite"));
        toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.elytrainfinite.toggle",
                GLFW.GLFW_KEY_H,
                keybindCategory));
        ClientTickEvents.START_WORLD_TICK.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        UseItemCallback.EVENT.register(this);
        LOGGER.info("Elytra Infinite loaded.");
    }

    @Override
    public void onStartTick(@NotNull ClientLevel world) {
        if (!CONFIG.enabled)
            return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isFallFlying()) {
            state = FlyState.NOT_FLYING;
            return;
        }
        if (state == FlyState.NOT_FLYING) { // we weren't flying, but now are: pitch down to get speed
            state = FlyState.GLIDING_DOWN;
            pitch = CONFIG.pitchDown;
        }
        if (state == FlyState.PITCHING_DOWN) {
            pitch += Math.min(CONFIG.pitchDown - pitch, CONFIG.pitchDownSpeed); // change pitch by no more than
                                                                                // pitchDownSpeed

            // check we are above lowest_y to prevent instantly pitching down from leftover
            // downwards velocity after gliding down
            boolean movingDownwards = player.getDeltaMovement().y <= 0 && player.getY() > lowest_y;

            if (pitch >= CONFIG.pitchDown || movingDownwards) {
                pitch = CONFIG.pitchDown;
                state = FlyState.GLIDING_DOWN;
            }
        }
        if (state == FlyState.GLIDING_DOWN) {
            boolean willCollide = this.willCollideWhileGliding(player, CONFIG.ticksCollisionLookAhead);

            if (willCollide || player.getDeltaMovement().horizontalDistanceSqr() > Math.pow(CONFIG.pitchUpVelocity, 2)) {
                pitch = CONFIG.pitchUp;
                state = FlyState.PITCHING_DOWN;
                lowest_y = player.getY();
            }
        }
        player.setXRot(pitch);
    }

    @Override
    public void onEndTick(@NotNull Minecraft client) {
        while (toggleKeybind.consumeClick()) {
            if (client.player == null)
                continue;
            MutableComponent msg = Component.translatable("key.category.minecraft.elytrainfinite");
            msg.append(" ");
            if (!CONFIG.enabled) {
                CONFIG.enabled = true;
                ModConfig.HANDLER.save();
                msg.append(Component.translatable("message.elytrainfinite.on").withStyle(ChatFormatting.GREEN));
            } else {
                CONFIG.enabled = false;
                ModConfig.HANDLER.save();
                msg.append(Component.translatable("message.elytrainfinite.off").withStyle(ChatFormatting.RED));
            }
            client.player.displayClientMessage(msg, true);
        }
    }

    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull Level world, @NotNull InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() == Items.FIREWORK_ROCKET && CONFIG.enabled
                && !player.isSpectator() && player.isFallFlying()) {
            pitch = CONFIG.pitchUp;
            player.setXRot(pitch);
            state = FlyState.PITCHING_DOWN;
            lowest_y = player.getY();
        }
        return InteractionResult.PASS;
    }

    @SuppressWarnings("resource") // avoid IDE complaining about not closing level
    private boolean willCollideWhileGliding(LocalPlayer player, int ticks) {
        LivingEntityInvoker glidingPlayer = (LivingEntityInvoker) player;
        Vec3 velocity = player.getDeltaMovement();
        AABB boundingBox = player.getBoundingBox();
        for (int i = 0; i < ticks; i++) {
            velocity = glidingPlayer.invokeUpdateFallFlyingMovement(velocity);
            boundingBox = boundingBox.move(velocity);
            if (!player.level().noCollision(null, boundingBox, true)) {
                return true;
            }
        }
        return false;
    }
}