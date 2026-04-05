package shedar.mods.ic2.nuclearcontrol.crossmod.appeng;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import appeng.api.storage.data.IAEItemStack;
import appeng.tile.crafting.TileCraftingMonitorTile;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IRangeTriggerable;
import shedar.mods.ic2.nuclearcontrol.api.IRemoteSensor;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.inventory.nbt.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardBase;
import shedar.mods.ic2.nuclearcontrol.utils.CardAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;


import static shedar.mods.ic2.nuclearcontrol.items.ItemCardEnergySensorLocation.CARD_TYPE;

public class ItemCardAppeng extends ItemCardBase implements IRemoteSensor, IRangeTriggerable {
    public ItemCardAppeng() {
        super("nuclearcontrol:cardAEMonitor");
        //this.setTextureName("nuclearcontrol:cardAEMonitor");
        this.setUnlocalizedName("AppengCard");
    }

    public static final int DISPLAY_BYTES = 1;
    public static final int DISPLAY_ITEMS = 2;
    public static final int DISPLAY_CRAFTER = 3;
    public static final int DISPLAY_CRAFTSTACK = 4;
    // public static final int DISPLAY_TEMP = 16;
    public static final UUID CARD_TYPE1 = new UUID(0, 2);

    @Override
    public UUID getCardType() {
        return CARD_TYPE1;
    }

    @Override
    public AppengCardData getLayout() {
        return new AppengCardData();
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        ChunkCoordinates target = CardAccessors.getCoordinates(card);
        if (target == null) return CardState.NO_TARGET;
        AppengCardData data = (AppengCardData) layout;

        int targetType = data.targetType.get();

        if (targetType == 1) {
            TileEntity check = world.getTileEntity(target.posX, target.posY, target.posZ);
            if (check instanceof TileEntityNetworkLink) {
                TileEntityNetworkLink tileNetworkLink = (TileEntityNetworkLink) check;
                data.byteTotal.set(tileNetworkLink.getTOTALBYTES());
                data.usedBytes.set(tileNetworkLink.getUSEDBYTES());
                data.itemsTotal.set(tileNetworkLink.getITEMTYPETOTAL());
                data.usedItems.set(tileNetworkLink.getUSEDITEMTYPE());
                return CardState.OK;
            } else {
                return CardState.NO_TARGET;
            }
        } else if (targetType == 2) {
            TileEntity check = world.getTileEntity(target.posX, target.posY, target.posZ);
            if (check instanceof TileCraftingMonitorTile) {
                TileCraftingMonitorTile monitorTile = (TileCraftingMonitorTile) check;
                Item crafter;
                int size;
                if (monitorTile.getJobProgress() instanceof IAEItemStack ais) {
                    crafter = ais.getItem();
                    size = (int) ais.getStackSize();
                } else {
                    crafter = CrossAppeng.cardAppeng;
                    size = 0;
                }
                data.itemStack.set(Item.getIdFromItem(crafter));
                data.stackSize.set(size);
                return CardState.OK;
            }
        } else {
            return CardState.NO_TARGET;
        }
        return CardState.NO_TARGET;
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card, NBTCardLayout layout,
            boolean showLabels) {
        List<PanelString> result = new LinkedList<PanelString>();
        PanelString line;
        AppengCardData data = (AppengCardData) layout;
        int TYPE = data.targetType.get();

        if (TYPE == 1) {
            int byteTotal = data.byteTotal.get();
            int usedBytes = data.usedBytes.get();
            int items = data.itemsTotal.get();
            int itemsUsed = data.usedItems.get();

            // Total Bytes
            if (displaySettings.getSetting(DISPLAY_BYTES)) {
                line = new PanelString();
                line.textRight = String.format(
                        StatCollector.translateToLocal("msg.nc.InfoPanelAE.DisplayBytes"),
                        usedBytes,
                        byteTotal);
                result.add(line);
            }

            // Used Items
            if (displaySettings.getSetting(DISPLAY_ITEMS)) {
                line = new PanelString();
                line.textRight = String
                        .format(StatCollector.translateToLocal("msg.nc.InfoPanelAE.DisplayItem"), itemsUsed, items);
                result.add(line);
            }
        } else if (TYPE == 2) {
            int stackSize = data.stackSize.get();
            Item item = Item.getItemById(data.itemStack.get());
            String localName = "item.null.name";
            try {
                localName = StatCollector.translateToLocal(item.getUnlocalizedName() + ".name");
            } catch (NullPointerException e) {}
            if (localName == "item.null.name" || localName.equals("Applied Energistics Card")) {
                localName = StatCollector.translateToLocal("msg.null.craft");
            }

            // Crafting item
            if (displaySettings.getSetting(DISPLAY_CRAFTER)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelAE.CraftItemMake", localName, showLabels);
                result.add(line);
            }

            // Crafting Stacks
            if (displaySettings.getSetting(DISPLAY_CRAFTSTACK)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelAE.CraftAMT", stackSize, showLabels);
                result.add(line);
            }
        }
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>(4);
        result.add(new NewPanelSetting(LangHelper.translate("1"), DISPLAY_BYTES, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("2"), DISPLAY_ITEMS, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("3"), DISPLAY_CRAFTER, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("4"), DISPLAY_CRAFTSTACK, CARD_TYPE));
        return result;
    }

    public static class AppengCardData extends NBTCardLayout {
        public DataAccessor<Integer> byteTotal = intAccessor("ByteTotal");
        public DataAccessor<Integer> usedBytes = intAccessor("UsedBytes");
        public DataAccessor<Integer> itemsTotal = intAccessor("ItemsTotal");
        public DataAccessor<Integer> usedItems = intAccessor("UsedItems");
        public DataAccessor<Integer> itemStack = intAccessor("ITEMSTACK");
        public DataAccessor<Integer> stackSize = intAccessor("STACKSIZE");
        public DataAccessor<Integer> targetType = intAccessor("targetType");
    }
}
