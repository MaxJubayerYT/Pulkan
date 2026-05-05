package net.pulkan.mixin;

import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

@Mixin(targets = "net.vulkanmod.vulkan.Renderer", remap = false)
public class RendererSubOptimalMixin {

    // Correct LWJGL descriptor: (VkQueue queue, VkPresentInfoKHR pPresentInfo)
    @Redirect(
        method = "submitFrame",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/vulkan/KHRSwapchain;vkQueuePresentKHR(Lorg/lwjgl/vulkan/VkQueue;Lorg/lwjgl/vulkan/VkPresentInfoKHR;)I"
        ),
        remap = false,
        require = 0
    )
    private int pulkan$present(VkQueue queue, VkPresentInfoKHR presentInfo) {
        int result = vkQueuePresentKHR(queue, presentInfo);
        if (!AndroidEnvironment.isAndroid()) return result;
        if (result == VK_SUBOPTIMAL_KHR || result == VK_ERROR_OUT_OF_DATE_KHR) {
            PulkanSwapChain.setSubOptimal(true);
            return VK_SUCCESS;
        }
        return result;
    }
}
