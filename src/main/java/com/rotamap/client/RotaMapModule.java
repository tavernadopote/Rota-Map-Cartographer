package com.rotamap.client;

import com.rotacore.api.RotaModule;
import com.rotamap.client.gui.TopographicMapScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Módulo de integração do Rota Map com a suíte Rota Core.
 */
public class RotaMapModule implements RotaModule {

    @Override
    public String getId() {
        return "rota_map";
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Rota Map: Cartographer");
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Component getDescription() {
        return Component.literal("Carta topográfica regional com curvas de nível matemáticas, GPS e malha de estradas.");
    }

    @Override
    public Screen createConfigScreen(Screen parent) {
        return new TopographicMapScreen(parent);
    }

    @Override
    public int getOrder() {
        return 30; // Exibição prioritária ao lado de Rota Roads e Questbook
    }
}
