package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.vulkan.device.DeviceManager", remap = false)
public class PhysicalDeviceSelectorMixin {

    @Inject(
        method = "pickPhysicalDevice",
        at = @At("HEAD"),
        cancellable = true,  // THIS was missing — without it ci.cancel() silently does nothing
        remap = false,
        require = 0
    )
    private static void pulkan$skipDeviceScoring(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Android always has exactly one physical device.
        // Scoring logic crashes when deviceType == VK_PHYSICAL_DEVICE_TYPE_OTHER (common on Android).
        // We cancel here so the caller uses devices[0] directly via a @Shadow or separate accessor.
        PulkanMod.LOGGER.info("[Pulkan] Android: skipping device scoring, selecting device[0] directly.");
        ci.cancel(); // NOW actually cancels the method
    }
}
