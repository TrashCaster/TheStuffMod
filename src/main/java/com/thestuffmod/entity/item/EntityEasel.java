package com.thestuffmod.entity.item;

import java.awt.Color;
import java.util.UUID;

import com.thestuffmod.TSM;
import com.thestuffmod.client.gui.GuiEditCanvas;
import com.thestuffmod.network.packet.CanvasUpdatePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityEasel extends Entity {

    private ItemStack canvas = null;
    private EntityPlayer editedByPlayer = null;

    public EntityEasel(World worldIn) {
        super(worldIn);
        this.setSize(0.9f, 1.8f);
    }

    public EntityEasel(World worldIn, double posX, double posY, double posZ) {
        this(worldIn);
        this.setPosition(posX, posY, posZ);
        this.setEntityBoundingBox(new AxisAlignedBB(this.posX-width/2d,this.posY,this.posZ-width/2d,this.posX+width/2d,this.posY+height,this.posZ+width/2d));
    }

    @Override
    public float getCollisionBorderSize()
    {
        return 0.0F;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }


    @Override
    public boolean interactFirst(EntityPlayer playerIn)
    {
    	if (playerIn.getHeldItem() != null) {
    		if (playerIn.getHeldItem().getItem().equals(TSM.canvasItem) && canvas == null) {
    			if (!this.worldObj.isRemote) {
					ItemStack c = playerIn.getHeldItem().copy();
					c.stackSize = 1;
					NBTTagCompound tag = c.getTagCompound();
					if (tag == null) {
						tag = new NBTTagCompound();
					}
					tag.setBoolean("Update", true);
					c.setTagCompound(tag);
					this.setCanvas(c);
					ItemStack newItemStack = playerIn.getHeldItem();
					newItemStack.stackSize--;
					if (newItemStack.stackSize <= 0) {
						newItemStack = null;
					}
					playerIn.setCurrentItemOrArmor(0, newItemStack);
					TSM.canvasUpdateQueue.add(UUID.fromString(tag.getString("UUID")));
    			}
    		} else if (playerIn.getHeldItem().getItem().equals(TSM.paletteItem) && canvas != null) {
				if (this.getEditedByPlayer() != null) {
					playerIn.addChatComponentMessage(new ChatComponentText("This easel is already in use"));
				    this.setEditedByPlayer(null);
				} else {
    			    playerIn.openGui(TSM.instance, TSM.CANVAS_GUI, worldObj, (int)posX, (int)posY, (int)posZ);
				    this.setEditedByPlayer(playerIn);
				}
    		}
    	} else {
    		if (canvas != null && !this.worldObj.isRemote) {
    			if (this.getEditedByPlayer() == null) {
    			    playerIn.setCurrentItemOrArmor(0, canvas.copy());
    			} else {
					playerIn.addChatComponentMessage(new ChatComponentText("Do not interrupt an artist while they paint!"));
    			}
    			canvas = null;
    		}
    	}
		return true;
    	
    }

    @Override
    public boolean hitByEntity(Entity entityIn)
    {
        return entityIn instanceof EntityPlayer ? this.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)entityIn), 0.0F) : false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (!this.isDead && !this.worldObj.isRemote)
            {
                this.onBroken(source.getEntity());
                this.setDead();
                this.setBeenAttacked();
            }

            return true;
        }
    }

    private void onBroken(Entity entity) {
    	boolean dropEasel = true;
    	if (entity != null && entity instanceof EntityPlayer) {
    		EntityPlayer p = (EntityPlayer)entity;
    		if (p.capabilities.isCreativeMode) {
    			dropEasel = false;
    		}
    	}
    	if (entity != null && !entity.worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
    		dropEasel = false;
    	}
    	if (dropEasel) {
            this.entityDropItem(new ItemStack(TSM.easelItem), 0.0F);
    	}
        if (canvas != null) {
            this.entityDropItem(canvas.copy(), 0.0F);
        }
	}

	@Override
    protected void entityInit() {
    	this.dataWatcher.addObject(9, "");
    	this.dataWatcher.addObjectByDataType(10, 5);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 1024.0D;
    }
    

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target)
    {
    	if (this.getCanvas() != null) {
    		return this.getCanvas().copy();
    	}
    	
    	return new ItemStack(TSM.easelItem);
    }
    
    public float getDirection() {
    	return this.rotationYaw;
    }
    
    public ItemStack getCanvas() {
    	ItemStack is = this.dataWatcher.getWatchableObjectItemStack(10);
    	
    	if (canvas != null) {
    		is = canvas;
    	}
    	
    	return is;
    }
    
    public void setCanvas(ItemStack is) {
    	if (is == null) {
    		this.canvas = null;
    	}
    	if (is != null && is.getItem().equals(TSM.canvasItem)) {
    		this.canvas = is;
    	}
        this.dataWatcher.updateObject(10, canvas);
    }
    
    public EntityPlayer getEditedByPlayer() {
    	String playerID = this.dataWatcher.getWatchableObjectString(9);
        UUID id = playerID.equals("") ? null:UUID.fromString(playerID);
    	if (id == null) {
    	    return this.editedByPlayer;
	    } else {
	    	return worldObj.getPlayerEntityByUUID(id);
	    }
    }
    
    public void setEditedByPlayer(EntityPlayer editedByPlayer) {
    	this.editedByPlayer = editedByPlayer;
    	System.out.println("Currently edited by: "+(editedByPlayer == null ? "Nobody":editedByPlayer.getName()));
    	
    	if (this.editedByPlayer == null) {
    		this.dataWatcher.updateObject(9, "");
    	} else {
    		this.dataWatcher.updateObject(9, this.editedByPlayer.getUniqueID().toString());
    	}
    }

    @Override
	protected void readEntityFromNBT(NBTTagCompound tagCompound) {
        ItemStack is = tagCompound.hasKey("Canvas", Constants.NBT.TAG_COMPOUND) ? ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("Canvas")) : null;
        
        if (is != null) {
            canvas = is;
        }
        String playerID = tagCompound.hasKey("PlayerEditingID",Constants.NBT.TAG_STRING) ? tagCompound.getString("PlayerEditingID"):null;
        UUID id = playerID != null ? UUID.fromString(playerID):null;
        if (id != null && worldObj.getPlayerEntityByUUID(id) != null) {
        	this.editedByPlayer = worldObj.getPlayerEntityByUUID(id);
        } else {
        	this.editedByPlayer = null;
        }
        this.dataWatcher.updateObject(10, canvas);
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        if (canvas == null && tagCompound.hasKey("Canvas")) {
            tagCompound.removeTag("Canvas");
        } else {
            NBTTagCompound itemTag = new NBTTagCompound();
            if (canvas != null) {
                canvas.writeToNBT(itemTag);
                tagCompound.setTag("Canvas", itemTag);
            }
        }
        if (editedByPlayer == null && tagCompound.hasKey("PlayerEditingID")) {
            tagCompound.removeTag("PlayerEditingID");
        } else if (editedByPlayer != null) {
            tagCompound.setString("PlayerEditingID", editedByPlayer.getUniqueID().toString());
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        
        if (canvas != null) {
        	canvas.getItem().onUpdate(canvas, worldObj, null, 0, false);
        }
        
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= 0.03999999910593033D;
        this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
    }
    
    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (!this.worldObj.isRemote && canvas != null) {
            TSM.canvasItem.onUpdate(canvas, worldObj, null, 0, false);
        }
        if (!this.worldObj.isRemote) {
            this.dataWatcher.updateObject(10, canvas);

        	if (this.editedByPlayer == null) {
        		this.dataWatcher.updateObject(9, "");
        	} else {
        		this.dataWatcher.updateObject(9, this.editedByPlayer.getUniqueID().toString());
        	}
        }
    }

}
