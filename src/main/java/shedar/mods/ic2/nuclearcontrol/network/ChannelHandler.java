package shedar.mods.ic2.nuclearcontrol.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketAcounter;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketAlarm;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketChat;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientColor;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientDisplaySettings;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientRangeTrigger;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientRemoteMonitor;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientRequest;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientSensor;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketClientSound;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketDataSorter;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketDataSorterSync;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketDispSettingsAll;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketDispSettingsUpdate;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketEncounter;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketSensor;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketSensorTitle;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketServerUpdate;

public class ChannelHandler {

    public static SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel("IC2NC");

    public static void init() {
        network.registerMessage(PacketAlarm.class, PacketAlarm.class, 1, Side.CLIENT);
        network.registerMessage(PacketSensor.class, PacketSensor.class, 2, Side.CLIENT);
        network.registerMessage(PacketSensorTitle.class, PacketSensorTitle.class, 3, Side.CLIENT);
        network.registerMessage(PacketChat.class, PacketChat.class, 4, Side.CLIENT);
        network.registerMessage(PacketEncounter.class, PacketEncounter.class, 5, Side.CLIENT);
        network.registerMessage(PacketAcounter.class, PacketAcounter.class, 6, Side.CLIENT);
        network.registerMessage(PacketClientSound.class, PacketClientSound.class, 7, Side.SERVER);
        network.registerMessage(PacketClientRequest.class, PacketClientRequest.class, 8, Side.SERVER);
        network.registerMessage(PacketDispSettingsAll.class, PacketDispSettingsAll.class, 9, Side.CLIENT);
        network.registerMessage(PacketDispSettingsUpdate.class, PacketDispSettingsUpdate.class, 10, Side.CLIENT);
        network.registerMessage(PacketClientRangeTrigger.class, PacketClientRangeTrigger.class, 11, Side.SERVER);
        network.registerMessage(PacketClientSensor.class, PacketClientSensor.class, 12, Side.SERVER);
        network.registerMessage(PacketClientColor.class, PacketClientColor.class, 13, Side.SERVER);
        network.registerMessage(PacketClientDisplaySettings.class, PacketClientDisplaySettings.class, 14, Side.SERVER);
        network.registerMessage(PacketServerUpdate.Handler.class, PacketServerUpdate.class, 15, Side.SERVER);
        network.registerMessage(
                PacketClientRemoteMonitor.Handler.class,
                PacketClientRemoteMonitor.class,
                16,
                Side.CLIENT);
        network.registerMessage(PacketDataSorter.class, PacketDataSorter.class, 17, Side.CLIENT);
        network.registerMessage(PacketDataSorter.class, PacketDataSorter.class, 18, Side.SERVER);
        network.registerMessage(PacketDataSorterSync.class, PacketDataSorterSync.class, 19, Side.CLIENT);
        network.registerMessage(PacketDataSorterSync.class, PacketDataSorterSync.class, 20, Side.SERVER);
    }
}
