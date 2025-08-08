package meanwhile131.elytrainfinite;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class ModConfig {
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.of(ElytraInfinite.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("elytra_infinite.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public float pitchDown = 30f;
    @SerialEntry
    public float pitchUp = -48f;
    @SerialEntry
    public float pitchDownSpeed = 0.5f;
    @SerialEntry
    public double pitchUpVelocity = 2f;
    @SerialEntry
    public int ticksCollisionLookAhead = 10;
}