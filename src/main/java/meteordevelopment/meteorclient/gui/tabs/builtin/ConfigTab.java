/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.prompts.YesNoPrompt;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screen.Screen;

public class ConfigTab extends Tab {
    public ConfigTab() {
        super("Config");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new ConfigScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof ConfigScreen;
    }

    public static class ConfigScreen extends WindowTabScreen {
        private final Settings settings;

        public ConfigScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            settings = Config.get().settings;
            settings.onActivated();

            onClosed(() -> {
                String prefix = Config.get().prefix.get();

                if (prefix.isBlank()) {
                    YesNoPrompt.create(theme, this.parent)
                        .title("Empty command prefix")
                        .message("You have set your command prefix to nothing.")
                        .message("This WILL prevent you from sending chat messages.")
                        .message("Do you want to reset your prefix back to '.'?")
                        .onYes(() -> Config.get().prefix.set("."))
                        .id("empty-command-prefix")
                        .show();
                }
                else if (prefix.equals("/")) {
                    YesNoPrompt.create(theme, this.parent)
                        .title("Potential prefix conflict")
                        .message("You have set your command prefix to '/', which is used by minecraft.")
                        .message("This can cause conflict issues between meteor and minecraft commands.")
                        .message("Do you want to reset your prefix to '.'?")
                        .onYes(() -> Config.get().prefix.set("."))
                        .id("minecraft-prefix-conflict")
                        .show();
                }
                else if (prefix.length() > 7) {
                    YesNoPrompt.create(theme, this.parent)
                        .title("Long command prefix")
                        .message("You have set your command prefix to a very long string.")
                        .message("This means that in order to execute any command, you will need to type %s followed by the command you want to run.", prefix)
                        .message("Do you want to reset your prefix back to '.'?")
                        .onYes(() -> Config.get().prefix.set("."))
                        .id("long-command-prefix")
                        .show();
                }
            });
        }

        @Override
        public void initWidgets() {
            WHorizontalList root = add(theme.horizontalList()).expandX().widget();

            root.add(theme.settings(settings)).expandX().widget();

            WSection share = root.add(theme.section("Config Share")).minWidth(260).widget();
            share.add(theme.label("Save & share your Meteor config.")).widget().color(theme.textSecondaryColor());

            WHorizontalList buttons = share.add(theme.horizontalList()).expandX().widget();

            WButton save = buttons.add(theme.button("Save")).expandCellX().widget();
            save.action = () -> {
                Config.get().save();
                ChatUtils.info("Saved config to disk.");
            };

            WButton copy = buttons.add(theme.button(GuiRenderer.COPY)).widget();
            copy.action = () -> {
                if (toClipboard()) ChatUtils.info("Config copied to clipboard.");
                else ChatUtils.error("Could not copy config to clipboard.");
            };

            WButton paste = buttons.add(theme.button(GuiRenderer.PASTE)).widget();
            paste.action = () -> {
                if (fromClipboard()) {
                    ChatUtils.info("Config loaded from clipboard.");
                    reload();
                } else ChatUtils.error("Could not read config from clipboard.");
            };

            share.add(theme.label("Hex color values:")).widget().color(theme.textSecondaryColor());
            share.add(theme.label("Friend: " + toHex(Config.get().friendColor.get()))).widget();
            share.add(theme.label("Prefix: " + hexFromString(Config.get().prefix.get()))).widget();
            share.add(theme.label("Use Ctrl+C / Ctrl+V to copy and paste config data."));
        }

        private String toHex(Color color) {
            return String.format("#%02X%02X%02X", color.r, color.g, color.b);
        }

        private String hexFromString(String value) {
            if (value == null || value.isEmpty()) return "#000000";
            int hash = value.hashCode();
            int r = (hash >> 16) & 0xFF;
            int g = (hash >> 8) & 0xFF;
            int b = hash & 0xFF;
            return String.format("#%02X%02X%02X", r, g, b);
        }

        @Override
        public void tick() {
            super.tick();

            settings.tick(window, theme);
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Config.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Config.get());
        }
    }
}
