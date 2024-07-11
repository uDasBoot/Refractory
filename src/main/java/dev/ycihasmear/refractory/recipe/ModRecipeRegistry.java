package dev.ycihasmear.refractory.recipe;

import dev.ycihasmear.refractory.Refractory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Refractory.MODID);

    public static final RegistryObject<RecipeSerializer<RefractoryFurnaceRecipe>> REFRACTORY_FURNACE_RECIPE =
            SERIALIZERS.register("refractory_furnace_melting", () -> RefractoryFurnaceRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }

}
