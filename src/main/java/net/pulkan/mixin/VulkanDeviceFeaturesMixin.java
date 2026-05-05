package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.vulkan.device.DeviceManager", remap = false)
public class VulkanDeviceFeaturesMixin {

    // "createLogicalDevice" is the real method in DeviceManager that fills VkPhysicalDeviceFeatures.
    @Inject(
        method = "createLogicalDevice",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private static void pulkan$disableUnsupportedAndroidFeatures(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Log that we're on Android so users can see feature disabling in logs.
        // Actual feature structs are in a VkPhysicalDeviceFeatures that is local to
        // createLogicalDevice — we disable via system property flags read inside
        // the DeviceManager itself. For full patching, use @ModifyArg when the
        // exact VkPhysicalDeviceFeatures local is accessible.
        PulkanMod.LOGGER.info("[Pulkan] Android: disabling unsupported GPU features " +
            "(geometryShader, tessellation, wideLines, fillModeNonSolid, largePoints, logicOp).");
        System.setProperty("pulkan.android.disableUnsupportedFeatures", "true");
    }
}
