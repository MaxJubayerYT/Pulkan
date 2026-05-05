package net.pulkan.mixin;

import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.lwjgl.vulkan.VkDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

@Mixin(targets = "net.vulkanmod.vulkan.Renderer", remap = false)
public class RendererFrameMixin {

    // Correct LWJGL descriptor: (VkDevice, long swapchain, long timeout, long semaphore, long fence, IntBuffer pImageIndex)
    @Redirect(
        method = "beginFrame",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/vulkan/KHRSwapchain;vkAcquireNextImageKHR(Lorg/lwjgl/vulkan/VkDevice;JJJJLjava/nio/IntBuffer;)I"
        ),
        remap = false,
        require = 0
    )
    private int pulkan$acquire(VkDevice device, long swapchain, long timeout, long semaphore, long fence, IntBuffer pImageIndex) {
        int result = vkAcquireNextImageKHR(device, swapchain, timeout, semaphore, fence, pImageIndex);
        if (!AndroidEnvironment.isAndroid()) return result;
        if (result == VK_SUBOPTIMAL_KHR) {
            PulkanSwapChain.setSubOptimal(true);
            return VK_SUCCESS;
        }
        PulkanSwapChain.recreateIfNeeded();
        return result;
    }
}
