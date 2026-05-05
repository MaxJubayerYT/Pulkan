package net.pulkan.mixin;

import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.vulkan.VRenderSystem", remap = false)
public class ProjectionPreRotationMixin {

    // Shadow the projection matrix field from VRenderSystem
    @Shadow(remap = false)
    private static Matrix4f projectionMatrix;

    // Inject after projectionMatrix is set to multiply in pre-rotation
    @Inject(
        method = "setProjectionMatrix",
        at = @At("RETURN"),
        remap = false,
        require = 0
    )
    private static void pulkan$applyProjectionRotation(Matrix4f mat, CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        if (!PulkanSwapChain.hasPreRotation()) return;

        // Left-multiply the pre-rotation matrix so it applies AFTER the projection
        // PRE_ROTATION_MAT is already set in setupTransform() based on currentTransform
        Matrix4f rot = PulkanSwapChain.getPreRotationMatrix();
        // mul() multiplies this * rot — we want rot * projection, so use mulLocal
        projectionMatrix.mulLocal(rot);
    }
}
