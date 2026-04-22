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
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
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
    private static ServerInfo lastServer;

    public static final int[] COLORS = {0xFFFFFF, 0xFF5555, 0x55FF55, 0x5555FF, 0xFFFF55, 0xFF55FF, 0x55FFFF};
    public static final String[] COLOR_NAMES = {"Белый", "Красный", "Зеленый", "Синий", "Желтый", "Розовый", "Голубой"};

    @Override public void onInitialize() {}

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Меню Визуалов", GLFW.GLFW_KEY_RIGHT_SHIFT, "SuperMod"));
        totemBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("Свап Тотема", GLFW.GLFW_KEY_G, "SuperMod"));
        crystalBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("Свап Сферы/Кристалла", GLFW.GLFW_KEY_V, "SuperMod"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) client.setScreen(new VisualScreen());
            if (client.player != null) {
                if (client.getCurrentServerEntry() != null) lastServer = client.getCurrentServerEntry();
                if (cfg.autoSwap) {
                    if (totemBind.wasPressed()) performSwap(client, Items.TOTEM_OF_UNDYING);
                    if (crystalBind.wasPressed()) performSwap(client, Items.END_CRYSTAL);
                }
                // Glow в 1.21
                if (cfg.blockHighlight) client.player.setGlowing(true);
                else if (client.player.isGlowing()) client.player.setGlowing(false);
                
                if (cfg.worldEffects && client.world != null) client.world.setTimeOfDay(13000L);
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

    public static void runSkinMacro(MinecraftClient client) {
        if (client.player != null && client.player.networkHandler != null) {
            client.player.networkHandler.sendChatCommand("skin 1488");
            client.player.networkHandler.sendChatCommand("hub");
            new Thread(() -> {
                try { Thread.sleep(2000);
                    if (lastServer != null) client.execute(() -> net.minecraft.client.gui.screen.multiplayer.ConnectScreen.connect(null, client, ServerAddress.parse(lastServer.address), lastServer, false, null));
                } catch (Exception ignored) {}
            }).start();
        }
    }

    public static class VisualScreen extends Screen {
        public VisualScreen() { super(Text.of("SuperMod 1.21")); }
        private Text st(String n, boolean a) { return Text.of(n + (a ? " §a[ON]" : " §c[OFF]")); }

        @Override
        protected void init() {
            int x = width / 2 - 100; int y = 10;
            addDrawableChild(ButtonWidget.builder(Text.of("§6⚡ §lОБНОВИТЬ СКИН §6⚡"), b -> { runSkinMacro(client); this.close(); }).dimensions(x, y, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(st("§6♻ AutoSwap", cfg.autoSwap), b -> { cfg.autoSwap = !cfg.autoSwap; cfg.save(); b.setMessage(st("§6♻ AutoSwap", cfg.autoSwap)); }).dimensions(x, y+22, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(st("§b🛡 Armor HUD", cfg.armorHud), b -> { cfg.armorHud = !cfg.armorHud; cfg.save(); b.setMessage(st("§b🛡 Armor HUD", cfg.armorHud)); }).dimensions(x, y+44, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(st("§f🎯 Crosshair", cfg.crosshair), b -> { cfg.crosshair = !cfg.crosshair; cfg.save(); b.setMessage(st("§f🎯 Crosshair", cfg.crosshair)); }).dimensions(x, y+66, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(st("§e👒 China Hat", cfg.chinaHat), b -> { cfg.chinaHat = !cfg.chinaHat; cfg.save(); b.setMessage(st("§e👒 China Hat", cfg.chinaHat)); }).dimensions(x, y+88, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.of("§6🎨 Цвет: " + COLOR_NAMES[cfg.colorIndex]), b -> { cfg.colorIndex = (cfg.colorIndex + 1) % COLORS.length; cfg.save(); b.setMessage(Text.of("§6🎨 Цвет: " + COLOR_NAMES[cfg.colorIndex])); }).dimensions(x, y+110, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(st("§f🌫 NoFog", cfg.noFog), b -> { cfg.noFog = !cfg.noFog; cfg.save(); b.setMessage(st("§f🌫 NoFog", cfg.noFog)); }).dimensions(x, y+132, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.of("§7Закрыть"), b -> this.close()).dimensions(x, height - 25, 200, 20).build());
        }

        @Override public void render(DrawContext c, int mx, int my, float d) {
            super.render(c, mx, my, d); 
        }
    }

    public static class Config {
        public boolean noFog = false, autoSwap = true, armorHud = true, crosshair = true, chinaHat = true, blockHighlight = false, worldEffects = false;
        public float axX = 45; public int colorIndex = 0;
        private static final File F = FabricLoader.getInstance().getConfigDir().resolve("supermod.json").toFile();
        public static Config sync() {
            try { if (F.exists()) return new Gson().fromJson(new FileReader(F, StandardCharsets.UTF_8), Config.class); } catch (Exception e) {}
            return new Config();
        }
        public void save() { try (Writer w = new OutputStreamWriter(new FileOutputStream(F), StandardCharsets.UTF_8)) { new GsonBuilder().setPrettyPrinting().create().toJson(this, w); } catch (Exception e) {} }
    }
}
