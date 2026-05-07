package net.pulkan.platform;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class AndroidEnvironment {

    private static final boolean IS_ANDROID;
    private static final String ARCH;

    static {
        boolean detected = false;

        detected |= System.getenv("ANDROID_ROOT") != null;
        detected |= System.getenv("ANDROID_DATA") != null;
        detected |= System.getenv("ANDROID_BOOTLOGO") != null;

        detected |= System.getProperty("ro.build.version.sdk") != null;

        if (!detected) {
            try {
                detected = Files.exists(Paths.get("/system/build.prop"));
            } catch (Exception ignored) {
            }
        }

        if (!detected) {
            try {
                Class.forName("android.os.Build");
                detected = true;
            } catch (ClassNotFoundException ignored) {
            }
        }

        IS_ANDROID = detected;
        ARCH = System.getProperty("os.arch", "unknown");
    }

    public static boolean isAndroid() { return IS_ANDROID; }

    public static String getArch() { return ARCH; }

    public static boolean isArm64() { return ARCH.equals("aarch64"); }

    public static boolean isArm32() { return ARCH.equals("arm") || ARCH.startsWith("armv7"); }

    public static boolean isX86_64() { return ARCH.equals("amd64") || ARCH.equals("x86_64"); }

    public static String getVulkanVersion() {
        try {
            String prop = Files.readString(Paths.get("/system/build.prop"));
            for (String line : prop.split("\\n")) {
                if (line.startsWith("ro.build.version.release=")) {
                    return line.split("=", 2)[1].trim();
                }
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }

    private AndroidEnvironment() {
    }
}
