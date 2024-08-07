package suzutsuki.util;

import java.text.DecimalFormat;

public class Memory {
    private static final long BYTE = 1L;
    private static final long KB = BYTE * 1000;
    private static final long MB = KB * 1000;
    private static final long GB = MB * 1000;
    private static final long TB = GB * 1000;
    private static final long PB = TB * 1000;
    private static final long EB = PB * 1000;
    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("#.##");

    private static String formatSize(long size, long divider, String unitName) {
        return Memory.DEC_FORMAT.format((double) size / divider) + " " + unitName;
    }

    public static String toHumanReadableSIPrefixes(long size) {
        if (size < 0)
            throw new IllegalArgumentException("Invalid file size: " + size);
        if (size >= EB) return Memory.formatSize(size, EB, "EB");
        if (size >= PB) return Memory.formatSize(size, PB, "PB");
        if (size >= TB) return Memory.formatSize(size, TB, "TB");
        if (size >= GB) return Memory.formatSize(size, GB, "GB");
        if (size >= MB) return Memory.formatSize(size, MB, "MB");
        if (size >= KB) return Memory.formatSize(size, KB, "KB");
        return Memory.formatSize(size, BYTE, "Bytes");
    }
}
