package shedar.mods.ic2.nuclearcontrol.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import shedar.mods.ic2.nuclearcontrol.api.PanelString;

public class DataSorter {

    private List<Integer> customOrder;

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
     * Save a custom order, and completely overwrite the one currently stored.
     *
     * @param newOrder the new custom order
     */
    public void saveCustomOrder(List<Integer> newOrder) {
        this.customOrder = new ArrayList<>(newOrder);
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

        // Build prefix → order map
        Map<String, Integer> prefixOrderMap = new HashMap<>();
        for (int i = 0; i < customOrder.size(); i++) {
            int index = customOrder.get(i);
            if (index >= 0 && index < originalList.size()) {
                PanelString item = originalList.get(index);
                String prefix = getPrefix(item.toString());
                prefixOrderMap.put(prefix, i);
            }
        }

        // Sort based on prefix order
        data.sort((a, b) -> {
            int aIndex = prefixOrderMap.getOrDefault(getPrefix(a.toString()), Integer.MAX_VALUE);
            int bIndex = prefixOrderMap.getOrDefault(getPrefix(b.toString()), Integer.MAX_VALUE);
            return Integer.compare(aIndex, bIndex);
        });

    }

    // Helper to extract prefix
    private String getPrefix(String s) {
        int colonIndex = s.indexOf(':');
        return colonIndex == -1 ? s : s.substring(0, colonIndex);
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
    }

    public int[] getArray() {
        return this.customOrder.stream().mapToInt(Integer::intValue).toArray();
    }
}
