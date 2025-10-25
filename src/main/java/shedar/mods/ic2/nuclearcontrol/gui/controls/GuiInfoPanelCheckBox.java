package shedar.mods.ic2.nuclearcontrol.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

@SideOnly(Side.CLIENT)
public class GuiInfoPanelCheckBox extends GuiButton {

    private static final String TEXTURE_FILE = "nuclearcontrol:textures/gui/GUIInfoPanel.png";
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(TEXTURE_FILE);

    private TileEntityInfoPanel panel;
    private boolean checked;
    private PanelSetting setting;
    private byte slot;

    public GuiInfoPanelCheckBox(int id, int x, int y, PanelSetting setting, TileEntityInfoPanel panel, byte slot,
            FontRenderer renderer) {
        super(id, x, y, 0, 0, id < 10 ? LangHelper.translate("0" + id) : LangHelper.translate(String.valueOf(id)));
        this.setting = setting;
        this.slot = slot;
        height = renderer.FONT_HEIGHT + 1;
        width = renderer.getStringWidth(setting.title) + 8;
        this.panel = panel;
        this.checked = panel.getNewDisplaySettingsForCardInSlot(slot).getSetting(setting.displayBit);

    }

    @Override
    public void drawButton(Minecraft minecraft, int par2, int par3) {
        if (this.visible) {
            minecraft.renderEngine.bindTexture(TEXTURE_LOCATION);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            int delta = checked ? 6 : 0;
            drawTexturedModalRect(xPosition, yPosition + 1, 176, delta, 6, 6);
            minecraft.fontRenderer.drawString(displayString, xPosition + 8, yPosition, 0x404040);
        }
    }

    @Override
    public int getHoverState(boolean flag) {
        return 0;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (super.mousePressed(minecraft, mouseX, mouseY)) {
            checked = !checked;
            DisplaySettingHelper settings = panel.getNewDisplaySettingsForCardInSlot(slot);
            settings.toggleSetting(setting.displayBit);
            NuclearNetworkHelper.setDisplaySettings(panel, slot, settings);
            panel.setDisplaySettings(slot, settings);
            return true;
        } else return false;
    }

}
