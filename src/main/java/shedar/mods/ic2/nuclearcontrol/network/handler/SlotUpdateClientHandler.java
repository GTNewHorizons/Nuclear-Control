package shedar.mods.ic2.nuclearcontrol.network.handler;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import shedar.mods.ic2.nuclearcontrol.inventory.ITEInventoryHolder;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketUpdateSlotNBT;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInventory;

public class SlotUpdateClientHandler implements IMessageHandler<PacketUpdateSlotNBT, IMessage> {

    public SlotUpdateClientHandler() {}

    @Override
    public IMessage onMessage(PacketUpdateSlotNBT msg, MessageContext ctx) {
        // Server → client: update local card NBT and invalidate render cache
        WorldClient world = FMLClientHandler.instance().getClient().theWorld;
        TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
        if (!(te instanceof ITEInventoryHolder inventoryHolder)) return null;
        if (te instanceof TileEntityInfoPanel panel) panel.cardCache.markExternalSync(msg.slot);
        TileEntityInventory inventory = inventoryHolder.getInventory();

        if (msg.stack == null) inventory.remove(msg.slot);
        else inventory.set(msg.slot, msg.stack);

        return null;
    }
}
