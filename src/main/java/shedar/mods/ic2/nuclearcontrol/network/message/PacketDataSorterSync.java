package shedar.mods.ic2.nuclearcontrol.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import shedar.mods.ic2.nuclearcontrol.containers.ContainerAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class PacketDataSorterSync implements IMessage, IMessageHandler<PacketDataSorterSync, IMessage> {

    private int x;
    private int y;
    private int z;
    private NBTTagCompound dataSortersTag;

    public PacketDataSorterSync() {}

    public PacketDataSorterSync(TileEntityAdvancedInfoPanel panel) {
        this.x = panel.xCoord;
        this.y = panel.yCoord;
        this.z = panel.zCoord;
        this.dataSortersTag = new NBTTagCompound();
        panel.writeDataSortersToNBT(dataSortersTag);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        dataSortersTag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeTag(buf, dataSortersTag);
    }

    @Override
    public IMessage onMessage(PacketDataSorterSync message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            TileEntityAdvancedInfoPanel panel = (TileEntityAdvancedInfoPanel) Minecraft.getMinecraft().theWorld
                    .getTileEntity(message.x, message.y, message.z);
            if (panel != null) {
                panel.readDataSortersFromNBT(message.dataSortersTag);
            }
        } else if (ctx.side.isServer()) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            Container openContainer = player.openContainer;
            if (openContainer instanceof ContainerAdvancedInfoPanel) {
                TileEntityAdvancedInfoPanel panel = (TileEntityAdvancedInfoPanel) ((ContainerAdvancedInfoPanel) openContainer).panel;
                if (panel != null && panel.xCoord == message.x
                        && panel.yCoord == message.y
                        && panel.zCoord == message.z
                        && panel == player.worldObj.getTileEntity(message.x, message.y, message.z)) {
                    panel.readDataSortersFromNBT(message.dataSortersTag);
                    panel.markDirty();
                    player.worldObj.markBlockForUpdate(message.x, message.y, message.z);
                    NuclearNetworkHelper.sendDataSorterSync(panel);
                }
            }
        }
        return null;
    }
}
