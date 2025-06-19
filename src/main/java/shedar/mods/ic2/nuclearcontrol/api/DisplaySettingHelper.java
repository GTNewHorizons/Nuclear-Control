package shedar.mods.ic2.nuclearcontrol.api;

import io.netty.buffer.ByteBuf;

public class DisplaySettingHelper {

    private String settings = "0";

    private boolean all_true = false;

    public DisplaySettingHelper() {}

    public DisplaySettingHelper(boolean all_true) {
        this.all_true = all_true;
    }

    public DisplaySettingHelper(String settings) {
        this.settings = sanitizeSettings(settings);
    }

    /**
     * @param legacySettings Settings
     * @deprecated use any other constructor, and don't use an int to store display settings
     */
    public DisplaySettingHelper(int legacySettings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 31; i >= 0; i--) {
            sb.append(((legacySettings >>> i) & 1) == 1 ? "1" : "0");
        }
        this.settings = sb.reverse().toString();
    }

    public DisplaySettingHelper(ByteBuf buf) {
        short length = buf.readShort();
        int bytesToRead = (length + 7) / 8;

        StringBuilder sb = new StringBuilder(bytesToRead * 8);
        for (int i = 0; i < bytesToRead; i++) {
            byte value = buf.readByte();
            sb.append(String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0'));
        }

        // Only keep the first `length` bits (remove extra right-padding)
        this.settings = sb.substring(0, length);

    }

    public DisplaySettingHelper(DisplaySettingHelper helper) {
        this.settings = helper.settings;
        this.all_true = helper.all_true;
    }

    /**
     * get the current state of the setting at the given bitMask
     *
     * @param bitMask bitMask of the setting
     * @return value of the setting
     */
    public boolean getSetting(int bitMask) {
        return getNewSetting(bitMaskToIndex(bitMask));
    }

    /**
     * get the current state of the setting at the given index.
     *
     * @param index of the setting
     * @return value of the setting
     */
    public boolean getNewSetting(int index) {
        if (all_true) return true;
        if (index >= 0 && index < settings.length()) {
            return settings.charAt(index) == '1';
        }
        return false;
    }

    /**
     * add a setting to this DisplaySettingHelper
     *
     * @param value current status
     * @return index
     */
    public int addSetting(boolean value) {
        settings += value ? '1' : '0';
        return settings.length() - 1;
    }

    /**
     * @return the settings as an integer. does not support more than 32 options
     * @deprecated do not use.
     */
    public int getAsInteger() {
        if (all_true) return Integer.MAX_VALUE;
        String s = new StringBuilder(settings.substring(0, Math.min(31, settings.length()))).reverse().toString();
        return Integer.parseInt(s, 2);
    }

    /**
     * Write the current settings to the given ByteBuf
     *
     * @param buf ByteBuf to write to
     */
    public void writeToByteBuffer(ByteBuf buf) {
        if (!settings.matches("[01]+")) {
            settings = sanitizeSettings(settings);
        }
        // Write the actual bit length first
        buf.writeShort(settings.length());

        // Pad to the right (end) so the length becomes a multiple of 8
        int remainder = settings.length() % 8;
        if (remainder != 0) {
            int padding = 8 - remainder;
            StringBuilder sb = new StringBuilder(settings.length() + padding);
            sb.append(settings);
            for (int i = 0; i < padding; i++) sb.append('0');
            settings = sb.toString();
        }

        // Now write 8 bits at a time
        for (int i = 0; i < settings.length(); i += 8) {
            String byteString = settings.substring(i, i + 8);
            byte value = (byte) Integer.parseInt(byteString, 2);
            buf.writeByte(value);
        }
    }

    @Override
    public String toString() {
        return settings;
    }

    private void setSetting(int index, boolean value) {
        StringBuilder sb = new StringBuilder(settings);
        if (settings.length() > index) {
            sb.setCharAt(index, value ? '1' : '0');
        } else {
            while (sb.length() < index) {
                sb.append('0');
            }
            sb.append(value ? '1' : '0');

        }
        settings = sb.toString();
    }

    public void toggleSetting(int index) {
        setSetting(index, !getNewSetting(index));
    }

    public static int indexToBitMask(int value) {
        return 1 << value;
    }

    public static int bitMaskToIndex(int value) {
        return Integer.numberOfTrailingZeros(value);
    }

    private String sanitizeSettings(String input) {
        if (input == null || input.isEmpty()) {
            return "0";
        }

        if (input.matches("[01]+")) {
            return input;
        }

        try {
            int legacy = Integer.parseInt(input);
            StringBuilder sb = new StringBuilder();
            for (int i = 31; i >= 0; i--) {
                sb.append(((legacy >>> i) & 1) == 1 ? "1" : "0");
            }
            return sb.reverse().toString();
        } catch (NumberFormatException e) {
            return "0";
        }
    }
}
