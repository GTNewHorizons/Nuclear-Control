package shedar.mods.ic2.nuclearcontrol.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import shedar.mods.ic2.nuclearcontrol.api.PanelString;

public class DataSorter {

    private List<Integer> customOrder;

    private Map<String, Integer> prefixOrderCache;
    private List<PanelString> prefixCacheSource;

    /**
     * Default constructor, use only if you don't know the size of the list yet.
     */
    public DataSorter() {
        this.customOrder = new ArrayList<>();
    }

    public DataSorter(int[] order) {
        if (order != null) this.customOrder = Arrays.stream(order).boxed().collect(Collectors.toList());
        else this.customOrder = new ArrayList<>();
    }

    /**
     * Whether the stored order differs from the default identity order. Sorters that were never configured have an
     * empty order, which is a no-op, so the caller can skip sorting entirely.
     */
    public boolean hasCustomOrder() {
        return !customOrder.isEmpty();
    }

    /**
     * Save a custom order, and completely overwrite the one currently stored.
     *
     * @param newOrder the new custom order
     */
    public void saveCustomOrder(List<Integer> newOrder) {
        this.customOrder = new ArrayList<>(newOrder);
        this.prefixOrderCache = null;
    }

    /**
     * Reset the order of the list, whilst keeping the original size.
     */
    public void resetOrder() {
        int size = customOrder.size();
        customOrder = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            customOrder.add(i);
        }
        this.prefixOrderCache = null;
    }

    /**
     * Reset the order and set it to a specific size.
     *
     * @param size size to be set
     */
    public void resetOrder(int size) {
        customOrder = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            customOrder.add(i);
        }
        this.prefixOrderCache = null;
    }

    /**
     * Sort a list that is of equal or greater size to the custom order. If the size of the given list is greater than
     * the stored order, it will not touch any indexes above the stored order's size
     *
     * @param data to be sorted
     * @param <T>  any type
     */
    public <T> void sortList(List<T> data) {
        if (this.customOrder.isEmpty()) {
            this.resetOrder(data.size());
        }

        List<T> reordered = new ArrayList<>(data.size());
        Set<Integer> addedIndices = new HashSet<>();

        for (int index : this.customOrder) {
            if (index >= 0 && index < data.size()) {
                reordered.add(data.get(index));
                addedIndices.add(index);
            }
        }

        // Add the remaining items that weren't in the custom order
        for (int i = 0; i < data.size(); ++i) {
            if (!addedIndices.contains(i)) {
                reordered.add(data.get(i));
            }
        }

        data.clear();
        data.addAll(reordered);
    }

    /**
     * Sort a list based on the custom order using prefix matching. The elements must have a prefix (before ':') that
     * exists in the original list.
     */
    public void sortListByPrefix(List<PanelString> data, List<PanelString> originalList) {
        if (this.customOrder.isEmpty()) {
            this.resetOrder(originalList.size());
        }

        if (prefixOrderCache == null || prefixCacheSource != originalList) {
            // Build prefix → order map
            prefixOrderCache = new HashMap<>();
            for (int i = 0; i < customOrder.size(); i++) {
                int index = customOrder.get(i);
                if (index >= 0 && index < originalList.size()) {
                    PanelString item = originalList.get(index);
                    prefixOrderCache.put(getPrefix(item.textLeft, item.textCenter, item.textRight), i);
                }
            }
            prefixCacheSource = originalList;
        }

        // Compute the custom-order rank of every element once (one prefix extraction per element), then sort
        // stably by rank. Elements whose prefix is not in the cache rank last (Integer.MAX_VALUE).
        Map<PanelString, Integer> ranks = new HashMap<>(data.size() * 2);
        for (PanelString item : data) {
            ranks.put(
                    item,
                    prefixOrderCache.getOrDefault(
                            getPrefix(item.textLeft, item.textCenter, item.textRight),
                            Integer.MAX_VALUE));
        }
        data.sort(Comparator.comparingInt(ranks::get));
    }

    // Helper to extract prefix
    private String getPrefix(String left, String center, String right) {
        int colonIndex = -1;
        String text = left;
        if (text == null || text.isEmpty()) {
            text = center;
        }
        if (text == null || text.isEmpty()) {
            text = right;
        }
        if (text != null) {
            colonIndex = text.indexOf(':');
        }
        return colonIndex == -1 ? text : text.substring(0, colonIndex);
    }

    /**
     * Computes the order needed to sort listB into the order of listA. Both lists must contain the same elements in a
     * different order.
     */
    public <T> void computeSortOrder(List<T> listA, List<T> listB) {
        if (listA.size() != listB.size()) {
            throw new IllegalArgumentException("Lists must be the same size and contain the same elements.");
        }

        Map<T, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < listA.size(); i++) {
            indexMap.put(listA.get(i), i);
        }

        List<Integer> sortOrder = new ArrayList<>();
        for (T item : listB) {
            sortOrder.add(indexMap.get(item));
        }

        this.customOrder = sortOrder;
        this.prefixOrderCache = null;
    }

    public int[] getArray() {
        return this.customOrder.stream().mapToInt(Integer::intValue).toArray();
    }
}
