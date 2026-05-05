package net.pulkan;

import net.fabricmc.api.ClientModInitializer;
import net.pulkan.platform.AndroidEnvironment;
import net.pulkan.platform.NativeLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PulkanMod implements ClientModInitializer {

    public static final String MOD_ID = "pulkan";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        NativeLoader.loadIfNeeded();
        if (AndroidEnvironment.isAndroid()) {
            LOGGER.info("[Pulkan] Android environment detected — activating compatibility layer.");
            LOGGER.info("[Pulkan] Architecture: {}", AndroidEnvironment.getArch());
            LOGGER.info("[Pulkan] Vulkan API version: {}", AndroidEnvironment.getVulkanVersion());
        } else {
            LOGGER.info("[Pulkan] Desktop environment — all patches inactive.");
        }
    }
}
