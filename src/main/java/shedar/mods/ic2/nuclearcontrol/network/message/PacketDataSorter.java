package shedar.mods.ic2.nuclearcontrol.network.message;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import shedar.mods.ic2.nuclearcontrol.containers.ContainerAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.containers.ContainerInfoPanel;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.DataSorter;

public class PacketDataSorter implements IMessage, IMessageHandler<PacketDataSorter, IMessage> {

    private int x;
    private int y;
    private int z;
    private byte slot;
    private DataSorter sorter;

    public PacketDataSorter() {}

    public PacketDataSorter(TileEntityAdvancedInfoPanel panel, byte slot, DataSorter sorter) {
        this.x = panel.xCoord;
        this.y = panel.yCoord;
        this.z = panel.zCoord;
        this.slot = slot;
        this.sorter = sorter;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.slot = buf.readByte();
        int length = buf.readableBytes() / 4;
        int[] intArray = new int[length];
        int i = 0;
        while (buf.readableBytes() > 0) {
            intArray[i] = buf.readInt();
            i++;
        }
        sorter = new DataSorter(intArray);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(slot);
        int[] intArray = sorter.getArray();
        for (int j : intArray) {
            buf.writeInt(j);
        }
    }

    @Override
    public IMessage onMessage(PacketDataSorter message, MessageContext ctx) {
        if (ctx.side == Side.SERVER) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            Container openContainer = player.openContainer;
            if (openContainer instanceof ContainerInfoPanel) {
                int x = message.x;
                int y = message.y;
                int z = message.z;
                TileEntityAdvancedInfoPanel panel = (TileEntityAdvancedInfoPanel) ((ContainerAdvancedInfoPanel) openContainer).panel;
                if (panel != null && panel.xCoord == x
                        && panel.yCoord == y
                        && panel.zCoord == z
                        && panel == player.worldObj.getTileEntity(x, y, z))
                    panel.setDataSorter(message.slot, message.sorter, false);

            }
        } else if (ctx.side == Side.CLIENT) {
            TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
                    .getTileEntity(message.x, message.y, message.z);
            if (tileEntity == null || !(tileEntity instanceof TileEntityAdvancedInfoPanel)) {
                return null;
            }
            TileEntityAdvancedInfoPanel panel = (TileEntityAdvancedInfoPanel) tileEntity;
            panel.setDataSorter(message.slot, message.sorter, false);
        }

        return null;
    }
}
