package dev.ycihasmear.refractory.block;

import com.mojang.serialization.MapCodec;
import dev.ycihasmear.refractory.block.entity.ModBlockEntityRegistry;
import dev.ycihasmear.refractory.block.entity.RefractoryControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RefractoryControllerBlock extends ModEntityBlock {
    public static final MapCodec<RefractoryControllerBlock> CODEC = simpleCodec(RefractoryControllerBlock::new);

    public RefractoryControllerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public MapCodec<RefractoryControllerBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(LIT, false)
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof RefractoryControllerBlockEntity refractoryEntity) {
                refractoryEntity.drops();
                refractoryEntity.destroyMultiBlock();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        if (pLevel.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof RefractoryControllerBlockEntity && pPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu((MenuProvider) blockEntity, pPos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
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

    @Override
    protected void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof RefractoryControllerBlockEntity refractoryBE) {
            refractoryBE.tick(pLevel, pPos, pState);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntityRegistry.REFRACTORY_CONTROLLER.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }
}
