package bigjango.betterspawners;

import com.mojang.nbt.CompoundTag;

public interface ISpawner {
    public void setEntityData(CompoundTag data);
    public void setEntityName(String name);
}
