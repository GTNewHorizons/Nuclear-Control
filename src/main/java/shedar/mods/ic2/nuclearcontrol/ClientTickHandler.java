/**
 * 
 * @author Zuxelus (I copied him)
 * 
 */
package shedar.mods.ic2.nuclearcontrol;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.SoundListSerializer;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.world.WorldEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntitySound;

public class ClientTickHandler {

    public final static ClientTickHandler instance = new ClientTickHandler();
    private static final Gson gson = (new GsonBuilder()).registerTypeAdapter(SoundList.class, new SoundListSerializer())
            .create();
    private static final ParameterizedType type = new ParameterizedType() {

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] { String.class, SoundList.class };
        }

        @Override
        public Type getRawType() {
            return Map.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    @SubscribeEvent
    public void importSound(SoundLoadEvent event) {
        TileEntitySound.clear();
        IC2NuclearControl ncInstance = IC2NuclearControl.instance;
        ncInstance.availableAlarms = new ArrayList<String>();

        try {
            List list = Minecraft.getMinecraft().getResourceManager()
                    .getAllResources(new ResourceLocation("nuclearcontrol", "sounds.json"));

            for (int i = list.size() - 1; i >= 0; --i) {
                IResource iresource = (IResource) list.get(i);

                try {
                    Map map = (Map) gson.fromJson(new InputStreamReader(iresource.getInputStream()), type);
                    Iterator iterator1 = map.entrySet().iterator();

                    while (iterator1.hasNext()) {
                        Entry entry = (Entry) iterator1.next();
                        if (entry.getKey().toString().startsWith("alarm-")) {
                            String cleanedUpName = entry.getKey().toString().replace("alarm-", "");
                            if (ncInstance.availableAlarms.contains(cleanedUpName)) {
                                IC2NuclearControl.logger
                                        .warn("Alarm '%s' already available. Skipping inclusion.", cleanedUpName);
                            } else {
                                ncInstance.availableAlarms.add(cleanedUpName);
                            }
                        }
                    }
                } catch (RuntimeException runtimeexception) {
                    ;
                }
            }
        } catch (IOException ioexception) {
            ;
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        IC2NuclearControl.instance.serverAllowedAlarms = Collections.emptyList();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        IC2NuclearControl.instance.serverAllowedAlarms = Collections.emptyList();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused()) {
            TileEntitySound.tick();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world.isRemote) TileEntitySound.clear();
    }
}
