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
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
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
    public InvData getLayout() {
        return new InvData();
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        InvData data = (InvData) layout;
        ChunkCoordinates target = data.getTarget();
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
            data.name.set(inv.getInventoryName());
            data.totalInv.set(inv.getSizeInventory());
            data.totalInUse.set(inUse);
            return CardState.OK;
        }
        return CardState.INVALID_CARD;
    }

    @Override
    public UUID getCardType() {
        return new UUID(0, 2);
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card,
            NBTCardLayout layout, boolean showLabels) {
        List<PanelString> result = new LinkedList<PanelString>();
        PanelString line;

        InvData data = (InvData) layout;
        String name = data.name.get();
        int TotalInv = data.totalInv.get();
        int TotalInUse = data.totalInUse.get();

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

    public static class InvData extends NBTCardLayout {

        public DataAccessor<String> name = stringAccessor("name");
        public DataAccessor<Integer> totalInv = intAccessor("totalInv");
        public DataAccessor<Integer> totalInUse = intAccessor("totalInUse");
    }
}
