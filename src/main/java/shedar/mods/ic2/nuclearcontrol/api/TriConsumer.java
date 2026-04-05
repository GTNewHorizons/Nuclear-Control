package shedar.mods.ic2.nuclearcontrol.api;

@FunctionalInterface
public interface TriConsumer<T, U, V> {
    void consume(T t, U u, V v);
}
