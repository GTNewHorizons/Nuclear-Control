package shedar.mods.ic2.nuclearcontrol.network.message;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityEnergyCounter;

public class PacketEncounter implements IMessage, IMessageHandler<PacketEncounter, IMessage> {

    private int x;
    private int y;
    private int z;
    private Double counter;

    public PacketEncounter() {}

    public PacketEncounter(int x, int y, int z, Double counter) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.counter = counter;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        counter = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeDouble(counter);
    }

    @Override
    public IMessage onMessage(PacketEncounter message, MessageContext ctx) {
        TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
                .getTileEntity(message.x, message.y, message.z);
        if (tileEntity == null || !(tileEntity instanceof TileEntityEnergyCounter tileCounter)) {
            return null;
        }
        tileCounter.counter = message.counter;
        return null;
    }
}
