package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Only target SwapChain (one class per Mixin - targeting multiple classes requires separate Mixins)
@Mixin(targets = "net.vulkanmod.vulkan.framebuffer.SwapChain", remap = false)
public class SurfaceFormatMixin {

    // "chooseSurfaceFormat" is the real method name in VulkanMod's SwapChain
    @Inject(
        method = "chooseSurfaceFormat",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void pulkan$androidSurfaceFormatFallback(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Signal preference for R8G8B8A8_UNORM over B8G8R8A8_UNORM.
        // Many Android drivers (Samsung Exynos, some Dimensity) do NOT support BGRA
        // and return no matching format, causing a null pointer in SwapChain creation.
        // The actual format fallback logic reads this property in the format selector.
        System.setProperty("pulkan.android.preferRGBA", "true");
        PulkanMod.LOGGER.debug("[Pulkan] Android: preferring VK_FORMAT_R8G8B8A8_UNORM surface format.");
    }
}
