package dev.ycihasmear.refractory.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.ycihasmear.refractory.block.entity.RefractoryControllerBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

public class DebugCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("debugfluid")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int x = IntegerArgumentType.getInteger(context, "x");
                                            int y = IntegerArgumentType.getInteger(context, "y");
                                            int z = IntegerArgumentType.getInteger(context, "z");
                                            BlockPos pos = new BlockPos(x, y, z);

                                            BlockEntity be = context.getSource().getLevel().getBlockEntity(pos);
                                            if (be instanceof RefractoryControllerBlockEntity) {
                                                RefractoryControllerBlockEntity ybe = (RefractoryControllerBlockEntity) be;
                                                FluidStack fluid = ybe.getFluidTank().getFluid();
                                                context.getSource().sendSuccess(() -> Component.literal(
                                                        "Fluid at (" + x + ", " + y + ", " + z + "): " +
                                                                fluid.getDisplayName() + ", Amount: " + fluid.getAmount()
                                                ), true);
                                                ybe.requestUpdate();
                                                return 1;
                                            } else {
                                                context.getSource().sendFailure(Component.literal(
                                                        "No YourBlockEntity found at (" + x + ", " + y + ", " + z + ")"
                                                ));
                                                return 0;
                                            }
                                        })))));
    }
}