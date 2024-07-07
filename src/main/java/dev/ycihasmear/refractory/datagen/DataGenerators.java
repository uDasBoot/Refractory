package dev.ycihasmear.refractory.datagen;

import dev.ycihasmear.refractory.Refractory;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Refractory.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper efh = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        gen.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), ModLootTableProvider.create(packOutput, lookupProvider));

        gen.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, efh));
        gen.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, efh));

        ModBlockTagGenerator blockTagGenerator = gen.addProvider(event.includeServer(),
                new ModBlockTagGenerator(packOutput, lookupProvider, efh));
        gen.addProvider(event.includeServer(), new ModItemTagGenerator(packOutput, lookupProvider, blockTagGenerator.contentsGetter(), efh));
    }
}
