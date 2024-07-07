package dev.ycihasmear.refractory.datagen.loot;

import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.item.ModItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlockRegistry.RAW_ALUMINUM_BLOCK.get());
        this.dropSelf(ModBlockRegistry.ALUMINUM_BLOCK.get());
        this.dropSelf(ModBlockRegistry.ALUMINUM_POWDER_BLOCK.get());
        this.dropSelf(ModBlockRegistry.REFRACTORY_BRICKS.get());
        this.dropSelf(ModBlockRegistry.REFRACTORY_CONTROLLER.get());

        this.add(ModBlockRegistry.ALUMINUM_ORE.get(),
                block -> createAluminumOreDrops(ModBlockRegistry.ALUMINUM_ORE.get()));
        this.add(ModBlockRegistry.DEEPSLATE_ALUMINUM_ORE.get(),
                block -> createAluminumOreDrops(ModBlockRegistry.DEEPSLATE_ALUMINUM_ORE.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }

    private LootTable.Builder createAluminumOreDrops(Block pBlock) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(ModItemRegistry.RAW_ALUMINUM.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))).apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))));
    }
}
