package com.example.fps;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FpsScreen extends Screen {
    private ButtonWidget tickLabel;
    private KeyBinding listeningFor = null;
    private ButtonWidget listeningButton = null;
    private String listeningLabel = "";

    public FpsScreen() {
        super(Text.literal("fps"));
    }

    private static Text enabledText() {
        return Text.literal("Enabled: " + (Config.enabled ? "ON" : "OFF"));
    }

    private static Text tickText() {
        return Text.literal("Bow release: " + Config.drawTicks + " ticks");
    }

    private static Text keyText(String label, KeyBinding key) {
        return Text.literal(label + ": ").append(key.getBoundKeyLocalizedText());
    }

    private void changeTicks(int amount) {
        Config.drawTicks = Config.clamp(Config.drawTicks + amount);
        tickLabel.setMessage(tickText());
    }

    private void addKeyButton(String label, KeyBinding key, int cx, int y) {
        addDrawableChild(ButtonWidget.builder(keyText(label, key), b -> {
            if (listeningButton != null && listeningFor != null) {
                listeningButton.setMessage(keyText(listeningLabel, listeningFor));
            }
            listeningFor = key;
            listeningButton = b;
            listeningLabel = label;
            b.setMessage(Text.literal(label + ": > press a key <"));
        }).dimensions(cx - 100, y, 200, 20).build());
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 60;

        addDrawableChild(ButtonWidget.builder(enabledText(), b -> {
            Config.enabled = !Config.enabled;
            b.setMessage(enabledText());
        }).dimensions(cx - 100, y, 200, 20).build());

        // [-]  label  [+]
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> changeTicks(-1))
                .dimensions(cx - 100, y + 24, 24, 20).build());
        tickLabel = addDrawableChild(ButtonWidget.builder(tickText(), b -> {
            Config.drawTicks = Config.MIN_TICKS;
            b.setMessage(tickText());
        }).dimensions(cx - 74, y + 24, 148, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> changeTicks(1))
                .dimensions(cx + 76, y + 24, 24, 20).build());

        addKeyButton("Toggle mod", FpsClient.toggleKey, cx, y + 54);
        addKeyButton("Open menu", FpsClient.menuKey, cx, y + 78);

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - 100, y + 108, 200, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (listeningFor == null) return;

        long window = MinecraftClient.getInstance().getWindow().getHandle();

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            finishListening(null);
            return;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_BACKSPACE) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DELETE) == GLFW.GLFW_PRESS) {
            finishListening(InputUtil.UNKNOWN_KEY);
            return;
        }
        for (int code = GLFW.GLFW_KEY_SPACE; code <= GLFW.GLFW_KEY_LAST; code++) {
            if (code == GLFW.GLFW_KEY_ESCAPE) continue;
            if (GLFW.glfwGetKey(window, code) == GLFW.GLFW_PRESS) {
                finishListening(InputUtil.Type.KEYSYM.createFromCode(code));
                return;
            }
        }
    }

    private void finishListening(InputUtil.Key newKey) {
        KeyBinding key = listeningFor;
        ButtonWidget button = listeningButton;
        String label = listeningLabel;
        listeningFor = null;
        listeningButton = null;

        if (newKey != null) {
            key.setBoundKey(newKey);
            KeyBinding.updateKeysByCode();
            MinecraftClient.getInstance().options.write();
        }
        button.setMessage(keyText(label, key));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return listeningFor == null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("3 = fastest (minimum to fire), 20 = full charge. Click the label to reset."),
                this.width / 2, this.height / 2 + 80, 0xFFAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Click a key button, then press a key. Backspace = unbind, Esc = cancel."),
                this.width / 2, this.height / 2 + 92, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        Config.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
