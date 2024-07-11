package dev.ycihasmear.refractory.tag;

import dev.ycihasmear.refractory.Refractory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {

        public static final TagKey<Block> ALUMINUM_BLOCKS = tag("aluminum_blocks");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Refractory.MODID, name));
        }
    }

    public static class Items {

    }
}
