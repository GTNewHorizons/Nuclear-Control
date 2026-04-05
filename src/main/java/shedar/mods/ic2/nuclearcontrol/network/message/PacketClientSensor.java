package shedar.mods.ic2.nuclearcontrol.network.message;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.containers.ContainerInfoPanel;
//import shedar.mods.ic2.nuclearcontrol.panel.CardWrapperImpl;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class PacketClientSensor implements IMessage, IMessageHandler<PacketClientSensor, IMessage> {

    private int x;
    private int y;
    private int z;
    private int slot;
    private String className;
    private Map<String, Object> fields;

    public static final int FIELD_DOUBLE = 1;
    public static final int FIELD_INT = 2;
    public static final int FIELD_STRING = 3;
    public static final int FIELD_BOOLEAN = 4;
    public static final int FIELD_TAG = 5;
    public static final int FIELD_NULL = 6;
    public static final int FIELD_LONG = 7;

    public PacketClientSensor() {}

    public PacketClientSensor(int x, int y, int z, int slot, String className, Map<String, Object> fields) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.slot = slot;
        this.className = className;
        this.fields = fields;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        slot = buf.readByte();
        className = ByteBufUtils.readUTF8String(buf);
        int fieldCount = buf.readShort();
        fields = new HashMap<String, Object>();
        for (int i = 0; i < fieldCount; i++) {
            String name = ByteBufUtils.readUTF8String(buf);
            byte type = buf.readByte();
            switch (type) {
                case NuclearNetworkHelper.FIELD_INT:
                    fields.put(name, buf.readInt());
                    break;
                case NuclearNetworkHelper.FIELD_BOOLEAN:
                    fields.put(name, buf.readBoolean());
                    break;
                case NuclearNetworkHelper.FIELD_LONG:
                    fields.put(name, buf.readLong());
                    break;
                case NuclearNetworkHelper.FIELD_DOUBLE:
                    fields.put(name, buf.readDouble());
                    break;
                case NuclearNetworkHelper.FIELD_STRING:
                    fields.put(name, ByteBufUtils.readUTF8String(buf));
                    break;
                default:
                    IC2NuclearControl.logger.warn("Invalid field type: %d", type);
                    break;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(slot);
        ByteBufUtils.writeUTF8String(buf, className);
        buf.writeShort(fields.size());
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Long) {
                buf.writeByte(FIELD_LONG);
                buf.writeLong((Long) value);
            } else if (value instanceof Double) {
                buf.writeByte(FIELD_DOUBLE);
                buf.writeDouble((Double) value);
            } else if (value instanceof Integer) {
                buf.writeByte(FIELD_INT);
                buf.writeInt((Integer) value);
            } else if (value instanceof String) {
                buf.writeByte(FIELD_STRING);
                ByteBufUtils.writeUTF8String(buf, (String) value);
            } else if (value instanceof Boolean) {
                buf.writeByte(FIELD_BOOLEAN);
                buf.writeBoolean((Boolean) value);
            }
        }
    }

    @Override
    public IMessage onMessage(PacketClientSensor message, MessageContext ctx) {
        /*
         * TileEntity tile = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y,
         * message.z); if (tile instanceof TileEntityInfoPanel) { TileEntityInfoPanel panel = (TileEntityInfoPanel)
         * tile; ItemStack stack = panel.getStackInSlot(message.slot); if (stack == null || !(stack.getItem() instanceof
         * IPanelDataSource)) return null; if (!stack.getItem().getClass().getName().equals(message.className)) {
         * IC2NuclearControl.logger.warn("Class mismatch: '%s'!='%s'", message.className,
         * stack.getItem().getClass().getName()); return null; } CardWrapperImpl helper = new CardWrapperImpl(stack,
         * message.slot); for (Map.Entry<String, Object> entry : message.fields.entrySet()) { String name =
         * entry.getKey(); Object value = entry.getValue(); if (value instanceof Long) helper.setLong(name, (Long)
         * value); else if (value instanceof Double) helper.setDouble(name, (Double) value); else if (value instanceof
         * Integer) helper.setInt(name, (Integer) value); else if (value instanceof String) helper.setString(name,
         * (String) value); else if (value instanceof Boolean) helper.setBoolean(name, (Boolean) value); }
         * helper.commit(panel); }
         */
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container openContainer = player.openContainer;
        if (openContainer instanceof ContainerInfoPanel) {
            int x = message.x;
            int y = message.y;
            int z = message.z;
            TileEntityInfoPanel panel = ((ContainerInfoPanel) openContainer).panel;
            if (panel != null && panel.xCoord == x
                    && panel.yCoord == y
                    && panel.zCoord == z
                    && panel == player.worldObj.getTileEntity(x, y, z)) {
                ItemStack stack = panel.getStackInSlot(message.slot);
                if (stack == null || !(stack.getItem() instanceof IPanelDataSource)) return null;
                if (!stack.getItem().getClass().getName().equals(message.className)) {
                    IC2NuclearControl.logger.warn(
                            "Class mismatch: '%s'!='%s'",
                            message.className,
                            stack.getItem().getClass().getName());
                    return null;
                }
//                CardWrapperImpl helper = new CardWrapperImpl(stack, message.slot);
//                for (Map.Entry<String, Object> entry : message.fields.entrySet()) {
//                    String name = entry.getKey();
//                    Object value = entry.getValue();
//                    if (value instanceof Long) helper.setLong(name, (Long) value);
//                    else if (value instanceof Double) helper.setDouble(name, (Double) value);
//                    else if (value instanceof Integer) helper.setInt(name, (Integer) value);
//                    else if (value instanceof String) helper.setString(name, (String) value);
//                    else if (value instanceof Boolean) helper.setBoolean(name, (Boolean) value);
//
//                }
//                helper.commit(panel);
            }
        }

        return null;
    }
}
