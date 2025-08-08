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
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

enum FlyState {
	TOGGLED_OFF,
	NOT_FLYING,
	PITCHING_DOWN,
	GLIDING_DOWN
};

public class ElytraInfinite implements ClientModInitializer {
	public static final String MOD_ID = "elytra-infinite";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	static final float pitchDown = 34f;
	static final float pitchUp = -47f;
	static final float pitchDownSpeed = 0.5f;
	static final int pitchUpHeight = 5;
	static final double pitchUpVelocity = 2.4f;
	static final int ticksCollisionLookAhead = 15;
	private FlyState state = FlyState.NOT_FLYING;
	private static KeyBinding toggleKeybind;
	private float pitch;
	private double lowest_y;

	@Override
	public void onInitializeClient() {
		toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.elytrainfinite.toggle",
				GLFW.GLFW_KEY_H,
				"category.elytrainfinite"));
		ClientTickEvents.START_WORLD_TICK.register(world -> {
			if (state == FlyState.TOGGLED_OFF)
				return;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null || !player.isGliding()) {
				state = FlyState.NOT_FLYING;
				return;
			}
			if (state == FlyState.NOT_FLYING) {
				state = FlyState.GLIDING_DOWN;
				pitch = pitchDown;
			}
			if (state == FlyState.PITCHING_DOWN) {
				pitch += Math.min(pitchDown - pitch, pitchDownSpeed); // change pitch by no more than pitchDownSpeed

				// check we are above lowest_y to prevent instantly pitching down from leftover
				// downwards velocity after gliding down
				boolean movingDownwards = player.getVelocity().y <= 0 && player.getY() > lowest_y;

				if (pitch >= pitchDown || movingDownwards) {
					pitch = pitchDown;
					state = FlyState.GLIDING_DOWN;
				}
			}
			if (state == FlyState.GLIDING_DOWN) {
				boolean willCollide = this.willCollideWhileGliding(player, ticksCollisionLookAhead);

				if (willCollide || player.getVelocity().horizontalLengthSquared() > pitchUpVelocity) {
					pitch = pitchUp;
					state = FlyState.PITCHING_DOWN;
					lowest_y = player.getY();
				}
			}
			player.setPitch(pitch);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKeybind.wasPressed()) {
				MutableText msg = Text.translatable("category.elytrainfinite");
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
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (player.getStackInHand(hand).getItem() == Items.FIREWORK_ROCKET && state != FlyState.TOGGLED_OFF
					&& !player.isSpectator() && player.isGliding()) {
				player.setPitch(pitchUp);
				pitch = pitchUp;
				state = FlyState.PITCHING_DOWN;
				lowest_y = player.getY();
			}
			return ActionResult.PASS;
		});
		LOGGER.info("Elytra Infinite loaded.");
	}

	private boolean willCollideWhileGliding(ClientPlayerEntity player, int ticks) {
		LivingEntityInvoker glidingPlayer = (LivingEntityInvoker) player;
		Vec3d velocity = player.getVelocity();
		Box boundingBox = player.getBoundingBox();
		for (int i = 0; i < ticks; i++) {
			velocity = glidingPlayer.invokeCalcGlidingVelocity(velocity);
			boundingBox = boundingBox.offset(velocity);
			if (!player.getWorld().isSpaceEmpty(null, boundingBox, true)) {
				return true;
			}
		}
		return false;
	}
}