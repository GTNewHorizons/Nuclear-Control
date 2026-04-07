package shedar.mods.ic2.nuclearcontrol.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class NBTLayout {

    private final Map<String, Object> cachedData = new HashMap<>();
    private boolean isDirty = true;
    protected IndexedItem<?> item;

    public boolean isDirty() {
        return isDirty;
    }

    public void clearDirty() {
        isDirty = false;
    }

    public void setItem(IndexedItem<?> item) {
        this.item = item;
    }

    public IndexedItem<?> getItem() {
        return item;
    }

    protected DataAccessor<Boolean> boolAccessor(String key) {
        return new DataAccessor<>(this, key, NBTTagCompound::getBoolean, NBTTagCompound::setBoolean, false);
    }

    protected DataAccessor<Double> doubleAccessor(String key) {
        return new DataAccessor<>(this, key, NBTTagCompound::getDouble, NBTTagCompound::setDouble, 0d);
    }

    protected DataAccessor<Integer> intAccessor(String key) {
        return intAccessor(key, 0);
    }

    protected DataAccessor<Integer> intAccessor(String key, int defaultValue) {
        return new DataAccessor<>(this, key, NBTTagCompound::getInteger, NBTTagCompound::setInteger, defaultValue);
    }

    protected DataAccessor<String> stringAccessor(String key, String defaultValue) {
        return new DataAccessor<>(this, key, NBTTagCompound::getString, NBTTagCompound::setString, defaultValue);
    }

    protected DataAccessor<String> stringAccessor(String key) {
        return stringAccessor(key, null);
    }

    protected DataAccessor<NBTBase> tagAccessor(String key) {
        return tagAccessor(key, null);
    }

    protected DataAccessor<NBTBase> tagAccessor(String key, NBTBase defaultValue) {
        BiFunction<NBTTagCompound, String, NBTBase> getter = (compound, string) -> {
            NBTBase tag = compound.getTag(string);
            return tag == null ? defaultValue : tag;
        };
        return new DataAccessor<>(this, key, getter, NBTTagCompound::setTag, defaultValue);
    }

    protected DataAccessor<NBTTagCompound> compoundAccessor(String key) {
        return compoundAccessor(key, null);
    }

    protected DataAccessor<NBTTagCompound> compoundAccessor(String key, NBTTagCompound defaultValue) {
        BiFunction<NBTTagCompound, String, NBTTagCompound> getter = (compound, string) -> {
            NBTTagCompound tag = compound.getCompoundTag(string);
            return tag == null ? defaultValue : tag;
        };
        return new DataAccessor<>(this, key, getter, NBTTagCompound::setTag, defaultValue);
    }

    public static class DataAccessor<T> {

        private final NBTLayout layout;
        private final String key;
        private final BiFunction<NBTTagCompound, String, T> getter;
        private final TriConsumer<NBTTagCompound, String, T> setter;
        private final T defaultValue;

        private DataAccessor(NBTLayout layout, String key, BiFunction<NBTTagCompound, String, T> getter,
                TriConsumer<NBTTagCompound, String, T> setter, T defaultValue) {
            this.layout = layout;
            this.key = key;
            this.getter = getter;
            this.setter = setter;
            this.defaultValue = defaultValue;
        }

        public T get() {
            NBTTagCompound compound = layout.item.getNBT();
            if (compound == null) return defaultValue;
            return getter.apply(compound, key);
        }

        public void set(T value) {
            Object cached = layout.cachedData.get(key);
            if (!Objects.equals(cached, value)) {
                layout.cachedData.put(key, value);
                layout.isDirty = true;
            }

            NBTTagCompound compound = layout.item.getOrCreateNBT();
            setter.consume(compound, key, value);
        }

        public boolean exists() {
            NBTTagCompound compound = layout.item.getNBT();
            return compound != null && compound.hasKey(key);
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + this.key.hashCode();
            hash = hash * 31 + this.get().hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != this.getClass()) return false;
            DataAccessor<?> other = (DataAccessor<?>) obj;
            return this.key.equals(other.key) && this.get().equals(other.get());
        }
    }
}
