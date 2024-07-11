package dev.ycihasmear.refractory.screen;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.block.entity.RefractoryControllerBlockEntity;
import dev.ycihasmear.refractory.inventory.RefractoryFurnaceSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class RefractoryControllerMenu extends AbstractContainerMenu {
    public final RefractoryControllerBlockEntity blockEntity;
    private final Level level;
    public final ContainerData data;
    public static final int ROWS_VISIBLE = 4;
    public static final int MAX_ROWS = 12;
    public static final int COLUMNS = 3;
    private float scrollOffset = 0;

    // Client constructor
    public RefractoryControllerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(3));
    }

    // Server constructor
    public RefractoryControllerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypeRegistry.REFRACTORY_CONTROLLER_MENU.get(), pContainerId);
        checkContainerSize(inv, 36);
        blockEntity = (RefractoryControllerBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addFurnaceSlots();
        addDataSlots(data);
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

    private void addFurnaceSlots() {
        for (int i = 0; i < MAX_ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                int slotIndex = j + i * COLUMNS;
                int row = (slotIndex % 12) / COLUMNS;
                int col = j;
                this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    this.addSlot(new RefractoryFurnaceSlot(handler, slotIndex, 8 + col * 18, 8 + row * 18, this));
                });
            }
        }
    }

    public int getEnergyStored() {
        return this.blockEntity.getEnergyStorage().getEnergyStored();
    }

    ;

    public int getEnergyCapacity() {
        return this.blockEntity.getEnergyStorage().getMaxEnergyStored();
    }

    ;

    public int getFurnaceSize() {
        return this.data.get(1);
    }

    public FluidTank getFluidTank() {
        return blockEntity.getFluidTank();
    }

    public int getFluidCapacity() {
        return blockEntity.getFluidTankCapacity();
    }

    public int getProgressForSlot(int slot) {
        return blockEntity.getProgressForSlot(slot);
    }

    public int getMaxProgress() {
        return this.data.get(0);
    }

    public float getScrollOffset() {
        return this.scrollOffset;
    }

    public boolean canScroll() {
        return getFurnaceSize() * 6 > ROWS_VISIBLE * COLUMNS;
    }

    public void scrollTo(float scrollPos) {
        this.scrollOffset = scrollPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlockRegistry.REFRACTORY_CONTROLLER.get());
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
            if (!moveItemStackToAllSlots(sourceStack, 36, 36 + inventorySize, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < 36 + inventorySize) {
            if (!moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            Refractory.LOGGER.warn("Invalid slotIndex: " + pIndex);
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    private boolean moveItemStackToAllSlots(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        while (!stack.isEmpty()) {
            if (reverseDirection) {
                if (i < startIndex) {
                    break;
                }
            } else if (i >= endIndex) {
                break;
            }

            Slot slot = this.slots.get(i);
            if (slot instanceof RefractoryFurnaceSlot furnaceSlot && furnaceSlot.isSlotInValidRange() && furnaceSlot.mayPlace(stack)) {
                ItemStack itemstack = slot.getItem();
                if (itemstack.isEmpty()) {
                    ItemStack itemToPlace = stack.copy();
                    itemToPlace.setCount(1);
                    slot.set(itemToPlace);
                    stack.shrink(1);
                    slot.setChanged();
                    flag = true;
                    if (stack.isEmpty()) break;
                }
            }

            if (reverseDirection) {
                --i;
            } else {
                ++i;
            }
        }

        return flag;
    }
}