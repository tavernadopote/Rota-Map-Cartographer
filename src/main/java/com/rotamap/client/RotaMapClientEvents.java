package com.rotamap.client;

import com.mojang.brigadier.CommandDispatcher;
import com.rotamap.client.gui.TopographicMapScreen;
import com.rotamap.topography.TopographicService;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Ouvintes de eventos do lado do cliente para teclado e comandos no Rota Map.
 */
public class RotaMapClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (RotaMapKeyBindings.OPEN_MAP.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new TopographicMapScreen(null));
            } else if (mc.screen instanceof TopographicMapScreen) {
                mc.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("rotamap")
                        .executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            mc.tell(() -> mc.setScreen(new TopographicMapScreen(null)));
                            return 1;
                        })
        );
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        TopographicService.getInstance().clearCache();
    }
}
