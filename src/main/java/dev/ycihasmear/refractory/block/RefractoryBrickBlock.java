package dev.ycihasmear.refractory.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class RefractoryBrickBlock extends Block {
    public static final MapCodec<RefractoryBrickBlock> CODEC = simpleCodec(RefractoryBrickBlock::new);
    public static final IntegerProperty VARIANT_ID = IntegerProperty.create("variant_id", 1, 10);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public MapCodec<RefractoryBrickBlock> codec() {
        return CODEC;
    }

    public RefractoryBrickBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.defaultBlockState().setValue(VARIANT_ID, 1).setValue(LIT, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(VARIANT_ID, 10)
                .setValue(LIT, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(VARIANT_ID, LIT);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }
}
