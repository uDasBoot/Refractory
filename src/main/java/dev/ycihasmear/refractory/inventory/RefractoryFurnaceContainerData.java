package dev.ycihasmear.refractory.inventory;

import net.minecraft.world.inventory.ContainerData;

public interface RefractoryFurnaceContainerData extends ContainerData {
    int getProgressForSlot(int slot);
}
