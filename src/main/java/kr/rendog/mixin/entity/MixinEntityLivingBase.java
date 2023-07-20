package kr.rendog.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    @Unique
    private Vec3d modifiedVec = null;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @ModifyVariable(
        method = "travel(FFF)V",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/EntityLivingBase;getLookVec()Lnet/minecraft/util/math/Vec3d;", ordinal = 0)
    )
    private Vec3d vec3d(Vec3d original) {
        return original;
    }

    @ModifyVariable(
        method = "travel(FFF)V",
        at = @At(value = "STORE", ordinal = 0),
        ordinal = 3
    )
    private float f(float original) {
        return original;
    }

    @Inject(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionZ:D", ordinal = 3),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void getVec(
        float strafe,
        float vertical,
        float forward,
        CallbackInfo ci,
        // Local capture
        Vec3d vec3d
    ) {
        modifiedVec = vec3d;
    }

    @Redirect(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionX:D", ordinal = 7)
    )
    public double motionX(EntityLivingBase it) {
        return it.motionX;
    }

    @Redirect(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionY:D", ordinal = 7)
    )
    public double motionY(EntityLivingBase it) {
        return it.motionY;
    }

    @Redirect(
        method = "travel(FFF)V",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;motionZ:D", ordinal = 7)
    )
    public double motionZ(EntityLivingBase it) {
        return it.motionZ;
    }

}
