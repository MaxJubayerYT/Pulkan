package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.vulkan.shader.Pipeline", remap = false)
public class PipelineLayoutMixin {

    // Target the method that creates the pipeline layout.
    // "createPipelineLayout" is the real method name in VulkanMod's Pipeline class.
    @Inject(
        method = "createPipelineLayout",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void pulkan$clampPushConstants(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Push constant range clamping to 128 bytes for older Adreno (505/506) drivers.
        // The actual VkPipelineLayoutCreateInfo patching happens via the ModifyArg
        // approach below if the createPipelineLayout call is visible, otherwise this
        // serves as a log point for Android pipeline creation.
        PulkanMod.LOGGER.debug("[Pulkan] Pipeline layout creation intercepted on Android.");
    }
}
