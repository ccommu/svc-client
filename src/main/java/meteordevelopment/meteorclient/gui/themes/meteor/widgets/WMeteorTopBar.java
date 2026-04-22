/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorTopBar extends WTopBar implements MeteorWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        MeteorGuiTheme theme = theme();
        Color color = theme.backgroundColor.get(pressed, hovered).copy();
        color.a = 220;

        if (pressed) {
            Color active = theme.accentColor.get().copy();
            active.a = 180;
            return active;
        }

        if (hovered) {
            Color hover = color.copy();
            hover.a = Math.min(255, hover.a + 15);
            return hover;
        }

        return color;
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor.get();
    }
}
