package shedar.mods.ic2.nuclearcontrol.gui.controls;

import static shedar.mods.ic2.nuclearcontrol.gui.controls.GuiScrollableList.BUTTON_WIDTH;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class SmallGuiButton extends GuiButton {

    private Runnable onClick;

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "nuclearcontrol:textures/gui/GUIAdvancedInfoPanelLinesButtons.png");

    public SmallGuiButton(int id, int x, int y, int width, int height, String displayString, Runnable onClick) {
        super(id, x, y, width, height, displayString);
        this.onClick = onClick;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);
        GL11.glColor4f(1, 1, 1, 1);

        boolean hovered = mouseX >= xPosition && mouseX < xPosition + width
                && mouseY >= yPosition
                && mouseY < yPosition + height;

        int textureY = hovered ? height : 0;
        int textureX = BUTTON_WIDTH;
        if (width == 48) {
            textureX += width + 1;
        }
        drawTexturedModalRect(xPosition - 1, yPosition, textureX, textureY, width, height);

        mc.fontRenderer.drawString(displayString, xPosition + 3, yPosition + 3, 0xFFFFFF);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (mouseX < xPosition + width) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                onClick.run();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
