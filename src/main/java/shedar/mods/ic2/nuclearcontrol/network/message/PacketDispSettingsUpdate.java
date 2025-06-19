package shedar.mods.ic2.nuclearcontrol.network.message;

import java.util.UUID;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;

public class PacketDispSettingsUpdate implements IMessage, IMessageHandler<PacketDispSettingsUpdate, IMessage> {

    private int x;
    private int y;
    private int z;
    private byte slot;
    private UUID key;
    private DisplaySettingHelper value;
    private long most;
    private long least;

    public PacketDispSettingsUpdate() {}

    public PacketDispSettingsUpdate(int x, int y, int z, byte slot, UUID key, DisplaySettingHelper value) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.slot = slot;
        this.key = key;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        slot = buf.readByte();
        most = buf.readLong();
        least = buf.readLong();
        value = new DisplaySettingHelper(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(slot);
        buf.writeLong(key.getMostSignificantBits());
        buf.writeLong(key.getLeastSignificantBits());
        value.writeToByteBuffer(buf);
    }

    @Override
    public IMessage onMessage(PacketDispSettingsUpdate message, MessageContext ctx) {
        TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
                .getTileEntity(message.x, message.y, message.z);
        if (tileEntity == null || !(tileEntity instanceof TileEntityInfoPanel)) {
            return null;
        }
        TileEntityInfoPanel panel = (TileEntityInfoPanel) tileEntity;
        panel.getDisplaySettingsForSlot(message.slot).put(new UUID(message.most, message.least), message.value);
        panel.resetCardData();
        return null;
    }
}
