package shedar.mods.ic2.nuclearcontrol.gui.controls;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.IPanelMultiCard;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.gui.GuiAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.panel.CardWrapperImpl;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.DataSorter;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class GuiScrollableList extends GuiScreen {

    // WARNING: These values are only meant to be edited when the texture is edited as well, you can find the texture
    // location at the TEXTURE variable further below.
    // General dimensions and paddings
    static final int GUI_WIDTH = 173;
    static final int GUI_HEIGHT = 233;
    private static final int PADDING_LEFT = 6;
    private static final int PADDING_TOP = 6;
    private static final int PADDING_BOTTOM = 7;
    static final int PADDING_MIDDLE = 7;
    static final int PADDING_RIGHT = 7;

    // Internal sizes
    static final int THUMB_WIDTH = 12;
    private static final int THUMB_HEIGHT = 32;

    // Button sizes and amounts, some of these can be calculated from earlier values
    static final int BUTTON_HEIGHT = 20;
    static final int BUTTON_WIDTH = 140;
    static final int FUNCTION_BUTTON_HEIGHT = 15;
    static final int FUNCTION_BUTTON_WIDTH = 49;
    static final int FUNCTION_BUTTON_PADDING = 7;
    private static final int LIST_HEIGHT = GUI_HEIGHT - PADDING_TOP
            - PADDING_BOTTOM
            - FUNCTION_BUTTON_HEIGHT
            - PADDING_BOTTOM
            - 1;
    private static final int VISIBLE_BUTTONS = LIST_HEIGHT / BUTTON_HEIGHT + 1;

    static final int TOGGLE_BUTTON_WIDTH = 20;

    private static final int SCROLL_SPEED = 1;

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(
            "nuclearcontrol:textures/gui/GUIAdvancedInfoPanelLinesBackground.png");
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(
            "nuclearcontrol:textures/gui/GUIAdvancedInfoPanelLinesButtons.png");
    private static final int HOVER_DELAY = 5;

    private int guiLeft = 0;
    private int guiTop = 0;
    private int guiRight = 0;
    private int guiBottom = 0;
    int internalLeft = 0;
    private int internalTop = 0;
    private int listRight = 0;
    private int scrollbarLeft = 0;
    private int scrollbarRight = 0;
    int internalBottom = 0;

    private List<GuiToggleButton> buttonListFull = new ArrayList<>();
    private List<GuiToggleButton> originalButtonList = new ArrayList<>();
    private List<GuiToggleButton> visibleButtonList = new ArrayList<>();
    private List<SmallGuiButton> functionButtons = new ArrayList<>();
    private final GuiAdvancedInfoPanel parentGui;

    private int scrollOffset = 0;
    private int thumbLocation = 0;
    private int scrollbarOffset = -1;
    private int hoverDelayLeft = HOVER_DELAY;
    private int previouslyHoveredButton = -1;

    private GuiToggleButton draggedButton = null;
    private int originalIndex = -1;
    private final TileEntityAdvancedInfoPanel panel;
    private final ItemStack card;
    private final byte cardSlot;
    private boolean dataSorterChanged = false;
    private DataSorter newDataSorter = null;
    private DataSorter originalDataSorter = null;
    private DisplaySettingHelper originalDisplaySettingHelper = null;

    /**
     * Constructor for the Scrollable List GUI.
     *
     * @param parentGui the GUI that should be opened when the esc key is pressed.
     * @param panel     the TileEntityAdvancedInfoPanel this is shown in
     * @param card      the specific card ItemStack
     */
    public GuiScrollableList(GuiAdvancedInfoPanel parentGui, TileEntityAdvancedInfoPanel panel, ItemStack card) {
        this.parentGui = parentGui;
        this.panel = panel;
        this.card = card;
        cardSlot = panel.getIndexOfCard(card);
    }

    @Override
    public void initGui() {
        guiLeft = (width - GUI_WIDTH) / 2;
        guiTop = (height - GUI_HEIGHT) / 2;
        guiRight = (width + GUI_WIDTH) / 2;
        guiBottom = (height + GUI_HEIGHT) / 2;
        internalLeft = guiLeft + PADDING_LEFT + 1;
        internalTop = guiTop + PADDING_TOP + 1;
        internalBottom = guiBottom - PADDING_BOTTOM;
        listRight = internalLeft + BUTTON_WIDTH;
        scrollbarLeft = guiRight - THUMB_WIDTH - PADDING_RIGHT;
        scrollbarRight = scrollbarLeft + THUMB_WIDTH;
        thumbLocation = internalTop;

        newDataSorter = panel.getDataSorter(cardSlot);
        originalDataSorter = new DataSorter(panel.getDataSorter(cardSlot).getArray());

        originalDisplaySettingHelper = new DisplaySettingHelper(panel.getNewDisplaySettingsForCardInSlot(cardSlot));

        this.buttonListFull.clear();
        this.buttonList.clear();
        this.visibleButtonList.clear();

        functionButtons.add(
                new SmallGuiButton(
                        0,
                        internalLeft + 1,
                        internalBottom - FUNCTION_BUTTON_HEIGHT,
                        FUNCTION_BUTTON_WIDTH,
                        FUNCTION_BUTTON_HEIGHT,
                        StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Save"),
                        this::onSave));
        functionButtons.add(
                new SmallGuiButton(
                        1,
                        internalLeft + 1 + FUNCTION_BUTTON_WIDTH + FUNCTION_BUTTON_PADDING,
                        internalBottom - FUNCTION_BUTTON_HEIGHT,
                        FUNCTION_BUTTON_WIDTH - 1,
                        FUNCTION_BUTTON_HEIGHT,
                        StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Reset"),
                        this::onReset));
        functionButtons.add(
                new SmallGuiButton(
                        2,
                        internalLeft + (FUNCTION_BUTTON_WIDTH + FUNCTION_BUTTON_PADDING) * 2,
                        internalBottom - FUNCTION_BUTTON_HEIGHT,
                        FUNCTION_BUTTON_WIDTH,
                        FUNCTION_BUTTON_HEIGHT,
                        StatCollector.translateToLocal("tile.blockAdvancedInfoPanel.Cancel"),
                        this::onCancel));

        IPanelDataSource source = (IPanelDataSource) card.getItem();
        List<PanelSetting> settingsList;
        if (card.getItem() instanceof IPanelMultiCard) {
            settingsList = ((IPanelMultiCard) source).getSettingsList(new CardWrapperImpl(card, (byte) 0));
        } else {
            settingsList = source.getSettingsList();
        }
        for (int i = 0; i < settingsList.size(); i++) {
            buttonListFull.add(
                    new GuiToggleButton(
                            i + 3,
                            internalLeft + 1,
                            0,
                            settingsList.get(i).title,
                            settingsList.get(i),
                            panel,
                            cardSlot));
        }
        originalButtonList = new ArrayList<>(buttonListFull);
        panel.getDataSorter(cardSlot).sortList(buttonListFull);
        updateVisibleButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);

        // The background texture should always be in the top left of the png
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        // the scrollbar should be directly to the right of the background texture
        drawTexturedModalRect(scrollbarLeft, thumbLocation, GUI_WIDTH, 0, THUMB_WIDTH, THUMB_HEIGHT);

        mc.getTextureManager().bindTexture(BUTTON_TEXTURE);

        for (SmallGuiButton functionButton : functionButtons) {
            functionButton.drawButton(mc, mouseX, mouseY);
        }

        // Cut off any buttons being drawn outside the list.
        // (technically redundant, but if one were to want to display half of a button, that is possible)
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        setScissor(internalLeft, internalTop, BUTTON_WIDTH + PADDING_MIDDLE + THUMB_WIDTH + PADDING_RIGHT, LIST_HEIGHT);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // if we're dragging a button, show it at the mouse position
        if (draggedButton != null) {
            draggedButton.drawButton(mc, mouseX, mouseY);
        } else {
            drawButtonTooltip(mc, mouseX, mouseY);
        }
    }

    /**
     * draw a tooltip for the button over which the mouse is hovering (if the delay has passed)
     *
     * @param mc     Minecraft instance
     * @param mouseX X location of the mouse
     * @param mouseY Y location of the mouse
     */
    private void drawButtonTooltip(Minecraft mc, int mouseX, int mouseY) {
        if (mouseX >= this.internalLeft + TOGGLE_BUTTON_WIDTH && mouseY >= this.internalTop
                && mouseX < this.listRight
                && mouseY < this.internalTop + (visibleButtonList.size() * BUTTON_HEIGHT)) {
            int buttonIndex = (mouseY - internalTop) / BUTTON_HEIGHT + scrollOffset;
            if (buttonIndex == previouslyHoveredButton) {
                GuiToggleButton button = buttonListFull.get(buttonIndex);
                if (hoverDelayLeft == 0) {
                    List<String> list = new ArrayList<>();
                    list.add(button.getFullTitle());
                    drawTooltip(mc, mouseX, mouseY, list);
                } else {
                    hoverDelayLeft--;
                }
            } else {
                hoverDelayLeft = HOVER_DELAY;
                previouslyHoveredButton = buttonIndex;
            }
        } else {
            hoverDelayLeft = HOVER_DELAY;
            previouslyHoveredButton = -1;
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

    /**
     * method to update visible buttons based off of scrollOffset. the scrollOffset variable should be changed before
     * calling this method
     */
    private void updateVisibleButtons() {
        buttonList.removeAll(buttonListFull);
        visibleButtonList.clear();
        scrollOffset = Math.max(0, Math.min(scrollOffset, buttonListFull.size() - VISIBLE_BUTTONS));
        for (int i = scrollOffset; i < scrollOffset + VISIBLE_BUTTONS && i < buttonListFull.size(); i++) {
            GuiToggleButton btn = buttonListFull.get(i);
            btn.yPosition = PADDING_TOP + (i - scrollOffset) * BUTTON_HEIGHT + guiTop;
            visibleButtonList.add(btn);
        }
        buttonList.addAll(visibleButtonList);
    }

    /**
     * set an openGL scissor
     *
     * @param x      left
     * @param y      top
     * @param width  width
     * @param height height
     */
    private void setScissor(int x, int y, int width, int height) {
        int scaleFactor = mc.gameSettings.guiScale;
        GL11.glScissor(
                x * scaleFactor,
                (mc.displayHeight - (y + height) * scaleFactor),
                width * scaleFactor,
                height * scaleFactor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            // check if click was within general GUI bounds
            if (mouseX > internalLeft && mouseX < guiRight - PADDING_RIGHT) {
                if (mouseX > internalLeft + TOGGLE_BUTTON_WIDTH && mouseX < listRight) {
                    if (mouseY > internalTop && mouseY < internalBottom - FUNCTION_BUTTON_HEIGHT) {
                        for (GuiToggleButton o : visibleButtonList) {
                            if (mouseY >= o.yPosition && mouseY < o.yPosition + BUTTON_HEIGHT) {
                                draggedButton = o;
                                originalIndex = buttonListFull.indexOf(draggedButton);
                                o.dragOffsetY = mouseY - o.yPosition;
                                break;
                            }
                        }
                    }
                    // check if the click was within scrollbar bounds
                } else if (mouseX > scrollbarLeft && mouseX < scrollbarRight
                        && mouseY > internalTop
                        && mouseY < internalBottom - FUNCTION_BUTTON_HEIGHT - PADDING_BOTTOM + 2) {
                            scrollbarOffset = Math.max(0, Math.min(mouseY - thumbLocation, THUMB_HEIGHT));
                            moveScrollbar(mouseY);
                        }
                if (mouseX > internalLeft && mouseX < internalLeft + GUI_WIDTH
                        && mouseY > internalBottom - FUNCTION_BUTTON_HEIGHT
                        && mouseY < internalBottom) {
                    for (SmallGuiButton b : functionButtons) {
                        b.mousePressed(mc, mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        // check if a button is being dragged
        if (draggedButton != null) {
            int adjustedY = mouseY - draggedButton.dragOffsetY;
            if (adjustedY < internalTop) {
                adjustedY = internalTop;
                scrollUp();
            } else if (adjustedY > internalBottom - FUNCTION_BUTTON_HEIGHT - PADDING_BOTTOM - BUTTON_HEIGHT) {
                adjustedY = internalBottom - FUNCTION_BUTTON_HEIGHT - PADDING_BOTTOM - BUTTON_HEIGHT;
                scrollDown();
            }
            draggedButton.setPosition(adjustedY);
            // no button is being dragged, but the player might be scrolling.
        } else if (scrollbarOffset != -1) {
            moveScrollbar(mouseY);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        // check if there is a button being dragged
        if (draggedButton != null) {
            int newIndex = (mouseY - guiTop + PADDING_TOP) / BUTTON_HEIGHT + scrollOffset - 1;
            newIndex = Math.max(0, Math.min(buttonListFull.size() - 1, newIndex));

            if (newIndex != originalIndex) {
                buttonListFull.remove(originalIndex);
                buttonListFull.add(newIndex, draggedButton);
            }

            draggedButton = null;
            updateVisibleButtons();
            newDataSorter.computeSortOrder(originalButtonList, buttonListFull);
            dataSorterChanged = true;
            // check if the scrollbar is being used
        } else if (scrollbarOffset != -1) {
            moveScrollbar(mouseY);
            scrollbarOffset = -1;
        }
    }

    /**
     * move the scrollbar to the mouseY position, keeping it within bounds.
     *
     * @param mouseY new Y position of the mouse
     */
    private void moveScrollbar(int mouseY) {
        int adjustedY = mouseY - scrollbarOffset;
        thumbLocation = adjustedY;
        if (adjustedY < internalTop) {
            thumbLocation = internalTop;
        } else if (adjustedY > internalBottom - THUMB_HEIGHT - PADDING_BOTTOM - FUNCTION_BUTTON_HEIGHT) {
            thumbLocation = internalBottom - THUMB_HEIGHT - PADDING_BOTTOM - FUNCTION_BUTTON_HEIGHT;
        }
        double offsetRatio = (double) (thumbLocation - internalTop)
                / (internalBottom - internalTop - THUMB_HEIGHT - PADDING_BOTTOM - FUNCTION_BUTTON_HEIGHT);
        int newScrollOffset = (int) (offsetRatio * (buttonListFull.size() - VISIBLE_BUTTONS));
        scrollOffset = Math.max(0, Math.min(newScrollOffset, buttonListFull.size() - VISIBLE_BUTTONS));
        updateVisibleButtons();
    }

    /**
     * Update the scrollbar's thumb location by the currently visible buttons
     */
    private void updateThumbLocation() {
        double locationRatio = (double) scrollOffset / (buttonListFull.size() - VISIBLE_BUTTONS);
        int newThumbLocation = (int) (locationRatio * (LIST_HEIGHT - THUMB_HEIGHT) + internalTop);
        if (newThumbLocation < internalTop + 1) {
            newThumbLocation = internalTop;
        } else if (newThumbLocation > internalBottom - THUMB_HEIGHT - PADDING_BOTTOM - FUNCTION_BUTTON_HEIGHT) {
            newThumbLocation = internalBottom - THUMB_HEIGHT - PADDING_BOTTOM - FUNCTION_BUTTON_HEIGHT;
        }
        thumbLocation = newThumbLocation;
        updateVisibleButtons();
    }

    // Handle scrolling with the mouse wheel
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = Mouse.getDWheel();

        if (scroll > 0) scrollUp();
        else if (scroll < 0) scrollDown();
    }

    /**
     * Scroll up by one SCROLL_SPEED
     */
    private void scrollUp() {
        if (scrollOffset > 0) {
            scrollOffset -= SCROLL_SPEED;
            updateThumbLocation();
        }
    }

    /**
     * Scroll down by one SCROLL_SPEED
     */
    private void scrollDown() {
        if (scrollOffset + VISIBLE_BUTTONS < buttonListFull.size()) {
            scrollOffset += SCROLL_SPEED;
            updateThumbLocation();
        }
    }

    // This GUI should not pause the game
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // Return to the AdvancedInfoPanel when the esc button is pressed
    @Override
    protected void keyTyped(char par1, int par2) {
        if (par2 == 1) {
            FMLClientHandler.instance().getClient().displayGuiScreen(parentGui);
        } else super.keyTyped(par1, par2);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (dataSorterChanged) {
            NuclearNetworkHelper.sendDataSorterSync(panel);
        }
    }

    private void onReset() {
        panel.setDataSorter(cardSlot, new DataSorter(), false);
        dataSorterChanged = true;
        panel.setDisplaySettings(cardSlot, new DisplaySettingHelper());
        buttonListFull = new ArrayList<>(originalButtonList);
        updateVisibleButtons();
    }

    private void onSave() {
        if (dataSorterChanged) {
            NuclearNetworkHelper.sendDataSorterSync(panel);
        }
        mc.displayGuiScreen(parentGui);
    }

    private void onCancel() {
        panel.setDataSorter(cardSlot, originalDataSorter, true);
        panel.setDisplaySettings(cardSlot, originalDisplaySettingHelper);
        mc.displayGuiScreen(parentGui);
    }
}
