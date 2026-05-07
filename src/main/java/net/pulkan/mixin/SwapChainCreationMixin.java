package net.pulkan.mixin;

import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.vulkan.framebuffer.SwapChain", remap = false)
public class SwapChainCreationMixin {

    @Inject(
        method = "createSwapChain",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void pulkan$setupPreRotation(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Ensure PulkanSwapChain state is initialized before createSwapChain runs.
        // The actual transform values are populated by NativeLoader at startup
        // via PulkanSwapChain.setupTransform(int, int, int).
        PulkanSwapChain.ensureInitialized();
    }
}
