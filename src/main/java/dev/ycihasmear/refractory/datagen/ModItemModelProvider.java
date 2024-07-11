package dev.ycihasmear.refractory.datagen;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.item.ModItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

import static dev.ycihasmear.refractory.util.ModResourceLocation.modLocation;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Refractory.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItems(ModItemRegistry.ALUMINUM_INGOT
                , ModItemRegistry.ALUMINUM_POWDER
                , ModItemRegistry.RAW_ALUMINUM);
    }

    @SafeVarargs
    protected final void simpleItems(RegistryObject<Item>... items) {
        Arrays.stream(items).forEach(this::simpleItem);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/generated")).texture("layer0",
                modLocation("item/" + item.getId().getPath()));
    }
}
