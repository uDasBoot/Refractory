package dev.ycihasmear.refractory.block.entity;

import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.block.ModBlockRegistry;
import dev.ycihasmear.refractory.block.RefractoryBrickBlock;
import dev.ycihasmear.refractory.block.RefractoryControllerBlock;
import dev.ycihasmear.refractory.block.entity.energy.MultiBlockEnergyStorage;
import dev.ycihasmear.refractory.inventory.RefractoryFurnaceInventoryHandler;
import dev.ycihasmear.refractory.recipe.RefractoryFurnaceRecipe;
import dev.ycihasmear.refractory.screen.RefractoryControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RefractoryControllerBlockEntity extends AbstractTieredMultiBlockEntity<RefractoryControllerBlock> implements MenuProvider, IItemHandler {
    private final RefractoryFurnaceInventoryHandler itemStackHandler;
    private final LazyOptional<IItemHandler> lazyItemHandler;
    private final FluidTank fluidTank;
    private final LazyOptional<FluidTank> lazyFluidTank;
    private final MultiBlockEnergyStorage energyStorage;
    private final LazyOptional<MultiBlockEnergyStorage> lazyEnergyStorage;

    protected final ContainerData data;

    private int[] progress = new int[36];
    private int fluidCapacity = 0;
    private int maxProgress = 100;
    private int energyUsagePerSlot = 50;
    private boolean wasMelting = false;

    public RefractoryControllerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityRegistry.REFRACTORY_CONTROLLER.get(), pPos, pBlockState, 6);

        this.itemStackHandler = new RefractoryFurnaceInventoryHandler(36) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                RefractoryControllerBlockEntity.this.requestUpdate();
            }
        };
        this.lazyItemHandler = LazyOptional.of(() -> this.itemStackHandler);

        this.fluidTank = new FluidTank(216000) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                RefractoryControllerBlockEntity.this.requestUpdate();
            }
        };
        this.lazyFluidTank = LazyOptional.of(() -> this.fluidTank);

        this.energyStorage = new MultiBlockEnergyStorage(36000000, 360000, 0, 36000000);
        this.lazyEnergyStorage = LazyOptional.of(() -> this.energyStorage);

        this.data = createContainerData();
    }

    private ContainerData createContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> RefractoryControllerBlockEntity.this.maxProgress;
                    case 1 -> RefractoryControllerBlockEntity.this.getMultiBlockSize();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) RefractoryControllerBlockEntity.this.maxProgress = value;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    // NBT Methods

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        CompoundTag modData = new CompoundTag();
        modData.put("inventory", itemStackHandler.serializeNBT(pRegistries));
        modData.put("fluidTank", fluidTank.writeToNBT(new CompoundTag()));
        modData.putInt("fluidCapacity", fluidCapacity);
        modData.putInt("energyStorage", energyStorage.getEnergyStored());
        modData.putIntArray("progress_array", progress);
        modData.putBoolean("was_melting", wasMelting);
        pTag.put(Refractory.MODID, modData);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        CompoundTag modData = pTag.getCompound(Refractory.MODID);
        if (modData.isEmpty())
            return;

        if (modData.contains("inventory", Tag.TAG_COMPOUND))
            itemStackHandler.deserializeNBT(pRegistries, modData.getCompound("inventory"));
        if (modData.contains("fluidTank", Tag.TAG_COMPOUND))
            fluidTank.readFromNBT(modData.getCompound("fluidTank"));
        if (modData.contains("fluidCapacity", Tag.TAG_INT))
            fluidCapacity = modData.getInt("fluidCapacity");
        if (modData.contains("energyStorage", Tag.TAG_INT))
            energyStorage.setEnergy(modData.getInt("energyStorage"), false);
        if (modData.contains("progress_array", Tag.TAG_INT_ARRAY))
            progress = modData.getIntArray("progress_array");
        if (modData.contains("was_melting", Tag.TAG_BYTE)) {
            wasMelting = modData.getBoolean("was_melting");
        }
    }

    // Block Entity Methods

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidTank.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidTank.invalidate();
        lazyEnergyStorage.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.refractory.refractory_controller");
    }

    public int getProgressForSlot(int slot) {
        return this.progress[slot];
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new RefractoryControllerMenu(pContainerId, pInventory, this, this.data);
    }

    public RefractoryFurnaceInventoryHandler getItemStackHandler() {
        return itemStackHandler;
    }

    public LazyOptional<IItemHandler> getLazyItemHandler() {
        return lazyItemHandler;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public LazyOptional<FluidTank> getLazyFluidTank() {
        return lazyFluidTank;
    }

    public MultiBlockEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public LazyOptional<MultiBlockEnergyStorage> getLazyEnergyStorage() {
        return lazyEnergyStorage;
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (level == null || level.isClientSide()) return;

        super.tick(pLevel, pPos, pState);

        if (isFormed()) {
            updateFluidCapacity();
            updateMeltingState();
            processSlots();
        }
    }

    private void updateFluidCapacity() {
        int newCapacity = 36000 * getMultiBlockSize();
        if (fluidCapacity != newCapacity) {
            fluidCapacity = newCapacity;
            requestUpdate();
        }
    }

    private void updateMeltingState() {
        boolean isMelting = isMelting();
        if (isMelting != wasMelting) {
            wasMelting = isMelting;
            updateMultiBlock();
        }
    }

    private void processSlots() {
        boolean progressChanged = false;
        for (int i = 0; i < getMultiBlockSize() * 6; i++) {
            if (processSlot(i)) {
                progressChanged = true;
            }
        }
        if (progressChanged) {
            requestUpdate();
        }
    }

    private boolean processSlot(int slot) {
        int oldProgress = progress[slot];
        if (energyStorage.removeEnergy(energyUsagePerSlot, true)) {
            if (hasRecipe(slot)) {
                increaseMeltingProgress(slot);
                if (hasMelted(slot)) {
                    meltItem(slot);
                    resetProgress(slot);
                }
                setChanged();
            } else {
                resetProgress(slot);
            }
        } else {
            resetProgress(slot);
        }
        return progress[slot] != oldProgress;
    }

    // Melting Functionality Methods

    private Boolean isMelting() {
        for (int i = 0; i < this.getMultiBlockSize() * 6; i++) {
            if (this.hasRecipe(i)) return true;
        }
        return false;
    }

    private boolean hasRecipe(int slot) {
        Optional<RecipeHolder<RefractoryFurnaceRecipe>> recipe = getCurrentRecipe(slot);

        if (recipe.isEmpty())
            return false;

        if (!isFluidValid(recipe.get().value().getResultFluid()))
            return false;

        return recipe.isPresent();
    }

    private boolean isFluidValid(FluidStack fluidStack) {
        boolean flag = fluidTank.getFluid().isFluidEqual(fluidStack) || fluidTank.isEmpty();
        boolean flag1 = (fluidCapacity - fluidTank.getFluidAmount()) >= fluidStack.getAmount();

        return flag && flag1;
    }

    private Optional<RecipeHolder<RefractoryFurnaceRecipe>> getCurrentRecipe(int slot) {
        SingleRecipeInput input = new SingleRecipeInput(this.itemStackHandler.getStackInSlot(slot));
        return this.level.getRecipeManager().getRecipeFor(RefractoryFurnaceRecipe.Type.INSTANCE, input, level);
    }

    private void increaseMeltingProgress(int slot) {
        this.progress[slot]++;
        this.energyStorage.removeEnergy(energyUsagePerSlot, false);
        this.requestUpdate();
    }

    private boolean hasMelted(int slot) {
        return this.progress[slot] >= this.maxProgress;
    }

    private void meltItem(int slot) {
        Optional<RecipeHolder<RefractoryFurnaceRecipe>> recipe = getCurrentRecipe(slot);
        if (recipe.isPresent()) {
            FluidStack result = recipe.get().value().getResultFluid().copy();
            ItemStack currentStack = itemStackHandler.getStackInSlot(slot);
            if (!currentStack.isEmpty()) {
                itemStackHandler.extractItem(slot, 1, false);
                fluidTank.fill(result, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    private void resetProgress(int slot) {
        this.progress[slot] = 0;
    }

    // MultiBlock methods.

    private int variantMapper(int x, int z) {
        switch (x) {
            case -1:
                switch (z) {
                    case -1:
                        return 6;
                    case 0:
                        return 3;
                    case 1:
                        return 7;
                }
            case 0:
                switch (z) {
                    case -1:
                        return 4;
                    case 1:
                        return 2;
                }
            case 1:
                switch (z) {
                    case -1:
                        return 5;
                    case 0:
                        return 1;
                    case 1:
                        return 8;
                }
        }
        return -3;
    }

    protected boolean checkBottomLayer(BlockPos controllerPos) {
        if (this.level != null) {
            BlockPos relative = controllerPos.below().relative(this.level.getBlockState(controllerPos).getValue(RefractoryControllerBlock.FACING).getOpposite());
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos currentPos = relative.offset(x, 0, z);
                    if (!this.level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())) {
                        return false;
                    }
                    putInMultiBlockArray(currentPos, (x == 0) && (z == 0) ? 9 : 10);
                }
            }
            return true;
        }
        return false;
    }

    protected boolean checkControllerLayer(BlockPos controllerPos) {
        if (this.level != null) {
            BlockPos center = controllerPos.relative(this.level.getBlockState(controllerPos).getValue(RefractoryControllerBlock.FACING).getOpposite());
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos currentPos = center.offset(x, 0, z);
                    if (x == 0 && z == 0) {
                        // Center should be air
                        if (!this.level.isEmptyBlock(currentPos)) {
                            return false;
                        }
                    } else if (currentPos.equals(controllerPos)) {
                        // Controller position
                        if (!this.level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_CONTROLLER.get())) {
                            return false;
                        }
                        putInMultiBlockArray(currentPos, -1);
                    } else {
                        // Other positions should be REFRACTORY_BRICK
                        if (!this.level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())) {
                            return false;
                        }
                        putInMultiBlockArray(currentPos, variantMapper(x, z));
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected boolean checkLayer(BlockPos controllerPos, int layer) {
        if (this.level != null) {
            BlockPos center = controllerPos.relative(this.level.getBlockState(controllerPos).getValue(RefractoryControllerBlock.FACING).getOpposite()).above(layer);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos currentPos = center.offset(x, 0, z);
                    if (x == 0 && z == 0) {
                        // Center should be air
                        if (!this.level.isEmptyBlock(currentPos)) {
                            return false;
                        }
                    } else {
                        // Surrounding blocks should be REFRACTORY_BRICK
                        if (!this.level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())) {
                            return false;
                        }
                        putInMultiBlockArray(currentPos, variantMapper(x, z));
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean updateMultiBlock() {
        if (this.level != null) {
            this.getMultiBlockArray().forEach((key, blockPos) -> {
                int variant = this.getVariantArray().get(key);
                if (variant > 0 && this.level.getBlockState(blockPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())) {
                    BlockState updatedBlockState = this.level.getBlockState(blockPos).setValue(RefractoryBrickBlock.VARIANT_ID,
                            variant).setValue(RefractoryBrickBlock.LIT, isMelting());
                    this.level.setBlockAndUpdate(blockPos, updatedBlockState);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void destroyMultiBlock() {
        if (this.level != null) {
            this.getMultiBlockArray().forEach((key, blockPos) -> {
                int variant = this.getVariantArray().get(key);
                if (variant > 0 && this.level.getBlockState(blockPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())) {
                    BlockState updatedBlockState = this.level.getBlockState(blockPos).setValue(RefractoryBrickBlock.VARIANT_ID,
                            variant).setValue(RefractoryBrickBlock.LIT, false);
                    this.level.setBlockAndUpdate(blockPos, updatedBlockState);
                }
            });
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookup) {
        handleUpdateTag(pkt.getTag(), lookup);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = super.getUpdateTag(pRegistries);
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider holders) {
        super.handleUpdateTag(tag, holders);
        loadAdditional(tag, holders);
    }

    public void requestUpdate() {
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getFluidTankCapacity() {
        return this.fluidCapacity;
    }

    @Override
    public int getSlots() {
        return this.itemStackHandler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        return this.itemStackHandler.getStackInSlot(i);
    }

    private int findFirstEmptySlot() {
        for (int i = 0; i < this.getMultiBlockSize() * 6; i++) {
            if (itemStackHandler.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;  // No empty slot found
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        int emptySlot = findFirstEmptySlot();
        if (emptySlot != -1) {
            return itemStackHandler.insertItem(emptySlot, stack, simulate);
        }
        return stack;  // Return the original stack if no empty slot is found
    }

    @Override
    public @NotNull ItemStack extractItem(int i, int i1, boolean b) {
        return this.itemStackHandler.extractItem(i, i1, b);
    }

    @Override
    public int getSlotLimit(int i) {
        return 1;
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
        return itemStackHandler.isItemValid(i, itemStack);
    }
}
