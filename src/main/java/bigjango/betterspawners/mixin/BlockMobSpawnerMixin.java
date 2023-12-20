package bigjango.betterspawners.mixin;

import bigjango.betterspawners.ISpawner;

import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.BlockMobSpawner;
import net.minecraft.core.block.Block;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.item.ItemStack;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockMobSpawner.class, remap = false)
public abstract class BlockMobSpawnerMixin {
    @Shadow public abstract TileEntity getNewBlockEntity();

    @Inject(method = "getBreakResult", at = @At(value = "HEAD"), cancellable = true)
    public void getBreakResult(World world, EnumDropCause dropCause, int x, int y, int z, int meta, TileEntity tileEntity, CallbackInfoReturnable<ItemStack[]> cir) {
        switch (dropCause) {
            case SILK_TOUCH:
                // return new ItemStack[]{new ItemStack(Block.mobspawnerDeactivated)};
                cir.setReturnValue(new ItemStack[]{new ItemStack(Block.mobspawnerDeactivated)});
                break;
            case PICK_BLOCK:
                ItemStack stack = new ItemStack(Block.mobspawner);

                if (tileEntity != null) {
                    CompoundTag compound = new CompoundTag();
                    tileEntity.writeToNBT(compound);
                    stack.getData().putCompound("SpawnerData", compound);
                    stack.setCustomName(compound.getString("Name"));
                }
                // return new ItemStack[]{stack};
                cir.setReturnValue(new ItemStack[]{stack});
                break;
            default:
                System.out.println("Test");
                // return null;
                cir.setReturnValue(null);
                break;
        }
    }

    @Inject(method = "onBlockPlaced", at = @At("TAIL"))
    public void onBlockPlaced(World world, int x, int y, int z, Side side, EntityLiving entity, double sideHeight, CallbackInfo ci) {
        // Spawn tile entity
        TileEntityMobSpawner tileEntity = (TileEntityMobSpawner) world.getBlockTileEntity(x, y, z);
        if (tileEntity == null) tileEntity = (TileEntityMobSpawner) this.getNewBlockEntity();

        // Get NBT (if any)
        EntityPlayer player = (EntityPlayer) entity;
        ItemStack held = player.getHeldItem();
        if (held == null) return;
        CompoundTag spawnerData = held.getData().getCompoundOrDefault("SpawnerData", null);
        if (spawnerData == null) return;

        // Set spawner data (ISpawner only needed because the fields aren't builtin)
        ((ISpawner) tileEntity).setEntityData(spawnerData.getCompoundOrDefault("EntityData", null));
        ((ISpawner) tileEntity).setEntityName(held.getCustomName());
        tileEntity.setMobId(spawnerData.getStringOrDefault("EntityId", "none"));
        tileEntity.delay = spawnerData.getShort("Delay");
    }
}
