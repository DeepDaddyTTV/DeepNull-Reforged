package dev.deepdaddyttv.deepnullreforged;

import dev.deepdaddyttv.deepnullreforged.client.DeepNullScreen;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullNetwork;
import dev.deepdaddyttv.deepnullreforged.data.RecipeValidationProvider;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod(DeepNullReforged.MODID)
public final class DeepNullReforged {
    public static final String MODID = "deepnullreforged";

    public static final ItemGroup TAB = new ItemGroup(MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModContent.DEEP_NULLS[0].get());
        }
    };

    public DeepNullReforged() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DeepNullNetwork.register();
        ModContent.register(modBus);
        modBus.addListener(this::gatherData);
        MinecraftForge.EVENT_BUS.register(new DeepNullEvents());
    }

    private void gatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            String resources = System.getProperty("deepnull.sourceResources");
            if (resources == null) throw new IllegalStateException("Missing deepnull.sourceResources data-run property");
            event.getGenerator().addProvider(new RecipeValidationProvider(java.nio.file.Paths.get(resources)));
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientSetup {
        private ClientSetup() {
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(new Runnable() {
                @Override
                public void run() {
                    ScreenManager.register(ModContent.DEEP_NULL_MENU.get(), DeepNullScreen::new);
                }
            });
        }
    }
}
