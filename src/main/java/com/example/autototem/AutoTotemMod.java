package com.example.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoTotemMod implements ClientModInitializer {

    public static boolean enabled = true;
    public static int heartThreshold = 6; // 0-20 kalp

    private static KeyBinding settingsKey;

    @Override
    public void onInitializeClient() {
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autototem.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.autototem"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (settingsKey.wasPressed()) {
                client.setScreen(new SettingsScreen(client.currentScreen));
            }
            tryEquipTotem(client);
        });
    }

    private static void tryEquipTotem(MinecraftClient client) {
        if (!enabled) return;
        PlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) return;

        float limit = Math.max(0, Math.min(20, heartThreshold)) * 2.0f;
        if (player.getHealth() > limit) return;
        if (player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                int screenSlot = (i < 9) ? i + 36 : i;
                client.interactionManager.clickSlot(
                        player.playerScreenHandler.syncId,
                        screenSlot,
                        40,
                        SlotActionType.SWAP,
                        player);
                return;
            }
        }
    }

    public static class SettingsScreen extends Screen {

        private final Screen parent;

        public SettingsScreen(Screen parent) {
            super(Text.literal("Auto Totem"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int cy = this.height / 2;

            addDrawableChild(ButtonWidget.builder(statusText(), b -> {
                        AutoTotemMod.enabled = !AutoTotemMod.enabled;
                        b.setMessage(statusText());
                    })
                    .dimensions(cx - 100, cy - 60, 200, 20)
                    .build());

            addDrawableChild(ButtonWidget.builder(Text.literal("-"), b ->
                            AutoTotemMod.heartThreshold = Math.max(0, AutoTotemMod.heartThreshold - 1))
                    .dimensions(cx - 100, cy - 20, 40, 20)
                    .build());

            addDrawableChild(ButtonWidget.builder(Text.literal("+"), b ->
                            AutoTotemMod.heartThreshold = Math.min(20, AutoTotemMod.heartThreshold + 1))
                    .dimensions(cx + 60, cy - 20, 40, 20)
                    .build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Kapat"), b ->
                            this.client.setScreen(parent))
                    .dimensions(cx - 100, cy + 40, 200, 20)
                    .build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            int cx = this.width / 2;
            int cy = this.height / 2;
            context.drawCenteredTextWithShadow(this.textRenderer,
                    "Tetiklenme: " + AutoTotemMod.heartThreshold + " kalp (" + (AutoTotemMod.heartThreshold * 2) + " can)",
                    cx, cy - 12, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer,
                    "Mod: " + (AutoTotemMod.enabled ? "AÇIK" : "KAPALI"),
                    cx, cy - 80, 0xFFFFFF);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        private Text statusText() {
            return Text.literal("Durum: " + (AutoTotemMod.enabled ? "AÇIK" : "KAPALI"));
        }
    }
}
