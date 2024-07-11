package dev.ycihasmear.refractory.util;

import dev.ycihasmear.refractory.Refractory;
import net.minecraft.resources.ResourceLocation;

public class ModResourceLocation {
    public static ResourceLocation modLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(Refractory.MODID, path);
    }
}
