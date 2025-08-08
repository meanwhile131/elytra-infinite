package meanwhile131.elytrainfinite.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("calcGlidingVelocity")
    public Vec3d invokeCalcGlidingVelocity(Vec3d oldVelocity);
}