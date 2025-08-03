package meanwhile131.elytrainfinite;

import net.fabricmc.api.ClientModInitializer;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

enum FlyState {
	TOGGLED_OFF,
	NOT_FLYING,
	PITCHING_DOWN,
	GLIDING_DOWN
};

public class ElytraInfinite implements ClientModInitializer {
	public static final String MOD_ID = "elytra-autopilot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	static final float pitchDown = 34f;
	static final float pitchUp = -47f;
	static final float pitchDownSpeed = 0.5f;
	private FlyState state = FlyState.NOT_FLYING;
	private static KeyBinding toggleKeybind;

	@Override
	public void onInitializeClient() {
		toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.elytrainfinite.toggle",
			GLFW.GLFW_KEY_H,
			"category.elytrainfinite"
		));
		ClientTickEvents.START_WORLD_TICK.register(world -> {
			if (state == FlyState.TOGGLED_OFF) return;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null || !player.isGliding()) {
				state = FlyState.NOT_FLYING;
				return;
			}
			if (state == FlyState.NOT_FLYING) {
				state = FlyState.PITCHING_DOWN;
			}
			if (state == FlyState.PITCHING_DOWN) {
				float pitch = player.getPitch();
				pitch += Math.min(pitchDown - pitch, pitchDownSpeed); // change pitch by no more than pitchDownSpeed
				if (pitch >= pitchDown) // fully pitched down, start gliding
					state = FlyState.GLIDING_DOWN;
				player.setPitch(pitch);
			}
			if (state == FlyState.GLIDING_DOWN) {
				BlockPos pos = player.getBlockPos();
				int height = world.getChunk(pos).sampleHeightmap(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ());
				if (pos.getY() - height < 5) {
					player.setPitch(pitchUp);
					state = FlyState.PITCHING_DOWN;
				} else
					player.setPitch(pitchDown); // enforce down pitch
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKeybind.wasPressed()) {
				MutableText msg = Text.translatable("category.elytrainfinite");
				msg.append(" ");
				if (state == FlyState.TOGGLED_OFF) {
					state = FlyState.NOT_FLYING;
					msg.append(Text.translatable("message.elytrainfinite.on").formatted(Formatting.GREEN));
				}
				else {
					state = FlyState.TOGGLED_OFF;
					msg.append(Text.translatable("message.elytrainfinite.off").formatted(Formatting.RED));
				}
				client.player.sendMessage(msg, true);
			}
		});
		LOGGER.info("Elytra Infinite loaded.");
	}
}