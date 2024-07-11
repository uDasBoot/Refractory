package dev.ycihasmear.refractory.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ModFluidUtils {
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> REGISTRY_STREAM_CODEC =
            new StreamCodec<>() {

                private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_STREAM_CODEC;

                @Override
                public FluidStack decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                    int amount = registryFriendlyByteBuf.readVarInt();
                    if (amount <= 0) {
                        return FluidStack.EMPTY;
                    } else {
                        Holder<Fluid> fluidHolder = FLUID_STREAM_CODEC.decode(registryFriendlyByteBuf);
                        return new FluidStack(fluidHolder.get(), amount);
                    }
                }

                @Override
                public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, FluidStack fluidStack) {
                    if (fluidStack.isEmpty()) {
                        registryFriendlyByteBuf.writeVarInt(0);
                    } else {
                        registryFriendlyByteBuf.writeVarInt(fluidStack.getAmount());
                        FLUID_STREAM_CODEC.encode(registryFriendlyByteBuf, Holder.direct(fluidStack.getFluid()));
                    }
                }

                static {
                    FLUID_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);
                }
            };

}
