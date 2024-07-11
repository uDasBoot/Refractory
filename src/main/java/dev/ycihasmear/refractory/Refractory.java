package dev.ycihasmear.refractory;

import com.mojang.logging.LogUtils;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.block.entity.ModBlockEntityRegistry;
import dev.ycihasmear.refractory.fluid.ModFluidRegistry;
import dev.ycihasmear.refractory.fluid.ModFluidTypeRegistry;
import dev.ycihasmear.refractory.item.ModItemRegistry;
import dev.ycihasmear.refractory.recipe.ModRecipeRegistry;
import dev.ycihasmear.refractory.screen.ModMenuTypeRegistry;
import dev.ycihasmear.refractory.screen.RefractoryControllerScreen;
import dev.ycihasmear.refractory.util.ModResourceLocation;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Refractory.MODID)
public class Refractory {

    public static final String MODID = "refractory";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("refractory_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.refractory"))
            .icon(ModItemRegistry.ALUMINUM_INGOT.get()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                ModItemRegistry.TAB_ITEM_LIST.forEach(itemLike -> output.accept(itemLike.get()));
            }).build());

    public Refractory() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModBlockRegistry.register(modEventBus);
        ModItemRegistry.register(modEventBus);

        CREATIVE_MODE_TABS.register(modEventBus);

        ModBlockEntityRegistry.register(modEventBus);
        ModMenuTypeRegistry.register(modEventBus);

        ModRecipeRegistry.register(modEventBus);

        ModFluidRegistry.register(modEventBus);
        ModFluidTypeRegistry.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypeRegistry.REFRACTORY_CONTROLLER_MENU.get(), RefractoryControllerScreen::new);
            ItemBlockRenderTypes.setRenderLayer(ModFluidRegistry.SOURCE_MOLTEN_ALUMINUM_FLUID.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluidRegistry.FLOWING_MOLTEN_ALUMINUM_FLUID.get(), RenderType.translucent());
        }
    }
}
