package shedar.mods.ic2.nuclearcontrol.panel;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.api.ICardSettingsWrapper;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.gui.GuiInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class CardSettingsWrapperImpl implements ICardSettingsWrapper {

    private final ItemStack card;
    private final TileEntity panel;
    private Map<String, Object> updateSet;
    private final GuiInfoPanel gui;
    private final int slot;

    public CardSettingsWrapperImpl(ItemStack card, TileEntity panel, GuiInfoPanel gui, int slot) {
        if (!(card.getItem() instanceof IPanelDataSource)) {
            IC2NuclearControl.logger.error("CardHelper sould be used for IPanelDataSource items.");
        }
        this.card = card;
        this.panel = panel;
        updateSet = new HashMap<String, Object>();
        this.gui = gui;
        this.slot = slot;
    }

    @Override
    public void setInt(String name, Integer value) {
        updateSet.put(name, value);
    }

    @Override
    public void setDouble(String name, double value) {
        updateSet.put(name, value);
    }

    @Override
    public void setString(String name, String value) {
        updateSet.put(name, value);
    }

    @Override
    public void setBoolean(String name, Boolean value) {
        updateSet.put(name, value);
    }

    @Override
    public void commit() {
        if (!updateSet.isEmpty()) {
            NuclearNetworkHelper.setCardSettings(card, panel, updateSet, slot);
            updateSet = new HashMap<String, Object>();
        }

    }

    @Override
    public void closeGui() {
        gui.prevCard = null;
        FMLClientHandler.instance().getClient().displayGuiScreen(gui);
    }

}
