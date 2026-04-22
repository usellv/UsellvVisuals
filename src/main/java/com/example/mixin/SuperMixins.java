package com.example.mixin;

import com.example.SuperMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class SuperMixins {

    @Mixin(BackgroundRenderer.class)
    public static class Fog {
        @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
        private static void onFog(Camera c, BackgroundRenderer.FogType f, float d, boolean t, float dt, CallbackInfo ci) {
            if (SuperMod.cfg.noFog) ci.cancel();
        }
    }

    @Mixin(InGameHud.class)
    public static class Hud {
        @Inject(method = "render", at = @At("TAIL"))
        private void onRender(DrawContext context, float delta, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            int color = SuperMod.COLORS[SuperMod.cfg.colorIndex] | 0xFF000000;

            if (SuperMod.cfg.armorHud) {
                int x = context.getScaledWindowWidth() / 2 + 15;
                int y = context.getScaledWindowHeight() - 55;
                for (ItemStack stack : client.player.getArmorItems()) {
                    if (!stack.isEmpty()) {
                        context.drawItem(stack, x, y);
                        context.drawItemInSlot(client.textRenderer, stack, x, y);
                        x += 18;
                    }
                }
            }

            if (SuperMod.cfg.crosshair) {
                int cx = context.getScaledWindowWidth() / 2, cy = context.getScaledWindowHeight() / 2;
                context.fill(cx - 4, cy, cx + 5, cy + 1, color);
                context.fill(cx, cy - 4, cx + 1, cy + 5, color);
            }
        }
    }

    @Mixin(HeldItemRenderer.class)
    public static class Hand {
        @Inject(method = "applyEquipOffset", at = @At("TAIL"))
        private void onRotation(MatrixStack m, net.minecraft.util.Arm a, float p, CallbackInfo ci) {
            if (SuperMod.cfg.axX != 0) {
                m.multiply(RotationAxis.POSITIVE_X.getDegreesQuaternion(SuperMod.cfg.axX));
            }
        }
    }
}
