package net.pulkan.mixin;

import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.vulkan.KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;

@Mixin(targets = "net.vulkanmod.vulkan.framebuffer.SwapChain", remap = false)
public class SwapChainPreRotationMixin {

    // After swapchain is fully created, log the active transform
    @Inject(
        method = "createSwapChain",
        at = @At("RETURN"),
        remap = false,
        require = 0
    )
    private void pulkan$logPreTransform(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        int transform = PulkanSwapChain.getCurrentTransform();
        if (transform != VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR) {
            // Swap extent dimensions for 90/270 rotation so the image fills the screen correctly
            int[] corrected = PulkanSwapChain.getTransformExtent(
                PulkanSwapChain.getDisplayWidth(),
                PulkanSwapChain.getDisplayHeight()
            );
            net.pulkan.PulkanMod.LOGGER.info(
                "[Pulkan] Pre-rotation active: transform=0x{}, effective extent={}x{}",
                Integer.toHexString(transform), corrected[0], corrected[1]
            );
        }
    }
}
