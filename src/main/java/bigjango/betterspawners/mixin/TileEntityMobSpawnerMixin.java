package bigjango.betterspawners.mixin;

import bigjango.betterspawners.ISpawner;

import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.world.World;
import com.mojang.nbt.CompoundTag;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TileEntityMobSpawner.class, remap = false)
public abstract class TileEntityMobSpawnerMixin implements ISpawner {
    private CompoundTag data = null;
    private String name = "";

    public void setEntityData(CompoundTag data) {
        this.data = data;
    }

    public void setEntityName(String name) {
        this.name = name == null ? "" : name;
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    public void readFromNBT(CompoundTag tag, CallbackInfo ci) {
        this.name = tag.getString("Name");
        this.data = tag.getCompoundOrDefault("EntityData", null);
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    public void writeToNBT(CompoundTag tag, CallbackInfo ci) {
        tag.putString("Name", this.name);
        if (this.data != null) {
            tag.putCompound("EntityData", this.data);
        }
    }

    // This is a mess, but basically it goes before "this.worldObj.entityJoinedWorld(entityliving);"
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "entityJoinedWorld"))
    private boolean entityJoinedWorld(World world, Entity entity) {
        if (this.data != null) {
            entity.readAdditionalSaveData(this.data);
        }
        ((EntityLiving) entity).nickname = this.name;
        // Call orignal
        return world.entityJoinedWorld(entity);
    }
}
