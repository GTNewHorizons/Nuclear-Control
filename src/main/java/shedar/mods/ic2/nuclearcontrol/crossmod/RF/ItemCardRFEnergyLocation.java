package shedar.mods.ic2.nuclearcontrol.crossmod.RF;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyHandler;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.ICardWrapper;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardEnergySensorLocation;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class ItemCardRFEnergyLocation extends ItemCardEnergySensorLocation {

    public static final UUID CARD_TYPE = new UUID(0, 3);

    public ItemCardRFEnergyLocation() {
        this.setUnlocalizedName("RFenergyCard");

    }

    @Override
    public UUID getCardType() {
        return CARD_TYPE;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>();
        result.add(new NewPanelSetting(LangHelper.translate("1"), DISPLAY_ENERGY, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("2"), DISPLAY_STORAGE, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("3"), DISPLAY_FREE, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("4"), DISPLAY_PERCENTAGE, CARD_TYPE));
        return result;
    }
}
