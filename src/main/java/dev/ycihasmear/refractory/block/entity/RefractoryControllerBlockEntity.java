package dev.ycihasmear.refractory.block.entity;

import dev.ycihasmear.refractory.block.RefractoryBrickBlock;
import dev.ycihasmear.refractory.block.RefractoryControllerBlock;
import dev.ycihasmear.refractory.inventory.RefractoryFurnaceContainerData;
import dev.ycihasmear.refractory.recipe.RefractoryFurnaceRecipe;
import dev.ycihasmear.refractory.screen.RefractoryControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.ycihasmear.refractory.block.ModBlockRegistry;

import java.util.Optional;

public class RefractoryControllerBlockEntity extends AbstractTieredMultiBlockEntity<RefractoryControllerBlock> implements MenuProvider {
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(36);
    private final FluidTank fluidTank = new FluidTank(12000);
    private final EnergyStorage energyStorage = new EnergyStorage(800000, 8000, 0);
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidTank = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyStorage = LazyOptional.empty();

    protected final RefractoryFurnaceContainerData data;
    private int[] progress = new int[36];
    private int maxProgress = 100;

    private boolean wasMelting = false;

    public RefractoryControllerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityRegistry.REFRACTORY_CONTROLLER.get(), pPos, pBlockState, 6);
        this.data = new RefractoryFurnaceContainerData() {
            @Override
            public int get(int i) {
                return switch(i) {
                    case 0 -> RefractoryControllerBlockEntity.this.maxProgress;
                    case 1 -> RefractoryControllerBlockEntity.this.energyStorage.getEnergyStored();
                    case 2 -> RefractoryControllerBlockEntity.this.energyStorage.getMaxEnergyStored();
                    case 3 -> RefractoryControllerBlockEntity.this.getMultiBlockSize();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int i1) {
                switch(i) {
                    case 0 -> RefractoryControllerBlockEntity.this.maxProgress = i1;
                };
            }

            @Override
            public int getProgressForSlot(int slot){
                return RefractoryControllerBlockEntity.this.progress[slot];
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER){
            return lazyItemHandler.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidTank.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
        lazyFluidTank = LazyOptional.of(() -> fluidTank);
        lazyEnergyStorage = LazyOptional.of(() -> energyStorage);
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
        for(int i = 0; i < itemStackHandler.getSlots(); i++){
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.refractory.refractory_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new RefractoryControllerMenu(i, inventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", itemStackHandler.serializeNBT(pRegistries));
        //pTag.put("energy", energyStorage.serializeNBT(pRegistries));
        pTag.putIntArray("refractory_controller.progress_array", progress);
        //pTag.put("fluids", fluidTank.writeToNBT(pTag));
        pTag.putBoolean("refractory_controller.was_melting", wasMelting);

        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        itemStackHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        //energyStorage.deserializeNBT(pRegistries, pTag.getCompound("energy"));
        progress = pTag.getIntArray("refractory_controller.progress_array");
        //fluidTank.readFromNBT(pTag.getCompound("fluids"));.
        wasMelting = pTag.getBoolean("refractory_controller.was_melting");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        boolean flag = false;
        if(this.isFormed()){

            if(this.isMelting() && !wasMelting){
                flag = true;
                wasMelting = true;
            } else if (!this.isMelting() && wasMelting){
                flag = true;
                wasMelting = false;
            }

            for(int i = 0; i < this.getMultiBlockSize()*6;i++) {
                if (hasRecipe(i)) {
                    increaseMeltingProgress(i);
                    setChanged(pLevel, pPos, pState);

                    if (hasMelted(i)) {
                        meltItem(i);
                        resetProgress(i);
                    }
                } else {
                    resetProgress(i);
                }
            }

            if(flag){
                updateMultiBlockArrayBlockStates(pLevel);
            }
        }
    }

    private int variantMapper(int x, int z){
        switch (x) {
            case -1:
                switch (z) {
                    case -1: return 6 ;
                    case 0:  return 3 ;
                    case 1:  return 7 ;
                }
            case 0:
                switch (z) {
                    case -1: return 4 ;
                    case 1:  return 2 ;
                }
            case 1:
                switch (z) {
                    case -1: return 5 ;
                    case 0:  return 1 ;
                    case 1:  return 8 ;
                }
        }
        return -3;
    }

    protected boolean checkBottomLayer(Level level, BlockPos controllerPos) {
        BlockPos relative = controllerPos.below().relative(level.getBlockState(controllerPos).getValue(RefractoryControllerBlock.FACING).getOpposite());
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos currentPos = relative.offset(x, 0, z);
                if(!level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())){
                    return false;
                }
                putInMultiBlockArray(currentPos, (x == 0) && (z == 0) ? 9 : 10);
            }
        }
        return true;
    }

    protected boolean checkControllerLayer(Level level, BlockPos controllerPos) {
        BlockPos center = controllerPos.relative(level.getBlockState(controllerPos).getValue(RefractoryControllerBlock.FACING).getOpposite());
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos currentPos = center.offset(x, 0, z);
                if (x == 0 && z == 0) {
                    // Center should be air
                    if (!level.isEmptyBlock(currentPos)) {
                        return false;
                    }
                } else if (currentPos.equals(controllerPos)) {
                    // Controller position
                    if (!level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_CONTROLLER.get())) {
                        return false;
                    }
                    putInMultiBlockArray(currentPos, -1);
                } else {
                    // Other positions should be REFRACTORY_BRICK
                    if (!level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())) {
                        return false;
                    }
                    putInMultiBlockArray(currentPos, variantMapper(x, z));
                }
            }
        }
        return true;
    }

    protected boolean checkLayer(Level level, BlockPos controllerPos, int layer) {
        BlockPos center = controllerPos.relative(level.getBlockState(controllerPos).getValue(RefractoryControllerBlock.FACING).getOpposite()).above(layer);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos currentPos = center.offset(x, 0, z);
                if (x == 0 && z == 0) {
                    // Center should be air
                    if (!level.isEmptyBlock(currentPos)) {
                        return false;
                    }
                } else {
                    // Surrounding blocks should be REFRACTORY_BRICK
                    if (!level.getBlockState(currentPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())) {
                        return false;
                    }
                    putInMultiBlockArray(currentPos, variantMapper(x, z));
                }
            }
        }
        return true;
    }

    @Override
    protected boolean updateMultiBlockArrayBlockStates(Level pLevel) {
        this.getMultiblockArray().forEach((key, blockPos) -> {
            int variant = this.getVariantArray().get(key);
            if(variant > 0 && pLevel.getBlockState(blockPos).is(ModBlockRegistry.REFRACTORY_BRICKS.get())){
                BlockState updatedBlockState = pLevel.getBlockState(blockPos).setValue(RefractoryBrickBlock.VARIANT_ID,
                        variant).setValue(RefractoryBrickBlock.LIT, isMelting());
                pLevel.setBlockAndUpdate(blockPos, updatedBlockState);
            }
        });
        return true;
    }

    private Boolean isMelting() {
        for(int i = 0; i < this.getMultiBlockSize()*6; i++){
            if(this.hasRecipe(i)) return true;
        }
        return false;
    }

    private boolean hasRecipe(int slot){
        Optional<RecipeHolder<RefractoryFurnaceRecipe>> recipe = getCurrentRecipe(slot);

        if(recipe.isEmpty()){
            return false;
        }

        ItemStack result = recipe.get().value().getResultItem(null);

        return recipe.isPresent();
    }

    private Optional<RecipeHolder<RefractoryFurnaceRecipe>> getCurrentRecipe(int slot){
        SingleRecipeInput input = new SingleRecipeInput(this.itemStackHandler.getStackInSlot(slot));
        return this.level.getRecipeManager().getRecipeFor(RefractoryFurnaceRecipe.Type.INSTANCE, input, level);
    }

    private void increaseMeltingProgress(int slot){
        this.progress[slot]++;
    }

    private boolean hasMelted(int slot){
        return this.progress[slot] >= this.maxProgress;
    }

    private void meltItem(int slot){
        Optional<RecipeHolder<RefractoryFurnaceRecipe>> recipe = getCurrentRecipe(slot);
        ItemStack result = recipe.get().value().getResultItem(null);
        itemStackHandler.extractItem(slot, 1, false);
    }

    private void resetProgress(int slot){
        this.progress[slot] = 0;
    }
}
