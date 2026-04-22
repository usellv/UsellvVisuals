package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class SuperMod implements ModInitializer, ClientModInitializer {
    public static Config cfg = Config.sync();
    private static KeyBinding menuKey, totemBind, crystalBind;

    @Override public void onInitialize() {}

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Меню", GLFW.GLFW_KEY_RIGHT_SHIFT, "SuperMod"));
        totemBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("Свап Тотема", GLFW.GLFW_KEY_G, "SuperMod"));
        crystalBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("Свап Кристалла", GLFW.GLFW_KEY_V, "SuperMod"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) client.setScreen(new VisualScreen());
            if (client.player != null && cfg.autoSwap) {
                if (totemBind.wasPressed()) performSwap(client, Items.TOTEM_OF_UNDYING);
                if (crystalBind.wasPressed()) performSwap(client, Items.END_CRYSTAL);
            }
        });
    }

    private static void performSwap(MinecraftClient client, net.minecraft.item.Item item) {
        if (client.player == null || client.interactionManager == null) return;
        int slot = -1;
        for (int i = 0; i < 45; i++) {
            if (client.player.getInventory().getStack(i).getItem() == item) {
                slot = i; if (slot < 9) slot += 36; break;
            }
        }
        if (slot != -1) client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slot, 40, SlotActionType.SWAP, client.player);
    }

    public static class VisualScreen extends Screen {
        public VisualScreen() { super(Text.of("SuperMod")); }
        @Override
        protected void init() {
            int x = width / 2 - 100;
            addDrawableChild(ButtonWidget.builder(Text.of("AutoSwap: " + (cfg.autoSwap ? "ON" : "OFF")), b -> { 
                cfg.autoSwap = !cfg.autoSwap; cfg.save(); b.setMessage(Text.of("AutoSwap: " + (cfg.autoSwap ? "ON" : "OFF"))); 
            }).dimensions(x, 50, 200, 20).build());
        }
        @Override public void render(DrawContext c, int mx, int my, float d) { super.render(c, mx, my, d); }
    }

    public static class Config {
        public boolean autoSwap = true;
        private static final File F = FabricLoader.getInstance().getConfigDir().resolve("supermod.json").toFile();
        public static Config sync() {
            try { if (F.exists()) return new Gson().fromJson(new FileReader(F, StandardCharsets.UTF_8), Config.class); } catch (Exception e) {}
            return new Config();
        }
        public void save() { try (Writer w = new OutputStreamWriter(new FileOutputStream(F), StandardCharsets.UTF_8)) { new GsonBuilder().setPrettyPrinting().create().toJson(this, w); } catch (Exception e) {} }
    }
}

