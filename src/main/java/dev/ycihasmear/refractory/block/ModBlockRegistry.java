package dev.ycihasmear.refractory.block;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.item.ModItemRegistry;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Refractory.MODID);

    public static final RegistryObject<Block> ALUMINUM_ORE = registerBlock("aluminum_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)), true);

    public static final RegistryObject<Block> DEEPSLATE_ALUMINUM_ORE = registerBlock("deepslate_aluminum_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(ALUMINUM_ORE.get())
                    .mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)), true);

    public static final RegistryObject<Block> RAW_ALUMINUM_BLOCK = registerBlock("raw_aluminum_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)), true);

    public static final RegistryObject<Block> ALUMINUM_BLOCK = registerBlock("aluminum_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.COPPER)), true);

    public static final RegistryObject<Block> ALUMINUM_POWDER_BLOCK = registerBlock("aluminum_powder_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)), true);


    public static final RegistryObject<Block> REFRACTORY_BRICKS = registerBlock("refractory_bricks",
            () -> new RefractoryBrickBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).mapColor(MapColor.SAND).requiresCorrectToolForDrops()), true);

    public static final RegistryObject<Block> REFRACTORY_CONTROLLER = registerBlock("refractory_controller",
            ()-> new RefractoryControllerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).mapColor(MapColor.SAND).requiresCorrectToolForDrops()), true);

    public static void register(IEventBus modEventBus){
        BLOCKS.register(modEventBus);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, boolean addToTab) {
        RegistryObject<T> registeredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, registeredBlock, addToTab);
        return registeredBlock;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, boolean addToTab) {
        RegistryObject<Item> blockItem = ModItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        if(addToTab){
            ModItemRegistry.TAB_ITEM_LIST.add(blockItem);
        }
        return blockItem;
    }
}