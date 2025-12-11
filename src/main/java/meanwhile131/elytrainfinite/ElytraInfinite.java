package meanwhile131.elytrainfinite;

import net.fabricmc.api.ClientModInitializer;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import meanwhile131.elytrainfinite.mixin.LivingEntityInvoker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

enum FlyState {
    TOGGLED_OFF,
    NOT_FLYING,
    PITCHING_DOWN,
    GLIDING_DOWN
};

public class ElytraInfinite
        implements ClientModInitializer, ClientTickEvents.StartWorldTick, ClientTickEvents.EndTick, UseItemCallback {
    public static final String MOD_ID = "elytra-infinite";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig CONFIG;
    private FlyState state = FlyState.NOT_FLYING;
    private static KeyBinding toggleKeybind;
    private float pitch;
    private double lowest_y;

    @Override
    public void onInitializeClient() {
        ModConfig.HANDLER.load();
        CONFIG = ModConfig.HANDLER.instance();
        KeyBinding.Category keybindCategory = KeyBinding.Category.create(Identifier.of("elytrainfinite"));
        toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elytrainfinite.toggle",
                GLFW.GLFW_KEY_H,
                keybindCategory));
        ClientTickEvents.START_WORLD_TICK.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        UseItemCallback.EVENT.register(this);
        LOGGER.info("Elytra Infinite loaded.");
    }

    @Override
    public void onStartTick(ClientWorld world) {
        if (state == FlyState.TOGGLED_OFF)
            return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.isGliding()) {
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
            boolean movingDownwards = player.getVelocity().y <= 0 && player.getY() > lowest_y;

            if (pitch >= CONFIG.pitchDown || movingDownwards) {
                pitch = CONFIG.pitchDown;
                state = FlyState.GLIDING_DOWN;
            }
        }
        if (state == FlyState.GLIDING_DOWN) {
            boolean willCollide = this.willCollideWhileGliding(player, CONFIG.ticksCollisionLookAhead);

            if (willCollide || player.getVelocity().horizontalLengthSquared() > Math.pow(CONFIG.pitchUpVelocity, 2)) {
                pitch = CONFIG.pitchUp;
                state = FlyState.PITCHING_DOWN;
                lowest_y = player.getY();
            }
        }
        player.setPitch(pitch);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        while (toggleKeybind.wasPressed()) {
            MutableText msg = Text.translatable("key.category.minecraft.elytrainfinite");
            msg.append(" ");
            if (state == FlyState.TOGGLED_OFF) {
                state = FlyState.NOT_FLYING;
                msg.append(Text.translatable("message.elytrainfinite.on").formatted(Formatting.GREEN));
            } else {
                state = FlyState.TOGGLED_OFF;
                msg.append(Text.translatable("message.elytrainfinite.off").formatted(Formatting.RED));
            }
            client.player.sendMessage(msg, true);
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand) {
        if (player.getStackInHand(hand).getItem() == Items.FIREWORK_ROCKET && state != FlyState.TOGGLED_OFF
                && !player.isSpectator() && player.isGliding()) {
            pitch = CONFIG.pitchUp;
            player.setPitch(pitch);
            state = FlyState.PITCHING_DOWN;
            lowest_y = player.getY();
        }
        return ActionResult.PASS;
    }

    private boolean willCollideWhileGliding(ClientPlayerEntity player, int ticks) {
        LivingEntityInvoker glidingPlayer = (LivingEntityInvoker) player;
        Vec3d velocity = player.getVelocity();
        Box boundingBox = player.getBoundingBox();
        for (int i = 0; i < ticks; i++) {
            velocity = glidingPlayer.invokeCalcGlidingVelocity(velocity);
            boundingBox = boundingBox.offset(velocity);
            if (!player.getEntityWorld().isSpaceEmpty(null, boundingBox, true)) {
                return true;
            }
        }
        return false;
    }
}