package dev.ycihasmear.refractory.inventory;

import net.minecraft.world.inventory.ContainerData;

public class RefractoryFurnaceData implements RefractoryFurnaceContainerData {

    private final int[] ints;

    public RefractoryFurnaceData(int size){
        this.ints = new int[size];
    }

    public int getProgressForSlot(int slot) {
        return 0;
    }

    public int get(int pIndex) {
        return this.ints[pIndex];
    }

    public void set(int pIndex, int pValue) {
        this.ints[pIndex] = pValue;
    }

    public int getCount() {
        return this.ints.length;
    }
}
