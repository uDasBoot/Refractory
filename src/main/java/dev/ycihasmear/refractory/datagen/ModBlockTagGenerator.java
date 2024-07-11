package dev.ycihasmear.refractory.datagen;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.tag.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Refractory.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModTags.Blocks.ALUMINUM_BLOCKS).add(ModBlockRegistry.ALUMINUM_ORE.get()
                , ModBlockRegistry.DEEPSLATE_ALUMINUM_ORE.get()
                , ModBlockRegistry.RAW_ALUMINUM_BLOCK.get()
                , ModBlockRegistry.ALUMINUM_BLOCK.get());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(ModTags.Blocks.ALUMINUM_BLOCKS).add(ModBlockRegistry.REFRACTORY_CONTROLLER.get());
        this.tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlockRegistry.ALUMINUM_POWDER_BLOCK.get());
        this.tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlockRegistry.REFRACTORY_BRICKS.get())
                .add(ModBlockRegistry.REFRACTORY_CONTROLLER.get())
                .add(ModBlockRegistry.ALUMINUM_POWDER_BLOCK.get())
                .addTag(ModTags.Blocks.ALUMINUM_BLOCKS);

        this.tag(Tags.Blocks.ORES).add(ModBlockRegistry.ALUMINUM_ORE.get()
                , ModBlockRegistry.DEEPSLATE_ALUMINUM_ORE.get());
    }
}
