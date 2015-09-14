package com.thestuffmod.item;

import com.thestuffmod.TSM;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemPalette extends Item {
	
	private static final String name = "palette";

    public ItemPalette() {
    	GameRegistry.registerItem(this, name);
    	GameRegistry.addShapedRecipe(new ItemStack(this), new Object[] {"WGW","RWB"," YW",'W',Blocks.planks,'R', new ItemStack(Items.dye, 1, EnumDyeColor.RED.getDyeDamage()),'G', new ItemStack(Items.dye, 1, EnumDyeColor.GREEN.getDyeDamage()),'B', new ItemStack(Items.dye, 1, EnumDyeColor.BLUE.getDyeDamage()),'Y', new ItemStack(Items.dye, 1, EnumDyeColor.YELLOW.getDyeDamage())});
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setUnlocalizedName(name);
    }
    
    public static String getName()
    {
        return name;
    }
}
