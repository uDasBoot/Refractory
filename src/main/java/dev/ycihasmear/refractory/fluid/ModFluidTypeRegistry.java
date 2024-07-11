package dev.ycihasmear.refractory.fluid;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.util.ModResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

public class ModFluidTypeRegistry {
    public static final ResourceLocation MOLTEN_STILL_RL = ModResourceLocation.modLocation("block/molten_fluid_still");
    public static final ResourceLocation MOLTEN_FLOWING_RL = ModResourceLocation.modLocation("block/molten_fluid_flowing");
    public static final ResourceLocation MOLTEN_OVERLAY_RL = ModResourceLocation.modLocation("misc/in_molten_fluid");

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Refractory.MODID);

    public static final RegistryObject<FluidType> MOLTEN_ALUMINUM_FLUID = FLUID_TYPES.register("molten_aluminum_fluid",
            () -> new ModMoltenBaseFluidType(MOLTEN_STILL_RL, MOLTEN_FLOWING_RL, MOLTEN_OVERLAY_RL,
                    0xFFDDE1FF, new Vector3f(221f / 255, 225f / 255, 225f / 255)
            ));

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
