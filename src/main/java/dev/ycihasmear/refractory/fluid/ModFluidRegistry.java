package dev.ycihasmear.refractory.fluid;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.item.ModItemRegistry;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluidRegistry {

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Refractory.MODID);

    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_ALUMINUM_FLUID = FLUIDS.register("molten_aluminum_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluidRegistry.MOLTEN_LIQUID_PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_ALUMINUM_FLUID = FLUIDS.register("flowing_molten_aluminum_fluid",
            () -> new ForgeFlowingFluid.Flowing(ModFluidRegistry.MOLTEN_LIQUID_PROPERTIES));

    public static final ForgeFlowingFluid.Properties MOLTEN_LIQUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypeRegistry.MOLTEN_ALUMINUM_FLUID, SOURCE_MOLTEN_ALUMINUM_FLUID, FLOWING_MOLTEN_ALUMINUM_FLUID)
            .slopeFindDistance(2).levelDecreasePerBlock(3).tickRate(10).block(ModBlockRegistry.MOLTEN_ALUMINUM_FLUID_BLOCK).bucket(
                    ModItemRegistry.MOLTEN_ALUMINUM_FLUID_BUCKET
            );

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }

}
