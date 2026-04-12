package shedar.mods.ic2.nuclearcontrol.items;

import java.util.List;
import java.util.UUID;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.utils.TextureResolver;

public abstract class ItemCardBase extends Item implements IPanelDataSource {

    public ItemCardBase(String textureItemName) {
        super();
        setMaxStackSize(1);
        canRepair = false;
        this.setCreativeTab(IC2NuclearControl.tabIC2NC);
        setTextureName(TextureResolver.getItemTexture(textureItemName));
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    /*
     * @SuppressWarnings("rawtypes")
     * @Override public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List){ //should not be
     * created via creative inventory }
     */

    @Override
    public CardState update(TileEntity panel, IndexedItem<?> card, NBTCardLayout layout, int range) {
        return update(panel.getWorldObj(), card, layout, range);
    }

    @Override
    abstract public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range);

    @Override
    abstract public UUID getCardType();

    @Override
    abstract public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card,
            NBTCardLayout layout, boolean showLabels);

    @Override
    public List<PanelString> getStringData(int displaySettings, IndexedItem<?> card, NBTCardLayout layout,
            boolean showLabels) {
        return getStringData(new DisplaySettingHelper(displaySettings), card, layout, showLabels);
    }

    @Override
    abstract public List<PanelSetting> getSettingsList();

    protected boolean isTargetInvalid(ChunkCoordinates coords, World world) {
        if (coords == null) return true;
        return !world.getChunkProvider().chunkExists(coords.posX >> 4, coords.posZ >> 4)
                || world.getTileEntity(coords.posX, coords.posY, coords.posZ) == null;
    }
}
