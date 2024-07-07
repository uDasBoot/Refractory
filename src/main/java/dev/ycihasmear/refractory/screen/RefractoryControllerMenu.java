package dev.ycihasmear.refractory.screen;

import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.block.entity.RefractoryControllerBlockEntity;
import dev.ycihasmear.refractory.inventory.RefractoryFurnaceContainerData;
import dev.ycihasmear.refractory.inventory.RefractoryFurnaceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RefractoryControllerMenu extends AbstractContainerMenu {
    public final RefractoryControllerBlockEntity blockEntity;
    private final Level level;
    private final RefractoryFurnaceContainerData data;
    public static final int ROWS_VISIBLE = 4;
    public static final int MAX_ROWS = 12;
    public static final int COLUMNS = 3;
    private float scrollOffset = 0;
    private final List<Slot> furnaceSlots = new ArrayList<>();

    // Client constructor
    public RefractoryControllerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new RefractoryFurnaceData(5));
    }

    // Server constructor
    public RefractoryControllerMenu(int pContainerId, Inventory inv, BlockEntity entity, RefractoryFurnaceContainerData data) {
        super(ModMenuTypeRegistry.REFRACTORY_CONTROLLER_MENU.get(), pContainerId);
        blockEntity = (RefractoryControllerBlockEntity) entity;
        checkContainerSize(inv, 36);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        for (int i = 0; i < MAX_ROWS; i++){
            for (int j = 0; j < COLUMNS; j++){
                int slotIndex = j + i * 3;
                int row = (slotIndex % 12) / 3;
                int col = j;
                this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    this.addSlot(new RefractoryFurnaceSlot(handler, slotIndex, 8 + col * 18, 8 + row * 18, this));
                });
            }
        }
        addDataSlots(data);
    }

    public boolean isMelting(int slot) {
        return this.data.getProgressForSlot(slot) > 0;
    }

    public int getScaledProgress(int slot) {
        int progress = this.data.getProgressForSlot(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 70;

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public void scrollTo(float scrollPos) {
        this.scrollOffset = scrollPos;
    }

    public boolean canScroll() {
        return getFurnaceSize() * 6 > ROWS_VISIBLE * COLUMNS;
    }

    public int getFurnaceSize() {
        return this.data.get(3);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlockRegistry.REFRACTORY_CONTROLLER.get());
    }

    public float getScrollOffset() {
        return this.scrollOffset;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        int furnaceSize = getFurnaceSize();
        int inventorySize = furnaceSize * 6;

        if (pIndex < 36) {
            // Player inventory slot
            if (!moveItemStackTo(sourceStack, 36, 36 + inventorySize, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < 36 + inventorySize) {
            // Furnace slot
            if (!moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    private void addPlayerInventory(Inventory pPlayerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 93 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory pPlayerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(pPlayerInventory, i, 8 + i * 18, 151));
        }
    }

    public static class RefractoryFurnaceSlot extends SlotItemHandler {
        private final RefractoryControllerMenu refractoryControllerMenu;

        public RefractoryFurnaceSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition,
                                     RefractoryControllerMenu refractoryControllerMenu) {
            super(itemHandler, index, xPosition, yPosition);
            this.refractoryControllerMenu = refractoryControllerMenu;
        }

        protected int calculatePageNum(float scrollPos, int maxPageNum) {
            return Math.round(scrollPos * maxPageNum);
        }

        @Override
        public boolean isActive() {
            int i = this.calculatePageNum(this.refractoryControllerMenu.getScrollOffset(),
                    (this.refractoryControllerMenu.getFurnaceSize()-1) / 2);
            return this.getSlotIndex() < this.refractoryControllerMenu.getFurnaceSize()*6
                    && this.getSlotIndex() / 12 == i;
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            if(!isActive()) return false;
            return super.mayPickup(playerIn);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            if(!isActive()) return false;
            return super.mayPlace(stack);
        }
    }
}