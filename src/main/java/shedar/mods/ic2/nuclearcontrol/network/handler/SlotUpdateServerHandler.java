package shedar.mods.ic2.nuclearcontrol.network.handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import shedar.mods.ic2.nuclearcontrol.inventory.ITEInventoryHolder;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketUpdateSlotNBT;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInventory;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class SlotUpdateServerHandler implements IMessageHandler<PacketUpdateSlotNBT, IMessage> {
    @Override
    public IMessage onMessage(PacketUpdateSlotNBT msg, MessageContext ctx) {
        // Client → server: apply UI-driven NBT change, then broadcast to all clients
        World world = ctx.getServerHandler().playerEntity.worldObj;
        TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
        if (!(te instanceof ITEInventoryHolder inventoryHolder)) return null;
        TileEntityInventory inventory = inventoryHolder.getInventory();

        if (msg.stack == null) {
            inventory.remove(msg.slot);
            world.markBlockForUpdate(msg.x, msg.y, msg.z);
        } else {
            inventoryHolder.getInventory().updateNBT(msg.slot, msg.stack);
            NuclearNetworkHelper.sendItemSyncPacket(te, (byte) msg.slot, msg.stack);
        }

        te.markDirty();
        return null;
    }
}
