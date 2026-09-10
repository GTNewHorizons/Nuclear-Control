package shedar.mods.ic2.nuclearcontrol.tileentities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import shedar.mods.ic2.nuclearcontrol.SoundHelper;

/** Client-owned one-shot playback, retained across tile unloads until the clip finishes. */
public class TileEntitySound extends PositionedSoundRecord implements ITickableSound {

    private static final Map<ChunkCoordinates, TileEntitySound> sounds = new HashMap<>();
    private static World world;

    private final ChunkCoordinates position;
    private final String alarmName;
    private TileEntityHowlerAlarm owner;
    private boolean muted;
    private boolean done;
    private int startupTicks = 20;

    private TileEntitySound(TileEntityHowlerAlarm alarm, String name, float range) {
        super(new ResourceLocation(name), range, 1.0F, alarm.xCoord + 0.5F, alarm.yCoord + 0.5F, alarm.zCoord + 0.5F);
        position = positionOf(alarm);
        alarmName = alarm.soundName;
        owner = alarm;
    }

    private static ChunkCoordinates positionOf(TileEntityHowlerAlarm alarm) {
        return new ChunkCoordinates(alarm.xCoord, alarm.yCoord, alarm.zCoord);
    }

    public static void updateAlarm(TileEntityHowlerAlarm alarm, String name, float range) {
        if (world != alarm.getWorldObj()) {
            clear();
            world = alarm.getWorldObj();
        }
        if (!alarm.isAlarmSoundReady()) return;

        ChunkCoordinates position = positionOf(alarm);
        TileEntitySound sound = sounds.get(position);
        if (sound != null && (sound.done || !alarm.powered
                || !sound.alarmName.equals(alarm.soundName)
                || sound.volume != range)) {
            sound.stop();
            sounds.remove(position);
            sound = null;
        }
        if (!alarm.powered) return;

        if (sound != null) {
            sound.owner = alarm;
            sound.muted = false;
        } else {
            sound = new TileEntitySound(alarm, name, range);
            if (SoundHelper.playAlarm(sound)) sounds.put(position, sound);
        }
    }

    public static void unloadAlarm(TileEntityHowlerAlarm alarm) {
        TileEntitySound sound = sounds.get(positionOf(alarm));
        if (sound != null && sound.owner == alarm) {
            sound.owner = null;
            sound.muted = true;
        }
    }

    public static void stopAlarm(TileEntityHowlerAlarm alarm) {
        TileEntitySound sound = sounds.get(positionOf(alarm));
        if (sound != null && sound.owner == alarm) {
            sound.stop();
            sounds.remove(sound.position);
        }
    }

    public static void tick() {
        if (world != Minecraft.getMinecraft().theWorld) {
            clear();
            return;
        }
        Iterator<TileEntitySound> iterator = sounds.values().iterator();
        while (iterator.hasNext()) {
            TileEntitySound sound = iterator.next();
            sound.update();
            // Minecraft starts sources asynchronously; do not mistake startup for completion.
            boolean playing = SoundHelper.isPlaying(sound);
            if (playing) sound.startupTicks = 0;
            else if (sound.startupTicks > 0) sound.startupTicks--;
            if (sound.done || sound.startupTicks == 0 && !playing) {
                sound.stop();
                iterator.remove();
            }
        }
    }

    public static void clear() {
        for (TileEntitySound sound : sounds.values()) sound.stop();
        sounds.clear();
        world = null;
    }

    private void stop() {
        done = true;
        muted = true;
        owner = null;
        SoundHelper.stopAlarm(this);
    }

    @Override
    public void update() {
        if (done) return;
        muted = true;
        if (world == null || world != Minecraft.getMinecraft().theWorld) {
            done = true;
            return;
        }
        // Client chunkExists() also returns true for missing chunks; check the actual chunk instead.
        if (!world.getChunkFromChunkCoords(position.posX >> 4, position.posZ >> 4).isChunkLoaded) {
            owner = null;
            return;
        }
        TileEntity tile = world.getTileEntity(position.posX, position.posY, position.posZ);
        if (!(tile instanceof TileEntityHowlerAlarm) || tile.isInvalid()) {
            done = true;
            return;
        }
        TileEntityHowlerAlarm alarm = (TileEntityHowlerAlarm) tile;
        if (!alarm.isAlarmSoundReady()) return;
        if (!alarm.powered || !alarmName.equals(alarm.soundName)) {
            done = true;
            return;
        }
        owner = alarm;
        muted = false;
    }

    @Override
    public float getVolume() {
        return muted ? 0.0F : super.getVolume();
    }

    @Override
    public boolean isDonePlaying() {
        return done;
    }
}
