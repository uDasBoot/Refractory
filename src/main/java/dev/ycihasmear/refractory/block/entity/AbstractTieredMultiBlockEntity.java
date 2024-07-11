package dev.ycihasmear.refractory.block.entity;

import dev.ycihasmear.refractory.block.ModEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTieredMultiBlockEntity<T extends ModEntityBlock> extends BlockEntity {
    private final Map<Integer, BlockPos> multiBlockArray = new HashMap<>();
    private final Map<Integer, Integer> variantArray = new HashMap<>();

    private final int maxHeight;
    private int size = 0;
    private int oldSize = -1;
    private boolean isFormed = false;

    private int tickCounter = 0;

    protected BlockPos centerPos;

    public AbstractTieredMultiBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int maxHeight) {
        super(pType, pPos, pBlockState);
        this.maxHeight = maxHeight;
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (level == null || level.isClientSide()) return;

        if (++tickCounter >= 20) {
            tickCounter = 0;
            boolean flag = false;

            if (isFormed) {
                if (oldSize == -1 || oldSize != size) {
                    oldSize = size;
                    multiBlockArray.clear();
                    flag = true;
                    setChanged();
                }
            }

            if (attemptToFormMultiBlock(pPos) || flag) {
                updateMultiBlock();
                setChanged(pLevel, pPos, pState);
            }
        }
    }

    protected abstract boolean checkBottomLayer(BlockPos controllerPos);

    protected abstract boolean checkControllerLayer(BlockPos controllerPos);

    protected abstract boolean checkLayer(BlockPos controllerPos, int layer);

    public Map<Integer, BlockPos> getMultiBlockArray() {
        return multiBlockArray;
    }

    public Map<Integer, Integer> getVariantArray() {
        return variantArray;
    }

    public void putInMultiBlockArray(BlockPos pPos, int variantId) {
        int key = multiBlockArray.size();
        multiBlockArray.put(key, pPos);
        variantArray.put(key, variantId);
    }

    public boolean attemptToFormMultiBlock(BlockPos controllerPos) {
        if (checkBottomLayer(controllerPos) && checkControllerLayer(controllerPos)) {
            size = setAndGetSize(controllerPos);
            centerPos = controllerPos.relative(level.getBlockState(controllerPos).getValue(T.FACING));
            isFormed = true;
            return true;
        }
        isFormed = false;
        return false;
    }

    public int setAndGetSize(BlockPos controllerPos) {
        for (int i = 1; i < maxHeight; i++) {
            if (!checkLayer(controllerPos, i)) {
                return i;
            }
        }
        return maxHeight;
    }

    public boolean isFormed() {
        return isFormed;
    }

    public int getMultiBlockSize() {
        return this.size;
    }

    protected abstract boolean updateMultiBlock();

    protected abstract void destroyMultiBlock();

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.putInt("oldSize", this.oldSize);
        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        this.oldSize = pTag.getInt("oldSize");
        super.loadAdditional(pTag, pRegistries);
    }
}
