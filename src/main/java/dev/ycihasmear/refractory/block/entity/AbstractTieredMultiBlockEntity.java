package dev.ycihasmear.refractory.block.entity;

import dev.ycihasmear.refractory.Refractory;
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
    private Map<Integer, BlockPos> multiBlockArray = new HashMap<>();
    private Map<Integer, Integer> variantArray = new HashMap<>();

    private int maxHeight;
    private int size = 0;
    private int oldSize = -1;
    private boolean isFormed = false;

    private int tickCounter = 0;

    private BlockPos centerPos;
    public AbstractTieredMultiBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int maxHeight) {
        super(pType, pPos, pBlockState);
        this.maxHeight = maxHeight;
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState){
        tickCounter++;
        boolean flag = false;

        if (this.isFormed){
            if (this.oldSize == -1){
                this.oldSize = this.size;
                flag = true;
            }
            if(this.oldSize != this.size){
                this.multiBlockArray.clear();
                this.oldSize = this.size;
                flag = true;
                this.setChanged();
            }
        }

        if(tickCounter >= 20){
            tickCounter = 0;
            attemptToFormMultiBlock(pLevel, pPos);
            this.setChanged();
            Refractory.LOGGER.debug("Attempt to form");
        }

        if(flag){
            internalUpdateMultiBlockArrayBlockStates(pLevel);
        }
    }

    protected abstract boolean  checkBottomLayer(Level level, BlockPos controllerPos);

    protected abstract boolean  checkControllerLayer(Level level, BlockPos controllerPos);

    protected abstract boolean checkLayer(Level level, BlockPos controllerPos, int layer);

    public Map<Integer, BlockPos> getMultiblockArray(){
        return multiBlockArray;
    }

    public Map<Integer, Integer> getVariantArray(){
        return variantArray;
    }

    public void putInMultiBlockArray(BlockPos pPos, int variantId){
        int key = multiBlockArray.size();
        multiBlockArray.put(key, pPos);
        variantArray.put(key, variantId);
    }

    public boolean attemptToFormMultiBlock(Level level, BlockPos controllerPos){
        if(checkBottomLayer(level, controllerPos) && checkControllerLayer(level, controllerPos)){
            this.size = setAndGetSize(level, controllerPos);
            this.centerPos = controllerPos.relative(level.getBlockState(controllerPos).getValue(T.FACING));
            isFormed = true;
            return true;
        }
        isFormed = false;
        return false;
    }

    public int setAndGetSize(Level level, BlockPos controllerPos){
        int size = 1;
        for(int i = 1; i < maxHeight; i++){
            if(checkLayer(level, controllerPos, i)){
                size++;
            } else {
                break;
            }
        }
        return size;
    }

    public boolean isFormed(){
        return isFormed;
    }

    public int getMultiBlockSize(){
        return this.size;
    }

    protected abstract boolean updateMultiBlockArrayBlockStates(Level pLevel);

    private void internalUpdateMultiBlockArrayBlockStates(Level pLevel){
        if(this.updateMultiBlockArrayBlockStates(pLevel))
            this.setChanged();
        Refractory.LOGGER.debug("updating block states!");
    }

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
