package com.freecam.mixin;

import com.freecam.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true, require = 0)
    private void freecam$turn(double yRot, double xRot, CallbackInfo ci) {
        if (Freecam.active && (Object) this == Minecraft.getInstance().player) {
            Freecam.look(yRot, xRot);
            ci.cancel();
        }
    }
}
