package shedar.mods.ic2.nuclearcontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;

import cpw.mods.fml.client.FMLClientHandler;

public class SoundHelper {

    private static final float DEFAULT_RANGE = 16F;

    public static boolean playAlarm(ISound sound) {
        float volume = sound.getVolume();
        float range = DEFAULT_RANGE;

        if (volume > 1.0F) {
            range *= volume;
        }

        Entity person = FMLClientHandler.instance().getClient().renderViewEntity;

        if (person != null && volume > 0
                && person.getDistanceSq(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()) < range * range) {
            Minecraft.getMinecraft().getSoundHandler().playSound(sound);
            return true;
        }
        return false;
    }

    public static boolean isPlaying(ISound sound) {
        return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound);
    }

    public static void stopAlarm(ISound sound) {
        Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
    }
}
