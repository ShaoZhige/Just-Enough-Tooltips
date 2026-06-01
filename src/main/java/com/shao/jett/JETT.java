package com.shao.jett;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JETT.MODID)
public class JETT {

    public static final String MODID = "jett";

    static final ForgeConfigSpec CONFIG_SPEC;
    static final Config CONFIG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CONFIG = new Config(builder);
        CONFIG_SPEC = builder.build();
    }

    public JETT() {
        // 纯客户端模组，服务端跳过
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG_SPEC);
                    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
                });
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new TooltipHandler());
    }
}
