package io.dampen59.mineboxadditions.features.menu;

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.ahlerter.AhAlerterScreen;
import io.dampen59.mineboxadditions.features.atlas.MineboxAtlasScreen;
import io.dampen59.mineboxadditions.features.bestiary.BestiaryScreen;
import io.dampen59.mineboxadditions.features.harvestable.HarvestableScreen;
import io.dampen59.mineboxadditions.features.hud.HudEditorScreen;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class MineboxMenuScreen extends Screen {

    private static final int CARD_SIZE = 80;
    private static final int CARD_GAP = 32;
    private static final int CORNER_R = 10;
    private static final int BORDER_W = 2;
    private static final float ICON_SCALE = 3.0f;
    private static final int ICON_SRC_SIZE = 16;

    private static final int COL_BORDER = 0xFF444455;
    private static final int COL_BORDER_HOVER = 0xFFFFAA00;
    private static final int COL_TEXT = 0xFFFFFFFF;

    private static final Identifier AH_ALERTER_ICON =
        Identifier.fromNamespaceAndPath("mineboxadditions", "textures/icons/ah_alert.png");
    private static final int AH_ALERTER_BG = 0xC0603010;

    private static final Identifier HUD_EDITOR_ICON =
        Identifier.fromNamespaceAndPath("mineboxadditions", "textures/icons/hud_editor.png");
    private static final int HUD_EDITOR_BG = 0xC0203050;

    private static final Identifier SETTINGS_ICON =
        Identifier.fromNamespaceAndPath("mineboxadditions", "textures/icons/settings_cogwheel.png");
    private static final int SETTINGS_BG = 0xC0404040;

    private static final MenuEntry[] ENTRIES = {
        new MenuEntry(
            "items",
            "mineboxadditions.gui.menu.items",
            Identifier.fromNamespaceAndPath("minecraft", "textures/item/filled_map.png"),
            0xC0203060
        ),
        new MenuEntry(
            "bestiary",
            "mineboxadditions.gui.menu.bestiary",
            Identifier.fromNamespaceAndPath("minecraft", "textures/item/book.png"),
            0xC0602020
        ),
        new MenuEntry(
            "harvestables",
            "mineboxadditions.gui.menu.harvestables",
            Identifier.fromNamespaceAndPath("minecraft", "textures/item/wheat.png"),
            0xC0205020
        )
    };

    public MineboxMenuScreen() {
        super(Component.translatable("mineboxadditions.gui.menu.title", Utils.getModVersion()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
        super.extractRenderState(draw, mouseX, mouseY, delta);

        int fullH = 2 * CARD_SIZE + 2 * this.font.lineHeight + 56;
        int startY = (this.height - fullH) / 2 + 20;
        int totalW = ENTRIES.length * CARD_SIZE + (ENTRIES.length - 1) * CARD_GAP;
        int startX = (this.width - totalW) / 2;

        Component title = Component.translatable("mineboxadditions.gui.menu.title", Utils.getModVersion());
        int titleW = this.font.width(title);
        draw.text(this.font, title, (this.width - titleW) / 2, startY - 20, COL_TEXT, true);

        for (int i = 0; i < ENTRIES.length; i++) {
            MenuEntry entry = ENTRIES[i];
            int cx = startX + i * (CARD_SIZE + CARD_GAP);
            int cy = startY;

            boolean hovered = mouseX >= cx && mouseX < cx + CARD_SIZE
                           && mouseY >= cy && mouseY < cy + CARD_SIZE;
            int borderColor = hovered ? COL_BORDER_HOVER : COL_BORDER;

            fillRoundedRect(draw, cx, cy, CARD_SIZE, CARD_SIZE, CORNER_R, borderColor);
            fillRoundedRect(draw, cx + BORDER_W, cy + BORDER_W,
                CARD_SIZE - BORDER_W * 2, CARD_SIZE - BORDER_W * 2,
                CORNER_R - BORDER_W, entry.bgColor);

            int iconDisplaySize = (int) (ICON_SRC_SIZE * ICON_SCALE);
            int iconX = cx + (CARD_SIZE - iconDisplaySize) / 2;
            int iconY = cy + (CARD_SIZE - iconDisplaySize) / 2;
            Matrix3x2f backup = new Matrix3x2f(draw.pose());
            draw.pose().translate(iconX, iconY);
            draw.pose().scale(ICON_SCALE, ICON_SCALE);
            draw.blit(RenderPipelines.GUI_TEXTURED, entry.texture,
                0, 0, 0, 0, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE);
            draw.pose().set(backup);

            Component label = Component.translatable(entry.labelKey);
            int labelW = this.font.width(label);
            draw.text(this.font, label, cx + (CARD_SIZE - labelW) / 2, cy + CARD_SIZE + 8, COL_TEXT, true);
        }

        int secondRowW = 3 * CARD_SIZE + 2 * CARD_GAP;
        int secondRowX = (this.width - secondRowW) / 2;
        int secondRowY = startY + CARD_SIZE + 8 + this.font.lineHeight + 20;

        int iconDisplaySize = (int) (ICON_SRC_SIZE * ICON_SCALE);

        int ahAlerterX = secondRowX;
        boolean ahAlerterHovered = mouseX >= ahAlerterX && mouseX < ahAlerterX + CARD_SIZE
                                && mouseY >= secondRowY && mouseY < secondRowY + CARD_SIZE;
        fillRoundedRect(draw, ahAlerterX, secondRowY, CARD_SIZE, CARD_SIZE, CORNER_R,
            ahAlerterHovered ? COL_BORDER_HOVER : COL_BORDER);
        fillRoundedRect(draw, ahAlerterX + BORDER_W, secondRowY + BORDER_W,
            CARD_SIZE - BORDER_W * 2, CARD_SIZE - BORDER_W * 2, CORNER_R - BORDER_W, AH_ALERTER_BG);
        Matrix3x2f backup = new Matrix3x2f(draw.pose());
        draw.pose().translate(ahAlerterX + (CARD_SIZE - iconDisplaySize) / 2,
            secondRowY + (CARD_SIZE - iconDisplaySize) / 2);
        draw.pose().scale(ICON_SCALE, ICON_SCALE);
        draw.blit(RenderPipelines.GUI_TEXTURED, AH_ALERTER_ICON,
            0, 0, 0, 0, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE);
        draw.pose().set(backup);
        Component ahAlerterLabel = Component.translatable("mineboxadditions.gui.menu.ahlerter");
        int ahAlerterLabelW = this.font.width(ahAlerterLabel);
        draw.text(this.font, ahAlerterLabel, ahAlerterX + (CARD_SIZE - ahAlerterLabelW) / 2,
            secondRowY + CARD_SIZE + 8, COL_TEXT, true);

        int hudEditorX = secondRowX + CARD_SIZE + CARD_GAP;
        boolean hudEditorHovered = mouseX >= hudEditorX && mouseX < hudEditorX + CARD_SIZE
                                && mouseY >= secondRowY && mouseY < secondRowY + CARD_SIZE;
        fillRoundedRect(draw, hudEditorX, secondRowY, CARD_SIZE, CARD_SIZE, CORNER_R,
            hudEditorHovered ? COL_BORDER_HOVER : COL_BORDER);
        fillRoundedRect(draw, hudEditorX + BORDER_W, secondRowY + BORDER_W,
            CARD_SIZE - BORDER_W * 2, CARD_SIZE - BORDER_W * 2, CORNER_R - BORDER_W, HUD_EDITOR_BG);
        backup = new Matrix3x2f(draw.pose());
        draw.pose().translate(hudEditorX + (CARD_SIZE - iconDisplaySize) / 2,
            secondRowY + (CARD_SIZE - iconDisplaySize) / 2);
        draw.pose().scale(ICON_SCALE, ICON_SCALE);
        draw.blit(RenderPipelines.GUI_TEXTURED, HUD_EDITOR_ICON,
            0, 0, 0, 0, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE);
        draw.pose().set(backup);
        Component hudEditorLabel = Component.translatable("mineboxadditions.gui.menu.hud_editor");
        int hudEditorLabelW = this.font.width(hudEditorLabel);
        draw.text(this.font, hudEditorLabel, hudEditorX + (CARD_SIZE - hudEditorLabelW) / 2,
            secondRowY + CARD_SIZE + 8, COL_TEXT, true);

        int settingsX = secondRowX + 2 * (CARD_SIZE + CARD_GAP);
        boolean settingsHovered = mouseX >= settingsX && mouseX < settingsX + CARD_SIZE
                               && mouseY >= secondRowY && mouseY < secondRowY + CARD_SIZE;
        fillRoundedRect(draw, settingsX, secondRowY, CARD_SIZE, CARD_SIZE, CORNER_R,
            settingsHovered ? COL_BORDER_HOVER : COL_BORDER);
        fillRoundedRect(draw, settingsX + BORDER_W, secondRowY + BORDER_W,
            CARD_SIZE - BORDER_W * 2, CARD_SIZE - BORDER_W * 2, CORNER_R - BORDER_W, SETTINGS_BG);
        backup = new Matrix3x2f(draw.pose());
        draw.pose().translate(settingsX + (CARD_SIZE - iconDisplaySize) / 2,
            secondRowY + (CARD_SIZE - iconDisplaySize) / 2);
        draw.pose().scale(ICON_SCALE, ICON_SCALE);
        draw.blit(RenderPipelines.GUI_TEXTURED, SETTINGS_ICON,
            0, 0, 0, 0, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE);
        draw.pose().set(backup);
        Component settingsLabel = Component.translatable("mineboxadditions.gui.menu.settings");
        int settingsLabelW = this.font.width(settingsLabel);
        draw.text(this.font, settingsLabel, settingsX + (CARD_SIZE - settingsLabelW) / 2,
            secondRowY + CARD_SIZE + 8, COL_TEXT, true);
    }

    private static void fillRoundedRect(GuiGraphicsExtractor draw, int x, int y, int w, int h, int r, int color) {
        draw.fill(x + r, y, x + w - r, y + h, color);
        draw.fill(x, y + r, x + r, y + h - r, color);
        draw.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int hw = (int) Math.floor(Math.sqrt(Math.max(0.0, r * r - dy * dy)));
            int xStart = r - hw;
            draw.fill(x + xStart, y + i, x + r, y + i + 1, color);
            draw.fill(x + w - r, y + i, x + w - xStart, y + i + 1, color);
            draw.fill(x + xStart, y + h - i - 1, x + r, y + h - i, color);
            draw.fill(x + w - r, y + h - i - 1, x + w - xStart, y + h - i, color);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        int fullH = 2 * CARD_SIZE + 2 * this.font.lineHeight + 56;
        int startY = (this.height - fullH) / 2 + 20;
        int totalW = ENTRIES.length * CARD_SIZE + (ENTRIES.length - 1) * CARD_GAP;
        int startX = (this.width - totalW) / 2;

        for (int i = 0; i < ENTRIES.length; i++) {
            int cx = startX + i * (CARD_SIZE + CARD_GAP);
            int cy = startY;
            if (mx >= cx && mx < cx + CARD_SIZE && my >= cy && my < cy + CARD_SIZE) {
                onCardClick(ENTRIES[i].id);
                return true;
            }
        }

        int secondRowW = 3 * CARD_SIZE + 2 * CARD_GAP;
        int secondRowX = (this.width - secondRowW) / 2;
        int secondRowY = startY + CARD_SIZE + 8 + this.font.lineHeight + 20;

        int ahAlerterX = secondRowX;
        if (mx >= ahAlerterX && mx < ahAlerterX + CARD_SIZE && my >= secondRowY && my < secondRowY + CARD_SIZE) {
            this.minecraft.gui.setScreen(new AhAlerterScreen());
            return true;
        }

        int hudEditorX = secondRowX + CARD_SIZE + CARD_GAP;
        if (mx >= hudEditorX && mx < hudEditorX + CARD_SIZE && my >= secondRowY && my < secondRowY + CARD_SIZE) {
            this.minecraft.gui.setScreen(new HudEditorScreen());
            return true;
        }

        int settingsX = secondRowX + 2 * (CARD_SIZE + CARD_GAP);
        if (mx >= settingsX && mx < settingsX + CARD_SIZE && my >= secondRowY && my < secondRowY + CARD_SIZE) {
            this.minecraft.gui.setScreen(ResourcefulConfigScreen.make(ConfigManager.configurator, Config.class).build());
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void onCardClick(String id) {
        switch (id) {
            case "items" -> {
                if (MineboxAdditions.INSTANCE.state.getMbxItems() == null
                        || MineboxAdditions.INSTANCE.state.getMbxItems().isEmpty()) {
                    Utils.displayChatErrorMessage(
                        Component.translatable("mineboxadditions.strings.errors.missing_atlas_data").getString()
                    );
                    return;
                }
                this.minecraft.gui.setScreen(new MineboxAtlasScreen());
            }
            case "bestiary" -> {
                if (MineboxAdditions.INSTANCE.state.getMbxBestiary() == null
                        || MineboxAdditions.INSTANCE.state.getMbxBestiary().isEmpty()) {
                    Utils.displayChatErrorMessage(
                        Component.translatable("mineboxadditions.strings.errors.missing_bestiary_data").getString()
                    );
                    return;
                }
                this.minecraft.gui.setScreen(new BestiaryScreen());
            }
            case "harvestables" -> this.minecraft.gui.setScreen(new HarvestableScreen());
        }
    }

    public boolean shouldPause() {
        return false;
    }

    private record MenuEntry(String id, String labelKey, Identifier texture, int bgColor) {}
}
