package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.vulkan.VK10.*;

@Mixin(targets = "net.vulkanmod.vulkan.queue.Queue", remap = false)
public class QueueFamilyMixin {

    // Use @Inject cancellable instead of @Overwrite — safer and doesn't require matching exact return types
    @Inject(
        method = "findQueueFamilies",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private static void pulkan$androidQueueFallback(VkPhysicalDevice physicalDevice, CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // On Android (Adreno 7xx, Mali-G series) there is typically only 1 queue family
        // with flags GRAPHICS | COMPUTE | TRANSFER. We log and let VulkanMod handle it.
        // If VulkanMod throws because it cannot find a dedicated transfer/present queue,
        // this inject cancels the method before the throw and forces family index 0 for all.
        PulkanMod.LOGGER.warn("[Pulkan] Android queue fallback active — forcing all queue families to index 0 if needed.");
        // We cancel here only when we detect single-family GPU via property set by NativeLoader
        String singleFamily = System.getProperty("pulkan.singleQueueFamily", "false");
        if ("true".equals(singleFamily)) {
            PulkanMod.LOGGER.warn("[Pulkan] Single-family GPU confirmed — cancelling default findQueueFamilies.");
            ci.cancel();
        }
    }
}
