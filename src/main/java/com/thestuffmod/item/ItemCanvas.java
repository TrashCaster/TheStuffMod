package com.thestuffmod.item;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;

import com.thestuffmod.TSM;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 * @author Mike
 */
public class ItemCanvas extends Item {

	private static final String name = "canvas";

	public ItemCanvas() {
		GameRegistry.registerItem(this, name);
		GameRegistry.addShapedRecipe(new ItemStack(this), new Object[] { "PPP",
				"PPP", "PPP", 'P', Items.paper });
		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setUnlocalizedName(name);
	}

	public static String getName() {
		return name;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn,
			int itemSlot, boolean isSelected) {
		NBTTagCompound tag;
		if (stack.getTagCompound() == null) {
			tag = new NBTTagCompound();
		} else {
			tag = (NBTTagCompound)stack.getTagCompound().copy();
		}
		if (!tag.hasKey("UUID", Constants.NBT.TAG_STRING)) {
			UUID id = UUID.randomUUID();

			while (TSM.canvasUniqueIDs.contains(id)) {
				id = UUID.randomUUID();
			}
			tag.setString("UUID", id.toString());
		}
		if (!tag.hasKey("PixelData", Constants.NBT.TAG_INT_ARRAY)) {
			int[] pixelData = new int[64 * 64];
			Color none = new Color(0f, 0f, 0f, 0f);
			for (int x = 0; x < 64; x++) {
				for (int y = 0; y < 64; y++) {
					pixelData[x + (y * 64)] = none.getRGB();
				}
			}
			tag.setIntArray("PixelData", pixelData);
			tag.setBoolean("Update", true);
		}
		if (!worldIn.isRemote) {
			stack.setTagCompound(tag);
		}

		if (stack.hasTagCompound()) {
			UUID id = null;
			if (stack.getTagCompound().hasKey("UUID", Constants.NBT.TAG_STRING)) {
				id = UUID.fromString(stack.getTagCompound().getString("UUID"));

				if (!TSM.canvasUniqueIDs.contains(id)) {
					TSM.canvasUniqueIDs.add(id);
					TSM.saveCanvasIDs();
					TSM.canvasUpdateQueue.add(id);
				}
			}
			if (id != null
					&& stack.getTagCompound().hasKey("Update")
					&& stack.getTagCompound().getBoolean("Update")
					&& stack.getTagCompound().hasKey("PixelData",
							Constants.NBT.TAG_INT_ARRAY)) {

				try {
					int[] pxData = stack.getTagCompound().getIntArray(
							"PixelData");
					BufferedImage bi = new BufferedImage(64, 64,
							BufferedImage.TYPE_INT_ARGB);
					Color none = new Color(0f, 0f, 0f, 0f);
					Graphics2D g = bi.createGraphics();
					g.setBackground(none);
					g.clearRect(0, 0, 64, 64);
					for (int x = 0; x < 64; x++) {
						for (int y = 0; y < 64; y++) {
							int px = pxData[x + (y * 64)];

							int alpha = (px >> 24 & 255);
							int red = (px >> 16 & 255);
							int green = (px >> 8 & 255);
							int blue = (px & 255);
							Color pxColor = new Color(red, green, blue, alpha);
							g.setColor(pxColor);
							g.drawLine(x, y, x, y);
						}
					}
					g.dispose();
					File image = new File(TSM.configDir.getAbsolutePath()
							.concat("/canvas-art/"), id.toString() + ".png");
					if (!image.exists()) {
						if (image.getParentFile() != null) {
							image.getParentFile().mkdirs();
						}
						image.createNewFile();
					}
					ImageIO.write(bi, "PNG", image);

					TSM.canvasUpdateQueue.add(id);
					System.out.println("Updating");
					stack.getTagCompound().setBoolean("Update", false);
				} catch (IOException e) {
				}
			}
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn,
			List tooltip, boolean advanced) {
		if (stack.hasTagCompound() && advanced) {
			if (stack.getTagCompound().hasKey("UUID", Constants.NBT.TAG_STRING)) {
				tooltip.add(stack.getTagCompound().getString("UUID"));
			}
		}
	}
}
