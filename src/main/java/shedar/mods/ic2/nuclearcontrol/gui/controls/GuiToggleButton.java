package shedar.mods.ic2.nuclearcontrol.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class GuiToggleButton extends GuiButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "nuclearcontrol:textures/gui/GUIAdvancedInfoPanelLinesButtons.png");

    private static final int ICON_HEIGHT = 16;
    final byte slot;
    private final PanelSetting setting;
    private boolean isChecked;
    int dragOffsetY = 0; // Stores how much offset from mouse click
    private TileEntityAdvancedInfoPanel panel;
    private String fullTitle;

    public GuiToggleButton(int id, int x, int y, String title, PanelSetting setting, TileEntityAdvancedInfoPanel panel,
            byte slot) {
        super(id, x, y, GuiScrollableList.BUTTON_WIDTH, GuiScrollableList.BUTTON_HEIGHT, title);
        this.setting = setting;
        this.panel = panel;
        this.slot = slot;
        this.fullTitle = title;
        // Ensure the title fits within the button
        int maxWidth = GuiScrollableList.BUTTON_WIDTH - GuiScrollableList.TOGGLE_BUTTON_WIDTH
                - GuiScrollableList.PADDING_RIGHT;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        if (fontRenderer.getStringWidth(title) > maxWidth) {
            this.displayString = fontRenderer.trimStringToWidth(title, maxWidth - fontRenderer.getStringWidth("..."))
                    + "...";
        } else {
            this.displayString = title;
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggle() {
        isChecked = !isChecked;
    }

    public void setPosition(int y) {
        this.yPosition = y;
    }

    public String getFullTitle() {
        return fullTitle;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        isChecked = panel.getNewDisplaySettingsForCardInSlot(slot).getSetting(setting.displayBit);
        mc.getTextureManager().bindTexture(TEXTURE);
        GL11.glColor4f(1, 1, 1, 1);

        boolean hovered = mouseX >= xPosition && mouseX < xPosition + width
                && mouseY >= yPosition
                && mouseY < yPosition + height;

        int textureY = hovered ? GuiScrollableList.BUTTON_HEIGHT : 0;
        drawTexturedModalRect(xPosition - 1, yPosition + 1, 0, textureY, 140, 20);

        int iconX = 0;
        int iconY = isChecked ? (GuiScrollableList.BUTTON_HEIGHT * 2)
                : ICON_HEIGHT + (GuiScrollableList.BUTTON_HEIGHT * 2) - 1;
        drawTexturedModalRect(xPosition + 1, yPosition + 3, iconX, iconY, 16, 15);

        mc.fontRenderer.drawString(displayString, xPosition + 24, yPosition + 6, 0xFFFFFF);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (mouseX < xPosition + GuiScrollableList.TOGGLE_BUTTON_WIDTH) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                toggle();
                DisplaySettingHelper settings = panel.getNewDisplaySettingsForCardInSlot(slot);
                settings.toggleSetting(setting.displayBit);
                NuclearNetworkHelper.setDisplaySettings(panel, slot, settings);
                panel.setDisplaySettings(slot, settings);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
