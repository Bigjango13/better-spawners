package bigjango.betterspawners.mixin;

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
        if (dropCause != EnumDropCause.PICK_BLOCK) return;
        ItemStack stack = new ItemStack(Block.mobspawner);

        if (tileEntity != null) {
            CompoundTag compound = new CompoundTag();
            tileEntity.writeToNBT(compound);
            stack.getData().putCompound("SpawnerData", compound);
        }
        cir.setReturnValue(new ItemStack[]{stack});
    }

    @Inject(method = "onBlockPlaced", at = @At("TAIL"))
    public void onBlockPlaced(World world, int x, int y, int z, Side side, EntityLiving entity, double sideHeight, CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer)entity;
        CompoundTag spawnerData = player.getHeldItem().getData().getCompoundOrDefault("SpawnerData", null);
        TileEntityMobSpawner tileEntity = (TileEntityMobSpawner)world.getBlockTileEntity(x, y, z);
        if (tileEntity == null) tileEntity = (TileEntityMobSpawner)getNewBlockEntity();
        if (spawnerData == null) return;

        tileEntity.setMobId(spawnerData.getString("EntityId"));
        tileEntity.delay = spawnerData.getShort("Delay");
    }
}
