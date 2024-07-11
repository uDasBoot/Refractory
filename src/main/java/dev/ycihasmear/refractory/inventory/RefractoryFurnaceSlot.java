package dev.ycihasmear.refractory.inventory;

import dev.ycihasmear.refractory.screen.RefractoryControllerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class RefractoryFurnaceSlot extends SlotItemHandler {
    private final RefractoryControllerMenu refractoryControllerMenu;

    public RefractoryFurnaceSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition,
                                 RefractoryControllerMenu refractoryControllerMenu) {
        super(itemHandler, index, xPosition, yPosition);
        this.refractoryControllerMenu = refractoryControllerMenu;
    }

    @Override
    public boolean isActive() {
        return isSlotInValidRange() && isSlotOnCurrentPage();
    }

    public boolean isSlotInValidRange() {
        return this.getSlotIndex() < this.refractoryControllerMenu.getFurnaceSize() * 6;
    }

    private boolean isSlotOnCurrentPage() {
        int pageNum = calculatePageNum(this.refractoryControllerMenu.getScrollOffset(),
                (this.refractoryControllerMenu.getFurnaceSize() - 1) / 2);
        return this.getSlotIndex() / 12 == pageNum;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return isSlotInValidRange() && getItem().isEmpty();
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return isActive() && super.mayPickup(playerIn);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    private int calculatePageNum(float scrollPos, int maxPageNum) {
        return Math.round(scrollPos * maxPageNum);
    }
}
