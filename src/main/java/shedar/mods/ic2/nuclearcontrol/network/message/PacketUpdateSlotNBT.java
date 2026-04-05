package shedar.mods.ic2.nuclearcontrol.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

public class PacketUpdateSlotNBT implements IMessage {
    public int x, y, z, slot;
    public ItemStack stack;

    public PacketUpdateSlotNBT() {}

    public PacketUpdateSlotNBT(int x, int y, int z, int slot, ItemStack stack) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        slot = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(slot);
        ByteBufUtils.writeItemStack(buf, stack);
    }
}