package shedar.mods.ic2.nuclearcontrol.network.message;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAverageCounter;

public class PacketAcounter implements IMessage, IMessageHandler<PacketAcounter, IMessage> {

    private int x;
    private int y;
    private int z;
    private int average;

    public PacketAcounter() {}

    public PacketAcounter(int x, int y, int z, int average) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.average = average;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        average = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(average);
    }

    @Override
    public IMessage onMessage(PacketAcounter message, MessageContext ctx) {
        TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
                .getTileEntity(message.x, message.y, message.z);
        if (tileEntity == null || !(tileEntity instanceof TileEntityAverageCounter avgCounter)) {
            return null;
        }
        avgCounter.setClientAverage(message.average);
        return null;
    }
}
