package net.pulkan.mixin;

import net.pulkan.PulkanMod;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Target the MemoryManager class which wraps VMA init in VulkanMod
@Mixin(targets = "net.vulkanmod.vulkan.memory.MemoryManager", remap = false)
public class MemoryAllocatorMixin {

    // "init" is the actual method name in MemoryManager — not method="*" (which is invalid)
    @Inject(
        method = "init",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private static void pulkan$tuneVmaForAndroid(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Signal to NativeLoader that VMA should be configured with mobile-safe flags.
        // VMA config itself (preferredLargeHeapBlockSize, budget extension check) is done
        // in NativeLoader.configureVma() which is called before MemoryManager.init().
        PulkanMod.LOGGER.info("[Pulkan] VMA init intercepted — Android memory tuning applied.");
        // Set a system property so other components can check VMA was tuned
        System.setProperty("pulkan.vma.tuned", "true");
    }
}
