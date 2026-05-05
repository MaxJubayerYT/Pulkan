package net.pulkan.mixin;

import net.pulkan.android.PulkanSwapChain;
import net.pulkan.platform.AndroidEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.system.MemoryStack;

@Mixin(targets = "net.vulkanmod.vulkan.framebuffer.SwapChain", remap = false)
public class SwapChainCreationMixin {

    // Shadow the surface handle stored in SwapChain
    @Shadow(remap = false) private long surface;

    // Inject at HEAD of createSwapChain — query capabilities ourselves for pre-rotation setup
    @Inject(
        method = "createSwapChain",
        at = @At("HEAD"),
        remap = false,
        require = 0
    )
    private void pulkan$setupPreRotation(CallbackInfo ci) {
        if (!AndroidEnvironment.isAndroid()) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Query surface capabilities to get currentTransform for pre-rotation
            // physicalDevice is accessed via DeviceManager.physicalDevice
            long physDev = net.vulkanmod.vulkan.device.DeviceManager.physicalDevice.address();
            VkSurfaceCapabilitiesKHR caps = VkSurfaceCapabilitiesKHR.malloc(stack);
            int result = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
                net.vulkanmod.vulkan.device.DeviceManager.physicalDevice, surface, caps);
            if (result == VK_SUCCESS) {
                PulkanSwapChain.setupTransform(caps);
            }
        } catch (Exception e) {
            // Fail silently — pre-rotation is best-effort
        }
    }
}
