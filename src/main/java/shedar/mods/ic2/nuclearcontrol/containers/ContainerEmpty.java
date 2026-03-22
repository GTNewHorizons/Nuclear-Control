package shedar.mods.ic2.nuclearcontrol.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;

public class ContainerEmpty extends Container {

    public final TileEntity entity;

    public ContainerEmpty(TileEntity entity) {
        super();
        this.entity = entity;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        if (this.entity == null || !this.entity.hasWorldObj()) return false;
        return this.entity.getWorldObj().getBlock(this.entity.xCoord, this.entity.yCoord, this.entity.zCoord)
                == IC2NuclearControl.blockNuclearControlMain
                && player.getDistanceSq(this.entity.xCoord + 0.5D, this.entity.yCoord + 0.5D, this.entity.zCoord + 0.5D)
                        <= 64.0D;
    }
}
