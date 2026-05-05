package net.pulkan.mixin;

import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;

@Mixin(targets = "net.vulkanmod.render.chunk.buffer.DrawBuffers", remap = false)
public class DrawBuffersMixin {

    // Inject at HEAD of buildDrawBatchesDirect to intercept vertex buffer binding on Android.
    // @Overwrite with Object params is wrong — it won't match any real method.
    // Instead we inject and guard. If the method signature changes, require=0 prevents a crash.
    @Inject(
        method = "buildDrawBatchesDirect",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void pulkan$fixVertexBufferOffsets(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // The main fix (zero-offset pOffsets for vkCmdBindVertexBuffers) is enforced
        // at the VMA/MemoryManager level. This inject is a hook point for future patches.
    }

    @Inject(
        method = "buildDrawBatchesIndirect",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void pulkan$fixIndirectOffsets(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        // Same guard for the indirect draw path.
    }
}
