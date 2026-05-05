package net.pulkan.android;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.joml.Matrix4f;

import static org.lwjgl.vulkan.KHRSurface.*;

public final class PulkanSwapChain {

    private static int currentTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
    private static boolean hasPreRotation = false;
    private static volatile boolean subOptimal = false;
    private static volatile boolean needsRecreate = false;
    private static int displayWidth = 1280;
    private static int displayHeight = 720;
    private static boolean initialized = false;

    private static final Matrix4f PRE_ROTATION_MAT = new Matrix4f().identity();

    /**
     * Called from NativeLoader after vkGetPhysicalDeviceSurfaceCapabilitiesKHR
     * to cache the device's currentTransform and build the pre-rotation matrix.
     */
    public static void setupTransform(int transform, int surfaceWidth, int surfaceHeight) {
        if (!AndroidEnvironment.isAndroid()) return;

        currentTransform = transform;
        hasPreRotation = transform != VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
        displayWidth  = surfaceWidth  > 0 ? surfaceWidth  : displayWidth;
        displayHeight = surfaceHeight > 0 ? surfaceHeight : displayHeight;
        initialized = true;

        PRE_ROTATION_MAT.identity();
        switch (currentTransform) {
            case VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR  -> PRE_ROTATION_MAT.rotateZ((float) Math.toRadians(90));
            case VK_SURFACE_TRANSFORM_ROTATE_180_BIT_KHR -> PRE_ROTATION_MAT.rotateZ((float) Math.toRadians(180));
            case VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR -> PRE_ROTATION_MAT.rotateZ((float) Math.toRadians(270));
        }

        PulkanMod.LOGGER.info("[Pulkan] SwapChain transform: 0x{} | preRotation={} | display={}x{}",
            Integer.toHexString(currentTransform), hasPreRotation, displayWidth, displayHeight);
    }

    /**
     * Called from SwapChainCreationMixin to ensure state is ready before createSwapChain runs.
     */
    public static void ensureInitialized() {
        if (!initialized) {
            // Default to identity if setupTransform was not called yet
            PulkanMod.LOGGER.warn("[Pulkan] PulkanSwapChain not yet initialized — using identity transform.");
            currentTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
            hasPreRotation = false;
            PRE_ROTATION_MAT.identity();
            initialized = true;
        }
    }

    /**
     * Called from RendererFrameMixin after VK_SUBOPTIMAL_KHR to trigger recreation on next frame.
     */
    public static void recreateIfNeeded() {
        if (!subOptimal) return;
        subOptimal = false;
        needsRecreate = true;
        PulkanMod.LOGGER.info("[Pulkan] Swapchain suboptimal — flagging for recreation on next frame.");
        // VulkanMod checks Renderer.swapchainRecreationRequested or similar.
        // We set a system property as a signal channel since we can't access private fields here.
        System.setProperty("pulkan.swapchain.recreate", "true");
    }

    public static int     getCurrentTransform()       { return currentTransform; }
    public static boolean hasPreRotation()            { return hasPreRotation; }
    public static Matrix4f getPreRotationMatrix()     { return PRE_ROTATION_MAT; }
    public static boolean isSubOptimal()              { return subOptimal; }
    public static void    setSubOptimal(boolean v)    { subOptimal = v; if (v) needsRecreate = true; }
    public static boolean needsRecreate()             { return needsRecreate; }
    public static void    clearRecreate()             { needsRecreate = false; }
    public static int     getDisplayWidth()           { return displayWidth; }
    public static int     getDisplayHeight()          { return displayHeight; }

    /**
     * Returns corrected [width, height] swapped for 90° and 270° rotations.
     */
    public static int[] getTransformExtent(int w, int h) {
        boolean rotated = currentTransform == VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR
                       || currentTransform == VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR;
        return rotated ? new int[]{h, w} : new int[]{w, h};
    }

    private PulkanSwapChain() {}
}
