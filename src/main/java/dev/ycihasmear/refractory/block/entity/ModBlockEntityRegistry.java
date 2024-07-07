package dev.ycihasmear.refractory.block.entity;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Refractory.MODID);

    public static final RegistryObject<BlockEntityType<RefractoryControllerBlockEntity>> REFRACTORY_CONTROLLER =
            BLOCK_ENTITIES.register("refractory_controller",
                    () -> BlockEntityType.Builder.of(RefractoryControllerBlockEntity::new,
                            ModBlockRegistry.REFRACTORY_CONTROLLER.get()).build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }

}
