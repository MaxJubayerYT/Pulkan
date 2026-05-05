package net.pulkan.platform;

import net.pulkan.PulkanMod;
import net.pulkan.android.PulkanSwapChain;

import java.io.*;
import java.nio.file.*;

public final class NativeLoader {

    private static final String[] LIBS = {
        "libshaderc.so",
        "liblwjgl_vma.so",
        "liblwjgl.so",
        "liblwjgl_stb.so",
        "liblwjgl_glfw.so",
        "liblwjgl_tinyfd.so"
    };

    public static void loadIfNeeded() {
        if (!AndroidEnvironment.isAndroid()) return;

        String arch = mapArch(AndroidEnvironment.getArch());
        PulkanMod.LOGGER.info("[Pulkan] Loading natives for arch: {} (raw os.arch={})",
            arch, AndroidEnvironment.getArch());

        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "pulkan-natives");
        try {
            Files.createDirectories(tmpDir);
        } catch (IOException e) {
            PulkanMod.LOGGER.error("[Pulkan] Cannot create native temp dir: {}", e.getMessage());
            return;
        }

        for (String lib : LIBS) {
            String folder = libFolder(lib);
            // Path inside the JAR as embedded resource
            String resource = "/linux/" + arch + "/org/lwjgl"
                + (folder.isEmpty() ? "" : "/" + folder)
                + "/" + lib;

            Path out = tmpDir.resolve(lib);
            if (!Files.exists(out)) {
                try (InputStream in = NativeLoader.class.getResourceAsStream(resource)) {
                    if (in == null) {
                        PulkanMod.LOGGER.warn("[Pulkan] Native not found in JAR: {}", resource);
                        continue;
                    }
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    PulkanMod.LOGGER.warn("[Pulkan] Failed to extract {}: {}", lib, e.getMessage());
                    continue;
                }
            }

            try {
                System.load(out.toAbsolutePath().toString());
                PulkanMod.LOGGER.debug("[Pulkan] Loaded: {}", lib);
            } catch (UnsatisfiedLinkError e) {
                PulkanMod.LOGGER.warn("[Pulkan] Could not load {}: {}", lib, e.getMessage());
            }
        }

        // Apply Android-specific Vulkan tuning after natives are loaded
        applyAndroidVulkanConfig();
    }

    /**
     * Sets system properties that mixins and VulkanMod read to tune their behaviour
     * for Android GPU constraints. Called after natives are confirmed loaded.
     */
    private static void applyAndroidVulkanConfig() {
        // Tell VMA to use smaller heap blocks (64 MB vs desktop 256 MB)
        System.setProperty("pulkan.vma.blockSize", "67108864"); // 64 MB
        // Prefer RGBA surface format over BGRA (Exynos/some MediaTek don't support BGRA)
        System.setProperty("pulkan.preferRGBA", "true");
        // Prefer FIFO present mode (battery-friendly)
        System.setProperty("pulkan.preferFifo", "true");
        // Clamp push constants to 128 bytes (Adreno 505/506 limit)
        System.setProperty("pulkan.pushConstantMaxSize", "128");

        // Probe for single-family GPU (Adreno 7xx exposes only 1 queue family)
        // This is read by QueueFamilyMixin
        // We default to false — set to true if we detect relevant GPU at runtime
        System.setProperty("pulkan.singleQueueFamily", "false");

        // Initialize swapchain state with safe defaults
        PulkanSwapChain.ensureInitialized();

        PulkanMod.LOGGER.info("[Pulkan] Android Vulkan config applied.");
    }

    private static String mapArch(String arch) {
        return switch (arch) {
            case "aarch64"                    -> "arm64";
            case "arm", "armv7l", "armv8l"   -> "arm32";
            case "i686", "x86"               -> "x86";
            default                           -> "x86_64";
        };
    }

    /**
     * Returns the LWJGL module subfolder for a given .so filename.
     * Empty string = root (liblwjgl.so lives at linux/{arch}/org/lwjgl/liblwjgl.so)
     */
    private static String libFolder(String lib) {
        if (lib.contains("shaderc"))  return "shaderc";
        if (lib.contains("vma"))      return "vma";
        if (lib.contains("stb"))      return "stb";
        if (lib.contains("glfw"))     return "glfw";
        if (lib.contains("tinyfd"))   return "tinyfd";
        return ""; // liblwjgl.so at root
    }

    private NativeLoader() {}
}
