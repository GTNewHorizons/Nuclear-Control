package shedar.mods.ic2.nuclearcontrol.api;

import net.minecraft.util.ChunkCoordinates;

public class NBTCardLayout extends NBTLayout {

    private final DataAccessor<Integer> chunkX = intAccessor("x");
    private final DataAccessor<Integer> chunkY = intAccessor("y");
    private final DataAccessor<Integer> chunkZ = intAccessor("z");
    private final DataAccessor<Integer> state = intAccessor("state", 1);
    public final DataAccessor<String> title = stringAccessor("title", "");

    public CardState getState() {
        return CardState.fromInteger(state.get());
    }

    public void setState(CardState state) {
        this.state.set(state.getIndex());
    }

    public ChunkCoordinates getTarget() {
        if (item.getNBT() == null) return null;
        return new ChunkCoordinates(chunkX.get(), chunkY.get(), chunkZ.get());
    }

    public void setTarget(ChunkCoordinates coords) {
        chunkX.set(coords.posX);
        chunkY.set(coords.posY);
        chunkZ.set(coords.posZ);
    }
}
