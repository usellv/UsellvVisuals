package com.example.mixin;

import com.example.SuperMod;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Camera;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class SuperMixins {

    @Mixin(BackgroundRenderer.class)
    public static class Fog {
        @Inject(method = "applyFog", at = @At("TAIL"), cancellable = true)
        private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
            if (SuperMod.cfg.noFog) {
                net.com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(viewDistance * 4);
                net.com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(viewDistance * 5);
            }
        }
    }

    @Mixin(InGameHud.class)
    public static class Hud {
        @Inject(method = "render", at = @At("TAIL"))
        private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
            if (SuperMod.cfg.armorHud) {
                // Здесь можно добавить отрисовку брони, если нужно
            }
        }
    }

    @Mixin(HeldItemRenderer.class)
    public static class Hand {
        @Inject(method = "renderItem", at = @At("INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
        private void onRenderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, net.minecraft.client.network.ClientPlayerEntity player, int light, CallbackInfo ci) {
            // Наклон меча (Old Animations style)
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(10f));
        }
    }
}

