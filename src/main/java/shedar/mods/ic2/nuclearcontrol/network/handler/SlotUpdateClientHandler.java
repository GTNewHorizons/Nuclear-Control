package shedar.mods.ic2.nuclearcontrol.network.handler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import shedar.mods.ic2.nuclearcontrol.inventory.ITEInventoryHolder;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketUpdateSlotNBT;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;

public class SlotUpdateClientHandler implements IMessageHandler<PacketUpdateSlotNBT, IMessage> {
    public SlotUpdateClientHandler() {}

    @Override
    public IMessage onMessage(PacketUpdateSlotNBT msg, MessageContext ctx) {
        // Server → client: update local card NBT and invalidate render cache
        WorldClient world = FMLClientHandler.instance().getClient().theWorld;
        TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
        if (!(te instanceof ITEInventoryHolder inventoryHolder)) return null;
        inventoryHolder.getInventory().updateNBT(msg.slot, msg.stack);
        if (te instanceof TileEntityInfoPanel panel) panel.cardCache.markExternalSync(msg.slot);
        return null;
    }
}
