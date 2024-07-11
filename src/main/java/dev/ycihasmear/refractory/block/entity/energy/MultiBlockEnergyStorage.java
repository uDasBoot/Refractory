package dev.ycihasmear.refractory.block.entity.energy;

import net.minecraftforge.energy.EnergyStorage;

public class MultiBlockEnergyStorage extends EnergyStorage {
    public MultiBlockEnergyStorage(int capacity) {
        super(capacity);
    }

    public MultiBlockEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public MultiBlockEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public MultiBlockEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    public boolean setEnergy(int energy, boolean simulate) {
        if(energy < 0) return false;
        if(!simulate) {
            this.energy = Math.min(energy, capacity);
        }
        return true;
    }

    public boolean addEnergy(int energy, boolean simulate) {
        if(energy <= 0 && this.energy + energy > this.capacity) return false;
        if(!simulate)
            this.energy += energy;
        return true;
    }

    public boolean removeEnergy(int energy, boolean simulate) {
        if(energy <= 0 && this.energy - energy < 0) return false;
        if(!simulate)
            this.energy -= energy;
        return true;
    }
}
