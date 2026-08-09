package shedar.mods.ic2.nuclearcontrol.renderers;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;

import cpw.mods.fml.common.Loader;

/**
 * Bridges the info panel text rendering to the Angelica font batcher when it is installed. Every method is a no-op when
 * Angelica is not present.
 */
public final class AngelicaFontBatcher {

    private static final boolean IS_ANGELICA_LOADED = Loader.isModLoaded("angelica");

    private AngelicaFontBatcher() {}

    public static void beginBatch(FontRenderer fontRenderer) {
        if (!IS_ANGELICA_LOADED) {
            return;
        }
        getBatcher(fontRenderer).beginBatch();
    }

    public static void endBatch(FontRenderer fontRenderer) {
        if (!IS_ANGELICA_LOADED) {
            return;
        }
        getBatcher(fontRenderer).endBatch();
    }

    private static BatchingFontRenderer getBatcher(FontRenderer fontRenderer) {
        return ((FontRendererAccessor) fontRenderer).angelica$getBatcher();
    }
}
