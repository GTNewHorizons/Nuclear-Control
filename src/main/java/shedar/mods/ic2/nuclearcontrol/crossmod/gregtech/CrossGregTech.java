package shedar.mods.ic2.nuclearcontrol.crossmod.gregtech;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.items.ItemRadioactiveCell;
import gregtech.api.items.ItemRadioactiveCellIC;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.crossmod.ModLib;

public class CrossGregTech {

    private boolean _isApiAvailable;

    public CrossGregTech() {
        if (Loader.isModLoaded(ModLib.GT)) {
            _isApiAvailable = true;
            IC2NuclearControl.logger.info("[IC2NuclearControl] find GregTech");
        } else {
            _isApiAvailable = false;
        }

    }

    public boolean isApiAvailable() {
        return _isApiAvailable;
    }

    public int getNuclearCellTimeLeft(ItemStack stack) {
        if (!_isApiAvailable || stack == null) {
            return 0;
        }
        try {
            if (stack.getItem() instanceof ItemRadioactiveCellIC) {
                String rodName = stack.getUnlocalizedName();
                ItemRadioactiveCell rod = (ItemRadioactiveCell) GameRegistry.findItem(ModLib.GT, rodName);
                int maxDamage = rod.getMaxDamageEx();
                int currentDmg = ItemRadioactiveCellIC.getDurabilityOfStack(stack);
                int dmg = maxDamage - currentDmg;
                return (dmg > 0) ? dmg : 0;
            } else {
                return -1;
            }
        } catch (Exception e) {
            IC2NuclearControl.logger.error(e);
            return -1;
        }
    }
}
