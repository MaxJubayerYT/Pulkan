package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Only target SwapChain (one class per Mixin)
@Mixin(targets = "net.vulkanmod.vulkan.framebuffer.SwapChain", remap = false)
public class PresentModeMixin {

    // "choosePresentMode" is the real method in SwapChain for present mode selection
    @Inject(
        method = "choosePresentMode",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void pulkan$androidPresentModeFallback(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Prefer VK_PRESENT_MODE_FIFO_KHR on Android (battery-friendly, avoids tearing).
        // MAILBOX is not always supported on mobile compositors and wastes battery.
        // Set a property that SwapChain's choosePresentMode can read if patched further.
        System.setProperty("pulkan.android.forceFifoPresentMode", "true");
        PulkanMod.LOGGER.debug("[Pulkan] Android: preferring VK_PRESENT_MODE_FIFO_KHR.");
    }
}
