package shedar.mods.ic2.nuclearcontrol.network.message;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.containers.ContainerEmpty;
import shedar.mods.ic2.nuclearcontrol.containers.ContainerInfoPanel;
import shedar.mods.ic2.nuclearcontrol.panel.CardWrapperImpl;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityHowlerAlarm;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class PacketClientSound implements IMessage, IMessageHandler<PacketClientSound, IMessage> {

    private int x;
    private int y;
    private int z;
    private byte slot;
    private String soundName;

    public PacketClientSound() {}

    public PacketClientSound(int x, int y, int z, byte slot, String soundName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.slot = slot;
        this.soundName = soundName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        slot = buf.readByte();
        soundName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(slot);
        ByteBufUtils.writeUTF8String(buf, soundName);
    }

    @Override
    public IMessage onMessage(PacketClientSound message, MessageContext ctx) {
        int x = message.x;
        int y = message.y;
        int z = message.z;
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container openContainer = player.openContainer;
        if (openContainer instanceof ContainerInfoPanel) {
            TileEntityInfoPanel panel = ((ContainerInfoPanel) openContainer).panel;
            if (panel != null && panel.xCoord == x
                    && panel.yCoord == y
                    && panel.zCoord == z
                    && panel == player.worldObj.getTileEntity(x, y, z)) {
                ItemStack stack = panel.getStackInSlot(message.slot);
                if (stack == null || !(stack.getItem() instanceof IPanelDataSource)) return null;
                new CardWrapperImpl(stack, -1).setTitle(message.soundName);
                NuclearNetworkHelper.setSensorCardTitle(panel, message.slot, message.soundName);
            }
        } else if (openContainer instanceof ContainerEmpty) {
            TileEntity tile = ((ContainerEmpty) openContainer).entity;
            if (tile instanceof TileEntityHowlerAlarm && tile.xCoord == x
                    && tile.yCoord == y
                    && tile.zCoord == z
                    && tile == player.worldObj.getTileEntity(x, y, z))
                ((TileEntityHowlerAlarm) tile).setSoundName(message.soundName);
        }

        return null;
    }
}
