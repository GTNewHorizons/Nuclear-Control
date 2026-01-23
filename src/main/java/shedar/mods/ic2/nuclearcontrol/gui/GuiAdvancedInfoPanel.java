package shedar.mods.ic2.nuclearcontrol.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.core.IC2;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.api.IAdvancedCardSettings;
import shedar.mods.ic2.nuclearcontrol.api.ICardGui;
import shedar.mods.ic2.nuclearcontrol.api.ICardSettingsWrapper;
import shedar.mods.ic2.nuclearcontrol.api.ICardWrapper;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.IPanelMultiCard;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.gui.controls.GuiScrollableList;
import shedar.mods.ic2.nuclearcontrol.gui.controls.IconButton;
import shedar.mods.ic2.nuclearcontrol.panel.CardSettingsWrapperImpl;
import shedar.mods.ic2.nuclearcontrol.panel.CardWrapperImpl;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;

@SideOnly(Side.CLIENT)
public class GuiAdvancedInfoPanel extends GuiInfoPanel {

    private static final String TEXTURE_FILE = "nuclearcontrol:textures/gui/GUIAdvancedInfoPanel.png";
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(TEXTURE_FILE);

    private static final int ID_LABELS = 1;
    private static final int ID_SLOPE = 2;
    private static final int ID_COLORS = 3;
    private static final int ID_POWER = 4;
    private static final int ID_SETTINGS = 5;
    private static final int ID_TRANSPARENCY = 6;
    private static final int ID_ROTATELEFT = 7;
    private static final int ID_ROTATERIGHT = 8;
    private static final int ID_LINES = 9;

    private static final int HOVER_DELAY = 5;

    private byte activeTab;
    private boolean initialized;
    private boolean willReturn = false;
    private int hoverDelayLeft = HOVER_DELAY;
    private int previousButtonX = -1;
    private int previousButtonY = -1;

    public GuiAdvancedInfoPanel(Container container) {
        super(container);
        ySize = 228;
        activeTab = 0;
        initialized = false;
        name = StatCollector.translateToLocal("gui.tile.blockAdvancedInfoPanel.name.title");
        isColored = this.container.panel.getColored();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(TEXTURE_LOCATION);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
        drawTexturedModalRect(left + 24, top + 62 + activeTab * 14, 182, 0, 1, 15);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (mouseX > guiLeft + 80 + 18 && mouseX < guiLeft + 80 + 18 * 5
                && mouseY > guiTop + 42
                && mouseY < guiTop + 42 + 18 * 2) {
            int buttonX = (mouseX - guiLeft - 79) / 18 - 1;
            int buttonY = (mouseY - guiTop - 41) / 18;
            if (buttonX == previousButtonX && buttonY == previousButtonY) {
                if (hoverDelayLeft <= 0) {
                    drawButtonTooltip(mouseX, mouseY, buttonX, buttonY);
                } else {
                    hoverDelayLeft--;
                }
            } else {
                hoverDelayLeft = HOVER_DELAY;
                previousButtonX = buttonX;
                previousButtonY = buttonY;
            }
        } else if (mouseX > guiLeft + 32 && mouseX < guiLeft + 64
                && mouseY > guiTop + 80
                && mouseY < guiTop + 80 + 16
                && getActiveCard() != null) {
                    if (previousButtonX == -2 && previousButtonY == -2) {
                        if (hoverDelayLeft <= 0) {
                            List<String> list = new ArrayList<>();
                            list.add(StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.LineConfig"));
                            drawTooltip(mc, mouseX, mouseY, list);
                        } else {
                            hoverDelayLeft--;
                        }
                    } else {
                        hoverDelayLeft = HOVER_DELAY;
                        previousButtonX = -2;
                        previousButtonY = -2;
                    }
                } else {
                    hoverDelayLeft = HOVER_DELAY;
                    previousButtonX = -1;
                    previousButtonY = -1;
                }
    }

    private void drawButtonTooltip(int mouseX, int mouseY, int buttonX, int buttonY) {
        String tooltipText = "";
        if (buttonY == 0) {
            if (buttonX == 0) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.RotateRight");
            } else if (buttonX == 1) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Colors");
            } else if (buttonX == 2) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Power");
            } else if (buttonX == 3) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Settings");
            }
        } else if (buttonY == 1) {
            if (buttonX == 0) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.RotateLeft");
            } else if (buttonX == 1) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Labels");
            } else if (buttonX == 2) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Slope");
            } else if (buttonX == 3) {
                tooltipText = StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Transparency");
            }
        }
        List<String> list = new ArrayList<>();
        list.add(tooltipText);
        drawTooltip(mc, mouseX, mouseY, list);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initControls() {
        ItemStack card = getActiveCard();
        if (((card == null && prevCard == null && initialized) || (card != null && card.equals(prevCard)))
                && !willReturn)
            return;
        willReturn = false;
        initialized = true;
        int h = fontRendererObj.FONT_HEIGHT + 1;
        buttonList.clear();
        prevCard = card;

        // labels
        buttonList.add(
                new IconButton(
                        ID_LABELS,
                        guiLeft + 80 + 18 * 2,
                        guiTop + 42 + 18,
                        16,
                        16,
                        TEXTURE_LOCATION,
                        192 - 16,
                        getIconLabelsTopOffset(container.panel.getShowLabels())));
        // slope
        buttonList.add(
                new IconButton(ID_SLOPE, guiLeft + 80 + 18 * 3, guiTop + 42 + 18, 16, 16, TEXTURE_LOCATION, 192, 15));
        // colors
        buttonList.add(
                new IconButton(ID_COLORS, guiLeft + 80 + 18 * 2, guiTop + 42, 16, 16, TEXTURE_LOCATION, 192, 15 + 16));
        // power
        buttonList.add(
                new IconButton(
                        ID_POWER,
                        guiLeft + 80 + 18 * 3,
                        guiTop + 42,
                        16,
                        16,
                        TEXTURE_LOCATION,
                        192 - 16,
                        getIconPowerTopOffset(((TileEntityAdvancedInfoPanel) container.panel).getPowerMode())));
        // transparency
        buttonList.add(
                new IconButton(
                        ID_TRANSPARENCY,
                        guiLeft + 80 + 18 * 4,
                        guiTop + 42 + 18,
                        16,
                        16,
                        TEXTURE_LOCATION,
                        192,
                        15 + 48));
        // rotate left
        buttonList.add(
                new IconButton(
                        ID_ROTATELEFT,
                        guiLeft + 80 + 18 * 1,
                        guiTop + 42,
                        16,
                        16,
                        TEXTURE_LOCATION,
                        192,
                        15 + 64));
        // rotate right
        buttonList.add(
                new IconButton(
                        ID_ROTATERIGHT,
                        guiLeft + 80 + 18 * 1,
                        guiTop + 42 + 18,
                        16,
                        16,
                        TEXTURE_LOCATION,
                        192,
                        15 + 80));

        if (card != null && card.getItem() instanceof IPanelDataSource) {
            byte slot = container.panel.getIndexOfCard(card);
            IPanelDataSource source = (IPanelDataSource) card.getItem();
            if (source instanceof IAdvancedCardSettings) {
                // settings
                buttonList.add(
                        new IconButton(
                                ID_SETTINGS,
                                guiLeft + 80 + 18 * 4,
                                guiTop + 42,
                                16,
                                16,
                                TEXTURE_LOCATION,
                                192,
                                15 + 16 * 2));
            }
            List<PanelSetting> settingsList;
            if (card.getItem() instanceof IPanelMultiCard) {
                settingsList = ((IPanelMultiCard) source).getSettingsList(new CardWrapperImpl(card, activeTab));
            } else {
                settingsList = source.getSettingsList();
            }
            if (settingsList != null) {
                if (((IPanelDataSource) card.getItem()).getSettingsList() != null) {
                    buttonList.add(
                            new IconButton(
                                    ID_LINES,
                                    guiLeft + 32,
                                    guiTop + 80,
                                    32,
                                    16,
                                    TEXTURE_LOCATION,
                                    192 - 16,
                                    111));
                }
            }
            if (!modified) {
                textboxTitle = new GuiTextField(fontRendererObj, 7, 16, 162, 18);
                textboxTitle.setFocused(true);
                textboxTitle.setText(new CardWrapperImpl(card, activeTab).getTitle());
            }
        } else {
            modified = false;
            textboxTitle = null;
        }
    }

    /**
     * draw a tooltip at the given location with the given text.
     *
     * @param mc        Minecraft instance.
     * @param mouseX    X location of the mouse
     * @param mouseY    Y location of the mouse
     * @param textLines lines of text to draw
     */
    private void drawTooltip(Minecraft mc, int mouseX, int mouseY, List<String> textLines) {
        drawHoveringText(textLines, mouseX, mouseY, mc.fontRenderer);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    protected ItemStack getActiveCard() {
        return container.panel.getCards().get(activeTab);
    }

    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3) {
        initialized = false;
        super.setWorldAndResolution(par1Minecraft, par2, par3);
    }

    private int getIconLabelsTopOffset(boolean checked) {
        return checked ? 15 : 31;
    }

    private int getIconPowerTopOffset(byte mode) {
        switch (mode) {
            case TileEntityAdvancedInfoPanel.POWER_REDSTONE:
                return 15 + 16 * 2;
            case TileEntityAdvancedInfoPanel.POWER_INVERTED:
                return 15 + 16 * 3;
            case TileEntityAdvancedInfoPanel.POWER_ON:
                return 15 + 16 * 4;
            case TileEntityAdvancedInfoPanel.POWER_OFF:
                return 15 + 16 * 5;
        }
        return 15 + 16 * 2;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case ID_COLORS -> {
                GuiScreen colorGui = new GuiScreenColor(this, container.panel);
                willReturn = true;
                mc.displayGuiScreen(colorGui);

            }
            case ID_SETTINGS -> {
                ItemStack card = getActiveCard();
                if (card == null) return;
                if (card.getItem() instanceof IAdvancedCardSettings) {
                    ICardWrapper helper = new CardWrapperImpl(card, activeTab);
                    Object guiObject = ((IAdvancedCardSettings) card.getItem()).getSettingsScreen(helper);
                    if (!(guiObject instanceof GuiScreen gui)) {
                        IC2NuclearControl.logger
                                .warn("Invalid card, getSettingsScreen method should return GuiScreen object");
                        return;
                    }
                    ICardSettingsWrapper wrapper = new CardSettingsWrapperImpl(card, container.panel, this, activeTab);
                    ((ICardGui) gui).setCardSettingsHelper(wrapper);
                    willReturn = true;
                    mc.displayGuiScreen(gui);
                }

            }
            case ID_LABELS -> {
                boolean checked = !container.panel.getShowLabels();
                if (button instanceof IconButton) {
                    IconButton iButton = (IconButton) button;
                    iButton.textureTop = getIconLabelsTopOffset(checked);
                }
                int value = checked ? -1 : -2;
                container.panel.setShowLabels(checked);
                IC2.network.get().initiateClientTileEntityEvent(container.panel, value);
                willReturn = true;
            }
            case ID_POWER -> {
                byte mode = ((TileEntityAdvancedInfoPanel) container.panel).getNextPowerMode();
                if (button instanceof IconButton) {
                    IconButton iButton = (IconButton) button;
                    iButton.textureTop = getIconPowerTopOffset(mode);
                }
                IC2.network.get().initiateClientTileEntityEvent(container.panel, mode);
            }
            case ID_SLOPE -> {
                GuiPanelSlope slopeGui = new GuiPanelSlope(this, (TileEntityAdvancedInfoPanel) container.panel);
                willReturn = true;
                mc.displayGuiScreen(slopeGui);
            }
            case ID_TRANSPARENCY -> {
                IC2.network.get().initiateClientTileEntityEvent(container.panel, ID_TRANSPARENCY);
            }
            case ID_ROTATELEFT -> {
                IC2.network.get().initiateClientTileEntityEvent(container.panel, ID_ROTATELEFT);
            }
            case ID_ROTATERIGHT -> {
                IC2.network.get().initiateClientTileEntityEvent(container.panel, ID_ROTATERIGHT);
            }
            case ID_LINES -> {
                ItemStack card = getActiveCard();
                if (((IPanelDataSource) card.getItem()).getSettingsList() != null) {
                    GuiScrollableList listGui = new GuiScrollableList(
                            this,
                            (TileEntityAdvancedInfoPanel) container.panel,
                            card);
                    willReturn = true;
                    mc.displayGuiScreen(listGui);
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int par3) {
        super.mouseClicked(x, y, par3);
        if (x >= guiLeft + 7 && x <= guiLeft + 24 && y >= guiTop + 62 && y <= guiTop + 104) {
            byte newTab = (byte) ((y - guiTop - 62) / 14);
            if (newTab > 2) newTab = 2;
            if (newTab != activeTab && modified) updateTitle();
            activeTab = newTab;
        }
    }
}
