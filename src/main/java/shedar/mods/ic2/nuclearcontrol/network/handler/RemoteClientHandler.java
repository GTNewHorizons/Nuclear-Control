package shedar.mods.ic2.nuclearcontrol.network.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.InventoryItem;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketRemoteMonitor;

public class RemoteClientHandler implements IMessageHandler<PacketRemoteMonitor, IMessage> {

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PacketRemoteMonitor message, MessageContext ctx) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack heldItem = player.getHeldItem();

        if (heldItem == null) return null;
        if (heldItem.getItem() != IC2NuclearControl.itemRemoteMonitor) return null;
        if (!(message.stack.getItem() instanceof IPanelDataSource)) return null;

        InventoryItem itemInv = new InventoryItem(player.getHeldItem());
        ItemStack insertCard = itemInv.getStackInSlot(0);

        if (!(insertCard.getItem() instanceof IPanelDataSource)) return null;
        insertCard.setTagCompound(message.stack.getTagCompound());

        itemInv.markDirty();
        return null;
    }
}
