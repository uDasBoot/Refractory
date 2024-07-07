package dev.ycihasmear.refractory.item;

import dev.ycihasmear.refractory.Refractory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Refractory.MODID);
    public static final List<Supplier<? extends ItemLike>> TAB_ITEM_LIST = new ArrayList<>();

    public static final RegistryObject<Item> ALUMINUM_INGOT = registerItem("aluminum_ingot", () -> new Item(new Item.Properties()), true);
    public static final RegistryObject<Item> ALUMINUM_POWDER = registerItem("aluminum_powder", () -> new Item(new Item.Properties()), true);
    public static final RegistryObject<Item> RAW_ALUMINUM = registerItem("raw_aluminum", () -> new Item(new Item.Properties()), true);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static <T extends Item> RegistryObject<T> registerItem(String name, Supplier<T> item, boolean addToTab) {
        RegistryObject<T> registeredItem = ITEMS.register(name, item);
        if(addToTab){
            TAB_ITEM_LIST.add(registeredItem);
        }
        return registeredItem;
    }
}