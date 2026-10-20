package com.rotamap;

import com.rotamap.client.RotaMapClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe principal do mod Rota Map: Cartographer.
 */
@Mod(RotaMap.MOD_ID)
public class RotaMap {
    public static final String MOD_ID = "rota_map";
    public static final Logger LOGGER = LoggerFactory.getLogger("RotaMap");

    public RotaMap(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Inicializando Rota Map: Cartographer (Minecraft 1.21.1)...");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RotaMapClient.registerClient(modEventBus, modContainer);
        }
    }
}
