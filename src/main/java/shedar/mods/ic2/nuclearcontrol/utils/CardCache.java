package shedar.mods.ic2.nuclearcontrol.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.bsideup.jabel.Desugar;

import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.PanelContent;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.api.NBTLayout;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardBase;

public class CardCache {

    private final Map<Integer, CardCacheEntry> cachedCards = new HashMap<>();
    /** Slots whose next dirty evaluation should refresh the display but NOT send an outgoing packet. */
    private final Set<Integer> externalSyncs = new HashSet<>();
    private List<PanelString> displayStrings = new LinkedList<>();

    private Integer textWidth;
    private boolean isDirty = false;

    public List<PanelString> getStrings(IndexedItem<?> card) {
        CardCacheEntry entry = cachedCards.get(card.slot);
        if (entry == null) return Collections.emptyList();
        return entry.content.getLines();
    }

    public List<PanelString> getCachedStrings() {
        if (isDirty) {
            this.isDirty = false;
            displayStrings.clear();
            for (CardCacheEntry entry : cachedCards.values()) {
                displayStrings.addAll(entry.content.getLines());
            }
        }
        return displayStrings;
    }

    public NBTCardLayout getLayout(IndexedItem<IPanelDataSource> card) {
        CardCacheEntry entry = cachedCards.computeIfAbsent(
                card.slot,
                _i -> new CardCacheEntry(new PanelContent(Collections.emptyList()), card.item.getLayout()));
        NBTCardLayout layout = (NBTCardLayout) entry.layout;
        layout.setItem(card);
        return layout;
    }

    /**
     * Marks a slot as externally synced (received from server). The next {@link #update} call will recompute display
     * strings but return {@code false} so no outgoing packet is sent.
     */
    public void markExternalSync(int slot) {
        externalSyncs.add(slot);
        clear(slot);
    }

    /**
     * Returns {@code true} if the card data changed and an outgoing packet should be sent. Returns {@code false} if
     * unchanged, or if the change came from an external server sync.
     */
    public boolean update(IndexedItem<IPanelDataSource> card,
            Function<IndexedItem<IPanelDataSource>, List<PanelString>> getStringData) {
        boolean isExternal = externalSyncs.remove(card.slot);
        CardCacheEntry entry = cachedCards.computeIfAbsent(
                card.slot,
                _i -> new CardCacheEntry(new PanelContent(Collections.emptyList()), card.item.getLayout()));
        NBTLayout layout = entry.layout;
        boolean isDirty = layout.isDirty();
        layout.clearDirty();
        if (!isDirty && !isExternal) return false;

        markDirty();
        List<PanelString> strings = getStringData.apply(card);
        PanelContent content = new PanelContent(strings);
        cachedCards.put(card.slot, new CardCacheEntry(content, layout));
        return !isExternal; // local change → send packet; external sync → display only
    }

    public void clear(int slot, boolean triggerDirty) {
        cachedCards.remove(slot);
        if (triggerDirty) markDirty();
    }

    public void clear(int slot) {
        clear(slot, false);
    }

    public Integer getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(int width) {
        this.textWidth = width;
    }

    private void markDirty() {
        this.isDirty = true;
        textWidth = null;
    }

    @Desugar
    private record CardCacheEntry(PanelContent content, NBTLayout layout) {

    }
}
