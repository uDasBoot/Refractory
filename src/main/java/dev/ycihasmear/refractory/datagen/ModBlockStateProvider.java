package dev.ycihasmear.refractory.datagen;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Refractory.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlockRegistry.ALUMINUM_ORE
                ,ModBlockRegistry.DEEPSLATE_ALUMINUM_ORE
                ,ModBlockRegistry.RAW_ALUMINUM_BLOCK
                ,ModBlockRegistry.ALUMINUM_BLOCK
                ,ModBlockRegistry.ALUMINUM_POWDER_BLOCK);


    }

    @SafeVarargs
    private void blockWithItem(RegistryObject<Block> ... blockRegistryObject){
        for (RegistryObject<Block> bro:blockRegistryObject) {
            simpleBlockWithItem(bro.get(), cubeAll(bro.get()));
        }
    }
}
