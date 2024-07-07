package dev.ycihasmear.refractory.block;

import com.mojang.serialization.MapCodec;
import dev.ycihasmear.refractory.block.entity.ModBlockEntityRegistry;
import dev.ycihasmear.refractory.block.entity.RefractoryControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RefractoryControllerBlock extends ModEntityBlock {
    public static final MapCodec<RefractoryControllerBlock> CODEC = simpleCodec(RefractoryControllerBlock::new);

    public MapCodec<RefractoryControllerBlock> codec() {
        return CODEC;
    }
    public RefractoryControllerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(LIT, false).setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if(blockEntity instanceof RefractoryControllerBlockEntity) {
                ((RefractoryControllerBlockEntity) blockEntity).drops();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if(!(blockEntity instanceof RefractoryControllerBlockEntity)){
            return InteractionResult.PASS;
        }
        if(pLevel.isClientSide)
            return InteractionResult.SUCCESS;

        if(pPlayer instanceof ServerPlayer){
            ((ServerPlayer) pPlayer).openMenu((MenuProvider) blockEntity, pPos);
        }

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RefractoryControllerBlockEntity(blockPos, blockState);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    protected BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    protected BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide){
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntityRegistry.REFRACTORY_CONTROLLER.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}
