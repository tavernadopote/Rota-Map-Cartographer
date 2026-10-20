package com.rotamap.client;

import com.rotacore.api.RotaCoreRegistry;
import com.rotamap.client.gui.TopographicMapScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Ponto de entrada das funcionalidades exclusivas do lado do cliente do Rota Map.
 */
public class RotaMapClient {

    public static void registerClient(IEventBus modEventBus, ModContainer modContainer) {
        // Registra o módulo no ecossistema oficial do Rota Core
        RotaCoreRegistry.register(new RotaMapModule());

        // Registra a tela de visualização no menu de Mods do NeoForge
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, screen) -> new TopographicMapScreen(screen)
        );

        // Registra teclas de atalho
        modEventBus.addListener(RotaMapKeyBindings::register);

        // Registra ouvintes de eventos do Forge (tick e comandos cliente)
        NeoForge.EVENT_BUS.register(RotaMapClientEvents.class);
    }
}
