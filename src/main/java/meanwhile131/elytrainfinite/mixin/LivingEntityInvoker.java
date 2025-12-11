package meanwhile131.elytrainfinite.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("updateFallFlyingMovement")
    public Vec3 invokeUpdateFallFlyingMovement(Vec3 oldVelocity);
}