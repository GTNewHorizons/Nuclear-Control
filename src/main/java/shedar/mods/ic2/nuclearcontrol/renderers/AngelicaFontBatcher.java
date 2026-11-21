package shedar.mods.ic2.nuclearcontrol.renderers;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;

import cpw.mods.fml.common.Loader;

/**
 * Bridges the info panel text rendering to the Angelica font batcher when it is installed. Every method is a no-op when
 * Angelica is not present, its font renderer mixin is disabled, or the current FontRenderer is not mixed in.
 */
public final class AngelicaFontBatcher {

    private static final boolean IS_ANGELICA_LOADED = Loader.isModLoaded("angelica");

    private AngelicaFontBatcher() {}

    public static void beginBatch(FontRenderer fontRenderer) {
        BatchingFontRenderer batcher = getBatcher(fontRenderer);
        if (batcher != null) {
            batcher.beginBatch();
        }
    }

    public static void endBatch(FontRenderer fontRenderer) {
        BatchingFontRenderer batcher = getBatcher(fontRenderer);
        if (batcher != null) {
            batcher.endBatch();
        }
    }

    private static BatchingFontRenderer getBatcher(FontRenderer fontRenderer) {
        if (IS_ANGELICA_LOADED && fontRenderer instanceof FontRendererAccessor accessor) {
            return accessor.angelica$getBatcher();
        }
        return null;
    }
}
