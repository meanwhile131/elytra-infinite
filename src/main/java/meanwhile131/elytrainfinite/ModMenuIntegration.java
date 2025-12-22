package meanwhile131.elytrainfinite;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("key.category.minecraft.elytrainfinite"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("key.category.minecraft.elytrainfinite"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("elytrainfinite.enabled"))
                                .description(OptionDescription.of(Component.translatable("elytrainfinite.enabled_description")))
                                .binding(ModConfig.HANDLER.defaults().enabled,
                                        () -> ModConfig.HANDLER.instance().enabled,
                                        newVal -> ModConfig.HANDLER.instance().enabled = newVal)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("elytrainfinite.pitchsettings"))
                                .description(OptionDescription
                                        .of(Component.translatable("elytrainfinite.pitchsettings_description")))
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("elytrainfinite.pitchdown"))
                                        .description(OptionDescription
                                                .of(Component.translatable("elytrainfinite.pitchdown_description")))
                                        .binding(ModConfig.HANDLER.defaults().pitchDown,
                                                () -> ModConfig.HANDLER.instance().pitchDown,
                                                newVal -> ModConfig.HANDLER.instance().pitchDown = newVal)
                                        .controller(FloatFieldControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("elytrainfinite.pitchup"))
                                        .description(OptionDescription
                                                .of(Component.translatable("elytrainfinite.pitchup_description")))
                                        .binding(ModConfig.HANDLER.defaults().pitchUp,
                                                () -> ModConfig.HANDLER.instance().pitchUp,
                                                newVal -> ModConfig.HANDLER.instance().pitchUp = newVal)
                                        .controller(FloatFieldControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("elytrainfinite.pitchdownspeed"))
                                        .description(OptionDescription
                                                .of(Component.translatable("elytrainfinite.pitchdownspeed_description")))
                                        .binding(ModConfig.HANDLER.defaults().pitchDownSpeed,
                                                () -> ModConfig.HANDLER.instance().pitchDownSpeed,
                                                newVal -> ModConfig.HANDLER.instance().pitchDownSpeed = newVal)
                                        .controller(FloatFieldControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder().name(Component.translatable("elytrainfinite.pitchtriggers"))
                                .description(OptionDescription
                                        .of(Component.translatable("elytrainfinite.pitchtriggers_description")))
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("elytrainfinite.pitchupvelocity"))
                                        .description(OptionDescription
                                                .of(Component.translatable("elytrainfinite.pitchupvelocity_description")))
                                        .binding(ModConfig.HANDLER.defaults().pitchUpVelocity,
                                                () -> ModConfig.HANDLER.instance().pitchUpVelocity,
                                                newVal -> ModConfig.HANDLER.instance().pitchUpVelocity = newVal)
                                        .controller(DoubleFieldControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("elytrainfinite.tickscollisionlookahead"))
                                        .description(OptionDescription
                                                .of(Component.translatable("elytrainfinite.tickscollisionlookahead_description")))
                                        .binding(ModConfig.HANDLER.defaults().ticksCollisionLookAhead,
                                                () -> ModConfig.HANDLER.instance().ticksCollisionLookAhead,
                                                newVal -> ModConfig.HANDLER.instance().ticksCollisionLookAhead = newVal)
                                        .controller(IntegerFieldControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .save(ModConfig.HANDLER::save)
                .build()
                .generateScreen(parentScreen);
    }
}
