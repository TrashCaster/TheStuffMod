package com.thestuffmod.item;

import java.util.List;

import com.thestuffmod.TSM;
import com.thestuffmod.entity.item.EntityEasel;
import com.thestuffmod.network.packet.CanvasUpdatePacket;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemEasel extends Item {

	private static final String name = "easel";

    public ItemEasel() {
    	GameRegistry.registerItem(this, name);
    	GameRegistry.addShapedRecipe(new ItemStack(this), new Object[] {"SWS"," S ","S S",'S',Items.stick,'W',Blocks.planks});
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setUnlocalizedName(name);
    }
    
    public static String getName()
    {
        return name;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (side == EnumFacing.DOWN) {
            return false;
        } else {
            boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
            BlockPos blockpos1 = flag ? pos : pos.offset(side);

            if (!playerIn.canPlayerEdit(blockpos1, side, stack)) {
                return false;
            } else {
                BlockPos blockpos2 = blockpos1.up();
                boolean flag1 = !worldIn.isAirBlock(blockpos1) && !worldIn.getBlockState(blockpos1).getBlock().isReplaceable(worldIn, blockpos1);
                flag1 |= !worldIn.isAirBlock(blockpos2) && !worldIn.getBlockState(blockpos2).getBlock().isReplaceable(worldIn, blockpos2);

                if (flag1) {
                    return false;
                } else {
                    double d0 = (double) blockpos1.getX();
                    double d1 = (double) blockpos1.getY();
                    double d2 = (double) blockpos1.getZ();
                    List list = worldIn.getEntitiesWithinAABBExcludingEntity((Entity) null, AxisAlignedBB.fromBounds(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));

                    if (list.size() > 0) {
                        return false;
                    } else {
                        if (!worldIn.isRemote) {
                            worldIn.setBlockToAir(blockpos1);
                            worldIn.setBlockToAir(blockpos2);
                            EntityEasel entityeasel = new EntityEasel(worldIn, d0 + 0.5D, d1, d2 + 0.5D);
                            float f3 = (float) MathHelper.floor_float((MathHelper.wrapAngleTo180_float(playerIn.rotationYaw - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                            entityeasel.setLocationAndAngles(d0 + 0.5D, d1, d2 + 0.5D, f3, 0.0F);
                            NBTTagCompound nbttagcompound = stack.getTagCompound();

                            if (nbttagcompound != null && nbttagcompound.hasKey("EntityTag", 10)) {
                                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                                entityeasel.writeToNBTOptional(nbttagcompound1);
                                nbttagcompound1.merge(nbttagcompound.getCompoundTag("EntityTag"));
                                entityeasel.readFromNBT(nbttagcompound1);
                            }

                            worldIn.spawnEntityInWorld(entityeasel);
                        }

                        --stack.stackSize;
                        return true;
                    }
                }
            }
        }
    }

}
