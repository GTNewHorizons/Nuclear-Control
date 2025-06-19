package shedar.mods.ic2.nuclearcontrol.utils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.network.ChannelHandler;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketAcounter;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketChat;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientColor;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientDisplaySettings;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientRangeTrigger;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientRequest;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientSensor;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientSound;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketDataSorterSync;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketDispSettingsAll;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketDispSettingsUpdate;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketEncounter;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketSensor;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketSensorTitle;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAverageCounter;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityEnergyCounter;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;

public class NuclearNetworkHelper {

    public static final int FIELD_DOUBLE = 1;
    public static final int FIELD_INT = 2;
    public static final int FIELD_STRING = 3;
    public static final int FIELD_BOOLEAN = 4;
    public static final int FIELD_TAG = 5;
    public static final int FIELD_NULL = 6;
    public static final int FIELD_LONG = 7;

    // server
    public static void sendEnergyCounterValue(TileEntityEnergyCounter counter, ICrafting crafter) {
        if (counter == null || !(crafter instanceof EntityPlayerMP)) return;
        ChannelHandler.network.sendTo(
                new PacketEncounter(counter.xCoord, counter.yCoord, counter.zCoord, counter.counter),
                (EntityPlayerMP) crafter);
    }

    // server
    public static void sendAverageCounterValue(TileEntityAverageCounter counter, ICrafting crafter, int average) {
        if (counter == null || !(crafter instanceof EntityPlayerMP)) return;
        ChannelHandler.network.sendTo(
                new PacketAcounter(counter.xCoord, counter.yCoord, counter.zCoord, average),
                (EntityPlayerMP) crafter);
    }

    // server
    private static void sendPacketToAllAround(int x, int y, int z, int dist, World world, IMessage packet) {
        @SuppressWarnings("unchecked")
        List<EntityPlayerMP> players = world.playerEntities;
        for (EntityPlayerMP player : players) {
            double dx = x - player.posX;
            double dy = y - player.posY;
            double dz = z - player.posZ;

            if (dx * dx + dy * dy + dz * dz < dist * dist) ChannelHandler.network.sendTo(packet, player);
        }

    }

    // server
    public static void setSensorCardField(TileEntity panel, byte slot, Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()
                || panel == null
                || !(panel instanceof TileEntityInfoPanel)
                || slot == -1)
            return;

        if (panel.getWorldObj().isRemote) return;

        sendPacketToAllAround(
                panel.xCoord,
                panel.yCoord,
                panel.zCoord,
                64,
                panel.getWorldObj(),
                new PacketSensor(panel.xCoord, panel.yCoord, panel.zCoord, slot, fields));
    }

    // client
    public static void setDisplaySettings(TileEntityInfoPanel panel, byte slot, DisplaySettingHelper settings) {
        if (panel == null) return;

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

        ChannelHandler.network.sendToServer(
                new PacketClientDisplaySettings(panel.xCoord, panel.yCoord, panel.zCoord, slot, settings));
    }

    // client
    public static void setCardSettings(ItemStack card, TileEntity panelTE, Map<String, Object> fields, int slot) {
        if (card == null || fields == null
                || fields.isEmpty()
                || panelTE == null
                || !(panelTE instanceof TileEntityInfoPanel))
            return;

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

        ChannelHandler.network.sendToServer(
                new PacketClientSensor(
                        panelTE.xCoord,
                        panelTE.yCoord,
                        panelTE.zCoord,
                        slot,
                        card.getItem().getClass().getName(),
                        fields));
    }

    // server
    public static void setSensorCardTitle(TileEntityInfoPanel panel, byte slot, String title) {
        if (title == null || panel == null) return;

        sendPacketToAllAround(
                panel.xCoord,
                panel.yCoord,
                panel.zCoord,
                64,
                panel.getWorldObj(),
                new PacketSensorTitle(panel.xCoord, panel.yCoord, panel.zCoord, slot, title));
    }

    public static void chatMessage(EntityPlayer player, String message) {
        if (player instanceof EntityPlayerMP) {
            ChannelHandler.network.sendTo(new PacketChat(message), (EntityPlayerMP) player);
        }
    }

    // client
    public static void setNewAlarmSound(int x, int y, int z, byte slot, String soundName) {
        ChannelHandler.network.sendToServer(new PacketClientSound(x, y, z, slot, soundName));
    }

    // client
    public static void setRangeTrigger(int x, int y, int z, double value, boolean isEnd) {
        ChannelHandler.network.sendToServer(new PacketClientRangeTrigger(x, y, z, value, isEnd));
    }

    // client
    public static void setScreenColor(int x, int y, int z, int back, int text) {
        ChannelHandler.network.sendToServer(new PacketClientColor(x, y, z, back << 4 | text));
    }

    // client
    public static void requestDisplaySettings(TileEntityInfoPanel panel) {
        ChannelHandler.network.sendToServer(new PacketClientRequest(panel.xCoord, panel.yCoord, panel.zCoord));
    }

    // server
    public static void sendDisplaySettingsToPlayer(int x, int y, int z, EntityPlayerMP player) {
        if (!player.worldObj.blockExists(x, y, z)) return;

        TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
        if (!(tileEntity instanceof TileEntityInfoPanel)) return;
        Map<Byte, Map<UUID, DisplaySettingHelper>> settings = ((TileEntityInfoPanel) tileEntity).getDisplaySettings();
        if (settings == null) return;
        ChannelHandler.network.sendTo(new PacketDispSettingsAll(x, y, z, settings), player);
    }

    // server
    public static void sendDisplaySettingsUpdate(TileEntityInfoPanel panel, byte slot, UUID key,
            DisplaySettingHelper value) {
        sendPacketToAllAround(
                panel.xCoord,
                panel.yCoord,
                panel.zCoord,
                64,
                panel.getWorldObj(),
                new PacketDispSettingsUpdate(panel.xCoord, panel.yCoord, panel.zCoord, slot, key, value));
    }

    // client and server
    public static void sendDataSorterSync(TileEntityAdvancedInfoPanel panel) {
        if (panel == null) return;
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            ChannelHandler.network.sendToServer(new PacketDataSorterSync(panel));
        } else if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            sendPacketToAllAround(
                    panel.xCoord,
                    panel.yCoord,
                    panel.zCoord,
                    64,
                    panel.getWorldObj(),
                    new PacketDataSorterSync(panel));
        }
    }

    public static void requestDataSorters(TileEntityInfoPanel panel) {
        ChannelHandler.network.sendToServer(new PacketClientRequest(panel.xCoord, panel.yCoord, panel.zCoord));
    }
}
