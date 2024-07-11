package dev.ycihasmear.refractory.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class RefractoryFurnaceInventoryHandler extends ItemStackHandler {

    private int slotLimit = 1;

    public RefractoryFurnaceInventoryHandler(int size) {
        super(size);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotLimit;
    }

    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (getStackInSlot(slot).isEmpty()) {
            ItemStack toInsert = stack.copy();
            toInsert.setCount(1);
            ItemStack remainder = super.insertItem(slot, toInsert, simulate);
            ItemStack leftover = stack.copy();
            leftover.shrink(1);
            return leftover;
        }
        return stack;
    }
}
