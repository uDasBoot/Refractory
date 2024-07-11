package dev.ycihasmear.refractory.datagen;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.item.ModItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private static final List<ItemLike> ALUMINUM_SMELTABLES = List.of(ModItemRegistry.RAW_ALUMINUM.get()
            , ModItemRegistry.ALUMINUM_POWDER.get()
            , ModBlockRegistry.ALUMINUM_ORE.get()
            , ModBlockRegistry.DEEPSLATE_ALUMINUM_ORE.get()
    );

    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        oreSmelting(recipeOutput, ALUMINUM_SMELTABLES, RecipeCategory.MISC, ModItemRegistry.ALUMINUM_INGOT.get(), 0.25f, 200, "aluminum");
        oreBlasting(recipeOutput, ALUMINUM_SMELTABLES, RecipeCategory.MISC, ModItemRegistry.ALUMINUM_INGOT.get(), 0.25f, 100, "aluminum");

        ninePacked(recipeOutput, ModBlockRegistry.RAW_ALUMINUM_BLOCK, ModItemRegistry.RAW_ALUMINUM);
        ninePacked(recipeOutput, ModBlockRegistry.ALUMINUM_BLOCK, ModItemRegistry.ALUMINUM_INGOT);
        ninePacked(recipeOutput, ModBlockRegistry.ALUMINUM_POWDER_BLOCK, ModItemRegistry.ALUMINUM_POWDER);


    }

    protected void ninePacked(RecipeOutput recipeOutput, RegistryObject<Block> block, RegistryObject<Item> item) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', item.get())
                .unlockedBy(getHasName(item.get()), has(item.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item.get(), 9)
                .requires(block.get())
                .unlockedBy(getHasName(block.get()), has(block.get()))
                .save(recipeOutput);
    }

    protected static void oreSmelting(RecipeOutput pRecipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pRecipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput pRecipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pRecipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    private static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput pRecipeOutput, RecipeSerializer<T> pSerializer, AbstractCookingRecipe.Factory<T> pRecipeFactory, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pSuffix) {
        Iterator var10 = pIngredients.iterator();

        while (var10.hasNext()) {
            ItemLike itemlike = (ItemLike) var10.next();
            SimpleCookingRecipeBuilder.generic(Ingredient.of(new ItemLike[]{itemlike}), pCategory, pResult, pExperience, pCookingTime, pSerializer, pRecipeFactory)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pRecipeOutput, Refractory.MODID + ":" + getItemName(pResult) + pSuffix + "_" + getItemName(itemlike));
        }

    }


}
