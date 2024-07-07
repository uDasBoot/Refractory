package dev.ycihasmear.refractory.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ycihasmear.refractory.util.ModResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class RefractoryFurnaceRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final ItemStack result;
    private final String group;

    public RefractoryFurnaceRecipe(String group, Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
        this.group = group;
    }

    @Override
    public boolean matches(SingleRecipeInput singleRecipeInput, Level level) {
        if(level.isClientSide){
            return false;
        }
        return ingredient.test(singleRecipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput singleRecipeInput, HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<RefractoryFurnaceRecipe>{
        public static final Type INSTANCE = new Type();
        public static final String ID = "refractory_furnace_melting";
    }

    public static class Serializer implements RecipeSerializer<RefractoryFurnaceRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ModResourceLocation.modLocation("refractory_furnace_melting");
        public static final MapCodec<RefractoryFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
                .group(Codec.STRING.optionalFieldOf("group", "").forGetter((recipe -> recipe.group)),
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((recipe) -> recipe.ingredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result))
                .apply(builder, RefractoryFurnaceRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, RefractoryFurnaceRecipe> STREAM_CODEC =
                StreamCodec.of(RefractoryFurnaceRecipe.Serializer::toNetwork, RefractoryFurnaceRecipe.Serializer::fromNetwork);

        protected Serializer() {
        }

        public MapCodec<RefractoryFurnaceRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, RefractoryFurnaceRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static RefractoryFurnaceRecipe fromNetwork(RegistryFriendlyByteBuf byteBuf) {
            String group = byteBuf.readUtf();
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(byteBuf);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(byteBuf);
            return new RefractoryFurnaceRecipe(group, ingredient, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf byteBuf, RefractoryFurnaceRecipe recipe) {
            byteBuf.writeUtf(recipe.group);
            Ingredient.CONTENTS_STREAM_CODEC.encode(byteBuf, recipe.ingredient);
            ItemStack.STREAM_CODEC.encode(byteBuf, recipe.result);
        }
    }
}
