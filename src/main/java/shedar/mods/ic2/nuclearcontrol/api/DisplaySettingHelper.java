package shedar.mods.ic2.nuclearcontrol.api;

import java.util.BitSet;

import io.netty.buffer.ByteBuf;

/**
 * Helper class for display settings. This used to be done by an integer bitmask, but that was limited to 32 settings.
 * This features an unlimited* amount of settings (within reason of course).
 *
 * @author Guid118
 */
public class DisplaySettingHelper {

    private final BitSet bits;
    private int length;
    private boolean all_true = false;

    public DisplaySettingHelper() {
        this.bits = new BitSet();
        this.length = 0;
    }

    public DisplaySettingHelper(boolean all_true) {
        this();
        this.all_true = all_true;
    }

    public DisplaySettingHelper(String bitString) {
        this.bits = new BitSet();
        this.length = bitString.length();
        for (int i = 0; i < length; i++) {
            char c = bitString.charAt(i);
            if (c == '1') bits.set(i, true);
        }
    }

    /**
     * @param legacySettings Settings
     * @deprecated use any other constructor, and don't use an int to store display settings
     */
    @Deprecated
    public DisplaySettingHelper(int legacySettings) {
        this.bits = new BitSet();
        this.length = 0;
        for (int i = 0; i < 32; i++) {
            boolean value = ((legacySettings >>> i) & 1) == 1;
            addSetting(value);
        }
    }

    /**
     * Constructor for reading from the ByteBuf used in packets.
     * 
     * @param buf packet's ByteBuf
     */
    public DisplaySettingHelper(ByteBuf buf) {
        this.length = buf.readShort();
        int bytesToRead = (length + 7) / 8;
        this.bits = new BitSet(length);

        for (int i = 0; i < bytesToRead; i++) {
            byte value = buf.readByte();
            for (int bit = 0; bit < 8; bit++) {
                int index = i * 8 + bit;
                if (index >= length) break;
                boolean bitSet = ((value >>> (7 - bit)) & 1) == 1;
                bits.set(index, bitSet);
            }
        }
    }

    /**
     * Deep copy constructor
     * 
     * @param origin original instance to deep copy
     */
    public DisplaySettingHelper(DisplaySettingHelper origin) {
        this.bits = (BitSet) origin.bits.clone();
        this.length = origin.length;
        this.all_true = origin.all_true;
    }

    /**
     * get a setting's value from the index.
     * 
     * @param index index
     * @return value
     */
    public boolean getSetting(int index) {
        if (all_true) return true;
        return get(index);
    }

    /**
     * add a value to the end of the settings, without giving an index.
     * 
     * @param value to set
     */
    public void addSetting(boolean value) {
        bits.set(length, value);
        length++;
    }

    /**
     * Get the first 32 bits of the settings.
     * 
     * @deprecated Don't use an integer to store or send settings. Will be removed in 3.0.0
     * @return integer representation of the first 32 bits.
     */
    @Deprecated
    public int getAsInteger() {
        if (all_true) return Integer.MAX_VALUE;

        int result = 0;
        int limit = Math.min(31, size());
        for (int i = 0; i < limit; i++) {
            if (get(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }

    /**
     * toggle the setting at the given index
     * 
     * @param index index of the to be toggled setting
     */
    public void toggleSetting(int index) {
        set(index, !get(index));
    }

    /**
     * convert an index to a bitmask
     * 
     * @param index index to set the bitmask to
     * @return bitmask with a 1 at the given index
     */
    public static int indexToBitMask(int index) {
        return 1 << index;
    }

    /**
     * get the index of the right-most 1 in a bitmask
     * 
     * @param value bitmask
     * @return index of the right-most 1
     */
    public static int bitMaskToIndex(int value) {
        return Integer.numberOfTrailingZeros(value);
    }

    /**
     * get the value at the given index.
     * 
     * @param index the index of the value to be retrieved, throws IndexOutOfBoundsException when index < 0
     * @return value at the given index
     */
    public boolean get(int index) {
        return bits.get(index);
    }

    /**
     * set the value of the given index to the given value.
     * 
     * @param index index to be used, should be non-negative
     * @param value value the index should be set to.
     */
    public void set(int index, boolean value) {
        if (index >= length) {
            length = index + 1;
        }
        bits.set(index, value);
    }

    /**
     * get the size of the list of settings.
     * 
     * @return the last bit ever set. includes trailing zeros.
     */
    public int size() {
        return length;
    }

    /**
     * write to the given ByteBuf.
     * 
     * @param buf ByteBuf to write to
     */
    public void writeToByteBuffer(ByteBuf buf) {
        buf.writeShort(length);
        int bytesToWrite = (length + 7) / 8;

        for (int i = 0; i < bytesToWrite; i++) {
            byte b = 0;
            for (int bit = 0; bit < 8; bit++) {
                int index = i * 8 + bit;
                if (index >= length) break;
                if (bits.get(index)) {
                    b |= (byte) (1 << (7 - bit));
                }
            }
            buf.writeByte(b);
        }
    }

    /**
     * write to a string, for NBT storage purposes
     * 
     * @return string representation
     */
    public String toBitString() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(bits.get(i) ? '1' : '0');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toBitString() + "\nAll true:" + this.all_true + ", length: " + this.length;
    }
}
