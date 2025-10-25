package shedar.mods.ic2.nuclearcontrol.crossmod.vanilla;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.ICardWrapper;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardBase;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class ItemCardInventoryScanner extends ItemCardBase {

    public static final int DISPLAY_NAME = 0;
    public static final int DISPLAY_TOTAL = 1;

    public ItemCardInventoryScanner() {
        super("cardVanilla");
    }

    @Override
    public CardState update(TileEntity panel, ICardWrapper card, int range) {
        return this.update(panel.getWorldObj(), card, range);
    }

    @Override
    public CardState update(World world, ICardWrapper card, int range) {
        ChunkCoordinates target = card.getTarget();
        if (target == null) return CardState.NO_TARGET;
        TileEntity tile = world.getTileEntity(target.posX, target.posY, target.posZ);
        if (tile instanceof IInventory) {
            IInventory inv = (IInventory) tile;
            int inUse = 0;
            for (int z = 0; z < inv.getSizeInventory(); z++) {
                if (inv.getStackInSlot(z) != null) {
                    inUse++;
                }
            }
            card.setString("name", inv.getInventoryName());
            card.setInt("totalInv", inv.getSizeInventory());
            card.setInt("totalInUse", inUse);
            return CardState.OK;
        }
        return CardState.INVALID_CARD;
    }

    @Override
    public UUID getCardType() {
        return new UUID(0, 2);
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, ICardWrapper card,
            boolean showLabels) {
        List<PanelString> result = new LinkedList<PanelString>();
        PanelString line;

        String name = card.getString("name");
        int TotalInv = card.getInt("totalInv");
        int TotalInUse = card.getInt("totalInUse");

        if (displaySettings.getSetting(DISPLAY_NAME)) {
            line = new PanelString();
            line.textLeft = StringUtils.getFormatted("%s", StatCollector.translateToLocal(name), showLabels);
            result.add(line);
        }
        if (displaySettings.getSetting(DISPLAY_TOTAL)) {
            line = new PanelString();
            line.textLeft = String
                    .format(StatCollector.translateToLocal("msg.nc.Vanilla.Display"), TotalInUse, TotalInv);
            result.add(line);
        }
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>();
        result.add(
                new NewPanelSetting(
                        StatCollector.translateToLocal("msg.nc.Vanilla.Name"),
                        DISPLAY_NAME,
                        getCardType()));
        result.add(
                new NewPanelSetting(
                        StatCollector.translateToLocal("msg.nc.Vanilla.StorageLVL"),
                        DISPLAY_TOTAL,
                        getCardType()));
        return result;
    }
}
