package com.rotamap.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Teclas de atalho para o Rota Map: Cartographer.
 */
public class RotaMapKeyBindings {

    public static final String KEY_CATEGORY = "key.categories.rota_map";
    public static final String KEY_OPEN_MAP = "key.rota_map.open_map";

    public static final KeyMapping OPEN_MAP = new KeyMapping(
            KEY_OPEN_MAP,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KEY_CATEGORY
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MAP);
    }
}
