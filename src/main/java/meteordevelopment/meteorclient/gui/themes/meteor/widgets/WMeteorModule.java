/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WMeteorModule extends WPressable implements MeteorWidget {
    private final Module module;
    private final String title;

    private double titleWidth;

    private double animationProgress1;

    private double animationProgress2;

    public WMeteorModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.tooltip = module.description;

        if (module.isActive()) {
            animationProgress1 = 1;
            animationProgress2 = 1;
        } else {
            animationProgress1 = 0;
            animationProgress2 = 0;
        }
    }

    @Override
    public double pad() {
        return theme.scale(4);
    }

    private double buttonSize() {
        return theme.scale(16);
    }

    private double buttonGap() {
        return theme.scale(4);
    }

    private double buttonsWidth() {
        return buttonSize() * 2 + buttonGap();
    }

    private boolean isInsideColorButton(double mouseX, double mouseY) {
        double pad = pad();
        double size = buttonSize();
        double x = this.x + width - pad - size;
        double y = this.y + pad;
        return mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
    }

    private boolean isInsideShapeButton(double mouseX, double mouseY) {
        double pad = pad();
        double size = buttonSize();
        double x = this.x + width - pad - size * 2 - buttonGap();
        double y = this.y + pad;
        return mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
    }

    private String accentHex() {
        Color accent = theme().accentColor.get();
        return String.format("#%02X%02X%02X", accent.r, accent.g, accent.b);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        if (titleWidth == 0) titleWidth = theme.textWidth(title);

        width = pad + titleWidth + pad + buttonsWidth();
        height = pad + theme.textHeight() + pad;
    }

    @Override
    public boolean onMouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        if (isInsideColorButton(click.x(), click.y())) {
            if (click.button() == GLFW_MOUSE_BUTTON_LEFT) {
                OkPrompt.create()
                    .title("UI Accent Color")
                    .message("Current UI accent: " + accentHex())
                    .message("This value is used across the interface.")
                    .dontShowAgainCheckboxVisible(false)
                    .show();
                return true;
            }
        }

        if (isInsideShapeButton(click.x(), click.y())) {
            if (click.button() == GLFW_MOUSE_BUTTON_LEFT) {
                OkPrompt.create()
                    .title("Button Shape")
                    .message("Buttons are using a rounded square style.")
                    .message("This module item is now grouped with its controls.")
                    .dontShowAgainCheckboxVisible(false)
                    .show();
                return true;
            }
        }

        return super.onMouseClicked(click, doubled);
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) mc.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();
        double btnSize = buttonSize();
        double gap = buttonGap();

        animationProgress1 += delta * 4 * ((module.isActive() || mouseOver) ? 1 : -1);
        animationProgress1 = MathHelper.clamp(animationProgress1, 0, 1);

        animationProgress2 += delta * 6 * (module.isActive() ? 1 : -1);
        animationProgress2 = MathHelper.clamp(animationProgress2, 0, 1);

        Color bg = theme.moduleBackground.get();
        if (mouseOver) {
            bg = bg.copy();
            bg.a = Math.min(255, bg.a + 50);
        }

        renderer.roundedQuad(this, theme.scale(4), bg);

        if (animationProgress2 > 0) {
            double sideWidth = theme.scale(2);
            renderer.quad(x, y + sideWidth, sideWidth, height - sideWidth * 2, theme.accentColor.get());
        }

        double buttonY = y + pad;
        double colorX = this.x + width - pad - btnSize;
        double shapeX = this.x + width - pad - btnSize * 2 - gap;

        boolean hoverColor = isInsideColorButton(mouseX, mouseY);
        boolean hoverShape = isInsideShapeButton(mouseX, mouseY);

        Color buttonBase = theme.moduleBackground.get().copy();
        buttonBase.a = Math.min(255, buttonBase.a + 40);
        Color hoverBase = buttonBase.copy();
        hoverBase.a = Math.min(255, hoverBase.a + 60);

        renderer.roundedQuad(shapeX, buttonY, btnSize, btnSize, theme.scale(4), hoverShape ? hoverBase : buttonBase);
        renderer.quad(shapeX + btnSize, buttonY + btnSize / 2 - theme.scale(1), gap, theme.scale(2), theme.accentColor.get());
        renderer.roundedQuad(colorX, buttonY, btnSize, btnSize, theme.scale(4), hoverColor ? hoverBase : buttonBase);
        renderer.quad(colorX + theme.scale(2), buttonY + theme.scale(2), btnSize - theme.scale(4), btnSize - theme.scale(4), theme.accentColor.get());

        String shapeText = "Sq";
        double textWidth = theme.textWidth(shapeText);
        renderer.text(shapeText, shapeX + btnSize / 2 - textWidth / 2, buttonY + (btnSize - theme.textHeight()) / 2, theme.textColor.get(), false);

        double x = this.x + pad + theme.scale(3);
        double w = width - pad * 2 - theme.scale(3) - buttonsWidth();

        if (theme.moduleAlignment.get() == AlignmentX.Center) {
            x += w / 2 - titleWidth / 2;
        }
        else if (theme.moduleAlignment.get() == AlignmentX.Right) {
            x += w - titleWidth;
        }

        renderer.text(title, x, y + pad, theme.textColor.get(), false);
    }
}
