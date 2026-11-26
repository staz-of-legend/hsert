package com.example.fps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FpsClient implements ClientModInitializer {
    static KeyBinding toggleKey;
    static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("fps", "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fps.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fps.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) return;

            while (toggleKey.wasPressed()) {
                Config.enabled = !Config.enabled;
                Config.save();
                player.sendMessage(Text.literal("fps: " + (Config.enabled ? "ON" : "OFF")), true);
            }

            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new FpsScreen());
            }

            if (!Config.enabled || client.currentScreen != null || client.interactionManager == null) return;

            if (client.options.useKey.isPressed()
                    && player.isUsingItem()
                    && player.getActiveItem().isOf(Items.BOW)
                    && player.getItemUseTime() >= Config.drawTicks) {
                // Release the arrow; right-click is still held, so vanilla starts a new draw next tick.
                client.interactionManager.stopUsingItem(player);
            }
        });
    }
}
