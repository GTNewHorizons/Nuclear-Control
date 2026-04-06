package shedar.mods.ic2.nuclearcontrol.renderers;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;

import org.lwjgl.opengl.GL11;

import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.panel.Screen;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;

public class TileEntityInfoPanelRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        if (!(tileEntity instanceof TileEntityInfoPanel panel)) return;
        if (!panel.getPowered()) return;
        renderPanelTileEntity(panel, x, y, z);
    }

    private void renderPanelTileEntity(TileEntityInfoPanel panel, double x, double y,
            double z) {
        GL11.glPushMatrix();
        GL11.glPolygonOffset(-10, -10);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        short side = (short) Facing.oppositeSide[panel.getFacing()];
        Screen screen = panel.getScreen();
        float dx = 1F / 16;
        float dz = 1F / 16;
        float displayWidth = 1 - 2F / 16;
        float displayHeight = 1 - 2F / 16;
        if (screen != null) {
            y -= panel.yCoord - screen.maxY;
            if (side == 0 || side == 1 || side == 2 || side == 3 || side == 5) {
                z -= panel.zCoord - screen.minZ;
            } else {
                z -= panel.zCoord - screen.maxZ;
            }

            if (side == 0 || side == 2 || side == 4) {
                x -= panel.xCoord - screen.minX;
            } else {
                x -= panel.xCoord - screen.maxX;
            }
        }
        GL11.glTranslatef((float) x, (float) y, (float) z);
        switch (side) {
            case 0:
                if (screen != null) {
                    displayWidth += screen.maxX - screen.minX;
                    displayHeight += screen.maxZ - screen.minZ;
                }
                break;
            case 1:
                GL11.glTranslatef(1, 1, 0);
                GL11.glRotatef(180, 1, 0, 0);
                GL11.glRotatef(180, 0, 1, 0);
                if (screen != null) {
                    displayWidth += screen.maxX - screen.minX;
                    displayHeight += screen.maxZ - screen.minZ;
                }
                break;
            case 2:
                GL11.glTranslatef(0, 1, 0);
                GL11.glRotatef(0, 0, 1, 0);
                GL11.glRotatef(90, 1, 0, 0);
                if (screen != null) {
                    displayWidth += screen.maxX - screen.minX;
                    displayHeight += screen.maxY - screen.minY;
                }
                break;
            case 3:
                GL11.glTranslatef(1, 1, 1);
                GL11.glRotatef(180, 0, 1, 0);
                GL11.glRotatef(90, 1, 0, 0);
                if (screen != null) {
                    displayWidth += screen.maxX - screen.minX;
                    displayHeight += screen.maxY - screen.minY;
                }
                break;
            case 4:
                GL11.glTranslatef(0, 1, 1);
                GL11.glRotatef(90, 0, 1, 0);
                GL11.glRotatef(90, 1, 0, 0);
                if (screen != null) {
                    displayWidth += screen.maxZ - screen.minZ;
                    displayHeight += screen.maxY - screen.minY;
                }
                break;
            case 5:
                GL11.glTranslatef(1, 1, 0);
                GL11.glRotatef(-90, 0, 1, 0);
                GL11.glRotatef(90, 1, 0, 0);
                if (screen != null) {
                    displayWidth += screen.maxZ - screen.minZ;
                    displayHeight += screen.maxY - screen.minY;
                }
                break;
        }
        float thickness = 1;
        double angleHor = 0;
        double angleVert = 0;
        double[] deltas = null;
        if (panel instanceof TileEntityAdvancedInfoPanel && screen != null) {
            TileEntityAdvancedInfoPanel advPanel = (TileEntityAdvancedInfoPanel) panel;
            deltas = advPanel.screenModelInfo.getDeltas();
            thickness = (float) (advPanel.thickness / 16F - (deltas[0] + deltas[1] + deltas[2] + deltas[3]) / 4F + 0.015);
        }

        GL11.glTranslatef(dx + displayWidth / 2, thickness, dz + displayHeight / 2);
        GL11.glRotatef(-90, 1, 0, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        switch (panel.rotation) {
            case 0:
                break;
            case 1:
                GL11.glRotatef(-90, 0, 0, 1);
                float t = displayHeight;
                displayHeight = displayWidth;
                displayWidth = t;
                break;
            case 2:
                GL11.glRotatef(90, 0, 0, 1);
                float tm = displayHeight;
                displayHeight = displayWidth;
                displayWidth = tm;
                break;
            case 3:
                GL11.glRotatef(180, 0, 0, 1);
                break;
        }
        if (deltas != null) {
            if (deltas[0] == 0) {
                // +1,-2
                angleHor = 180 / Math.PI * Math.atan(deltas[1] / (displayWidth + 2F / 16));
                angleVert = -180 / Math.PI * Math.atan(deltas[2] / (displayHeight + 2F / 16));
            } else if (deltas[1] == 0) {
                // -0,-3
                angleHor = -180 / Math.PI * Math.atan(deltas[0] / (displayWidth + 2F / 16));
                angleVert = -180 / Math.PI * Math.atan(deltas[3] / (displayHeight + 2F / 16));
            } else if (deltas[2] == 0) {
                // +3,+0
                angleHor = 180 / Math.PI * Math.atan(deltas[3] / (displayWidth + 2F / 16));
                angleVert = 180 / Math.PI * Math.atan(deltas[0] / (displayHeight + 2F / 16));
            } else {
                // -2,+1
                angleHor = -180 / Math.PI * Math.atan(deltas[2] / (displayWidth + 2F / 16));
                angleVert = 180 / Math.PI * Math.atan(deltas[1] / (displayHeight + 2F / 16));
            }
        }
        GL11.glRotatef((float) -angleVert, 1, 0, 0);
        GL11.glRotatef((float) angleHor, 0, 1, 0);
        // Do text rotation here
        GL11.glRotatef((float) panel.getTextRotation() * 90.0f, 0, 0, 1);
        FontRenderer fontRenderer = this.func_147498_b();

        Integer maxWidth = panel.cardCache.getTextWidth();
        List<PanelString> displayData = panel.getDisplayedData();

        if (maxWidth == null) {
            maxWidth = 1;
            StringBuilder widthSb = new StringBuilder();
            for (PanelString panelString : displayData) {
                widthSb.setLength(0);
                if (panelString.textLeft != null && !panelString.textLeft.isEmpty()) widthSb.append(panelString.textLeft);
                if (panelString.textCenter != null && !panelString.textCenter.isEmpty()) {
                    if (widthSb.length() > 0) widthSb.append(' ');
                    widthSb.append(panelString.textCenter);
                }
                if (panelString.textRight != null && !panelString.textRight.isEmpty()) {
                    if (widthSb.length() > 0) widthSb.append(' ');
                    widthSb.append(panelString.textRight);
                }
                maxWidth = Math.max(fontRenderer.getStringWidth(widthSb.toString()), maxWidth);
            }
            maxWidth += 4;
            panel.cardCache.setTextWidth(maxWidth);
        }

        int lineHeight = fontRenderer.FONT_HEIGHT + 2;
        int requiredHeight = lineHeight * displayData.size();
        if (panel.getTextRotation() == 1 || panel.getTextRotation() == 3) {
            float tm = displayWidth;
            displayWidth = displayHeight;
            displayHeight = tm;
        }
        float scaleX = displayWidth / maxWidth;
        float scaleY = displayHeight / requiredHeight;
        float scale = Math.min(scaleX, scaleY);
        GL11.glScalef(scale, -scale, scale);

        int offsetX;
        int offsetY;

        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);

        if (scaleX < scaleY) {
            offsetX = 2;
            offsetY = (realHeight - requiredHeight) / 2;
        } else {
            offsetX = (realWidth - maxWidth) / 2 + 2;
            offsetY = 0;
        }

        GL11.glDisable(GL11.GL_LIGHTING);

        int row = 0;
        for (PanelString panelString : displayData) {
            if (panelString.textLeft != null) {
                fontRenderer.drawString(
                        panelString.textLeft,
                        offsetX - realWidth / 2,
                        1 + offsetY - realHeight / 2 + row * lineHeight,
                        panelString.colorLeft != 0 ? panelString.colorLeft : panel.getColorTextHex());
            }
            if (panelString.textCenter != null) {
                fontRenderer.drawString(
                        panelString.textCenter,
                        -fontRenderer.getStringWidth(panelString.textCenter) / 2,
                        offsetY - realHeight / 2 + row * lineHeight,
                        panelString.colorCenter != 0 ? panelString.colorCenter : panel.getColorTextHex());
            }
            if (panelString.textRight != null) {
                fontRenderer.drawString(
                        panelString.textRight,
                        realWidth / 2 - fontRenderer.getStringWidth(panelString.textRight),
                        offsetY - realHeight / 2 + row * lineHeight,
                        panelString.colorRight != 0 ? panelString.colorRight : panel.getColorTextHex());
            }
            row++;
        }

        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPopMatrix();
    }
}
