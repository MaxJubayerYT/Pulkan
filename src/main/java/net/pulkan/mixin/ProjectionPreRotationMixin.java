package net.pulkan.mixin;

import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.vulkan.VRenderSystem", remap = false)
public class ProjectionPreRotationMixin {

    // No @Shadow — that requires VulkanMod on the compile classpath.
    // Instead we inject at RETURN of setProjectionMatrix and read the
    // matrix from the callback locals if available, or signal via system property.
    @Inject(
        method = "setProjectionMatrix",
        at = @At("RETURN"),
        remap = false,
        require = 0
    )
    private static void pulkan$applyProjectionRotation(Matrix4f mat, CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        if (!PulkanSwapChain.hasPreRotation()) return;
        // mat is the parameter — multiply pre-rotation in place
        Matrix4f rot = PulkanSwapChain.getPreRotationMatrix();
        mat.mulLocal(rot);
    }
}
