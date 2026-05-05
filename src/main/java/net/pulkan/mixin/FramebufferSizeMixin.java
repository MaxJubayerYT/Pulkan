package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.vulkan.framebuffer.Framebuffer", remap = false)
public class FramebufferSizeMixin {

    @Shadow(remap = false)
    private int width;

    @Shadow(remap = false)
    private int height;

    // Inject at RETURN of constructor — ensure width/height are never 0
    // GLFW reports 0x0 on some launchers during initialization on Android
    @Inject(
        method = "<init>",
        at = @At("RETURN"),
        remap = false,
        require = 0
    )
    private void pulkan$clampFramebufferSize(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        if (this.width < 1) {
            PulkanMod.LOGGER.warn("[Pulkan] Framebuffer width was {}, clamping to display width {}",
                this.width, PulkanSwapChain.getDisplayWidth());
            this.width = Math.max(PulkanSwapChain.getDisplayWidth(), 1);
        }
        if (this.height < 1) {
            PulkanMod.LOGGER.warn("[Pulkan] Framebuffer height was {}, clamping to display height {}",
                this.height, PulkanSwapChain.getDisplayHeight());
            this.height = Math.max(PulkanSwapChain.getDisplayHeight(), 1);
        }
    }
}
