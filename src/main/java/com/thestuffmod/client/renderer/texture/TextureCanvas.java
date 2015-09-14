package com.thestuffmod.client.renderer.texture;

import java.util.UUID;

import com.thestuffmod.TSM;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * This was the class I created to turn the GL textures into 'TextureAtlasSprites'.
 * The whole thing never worked for me, so I commented out the phase that implements this.
 * Un-comment it to see its behavior
 * see ClientEventHandler @onTextureStitch(TextureStitchEvent.Post event)
 */

@SideOnly(Side.CLIENT)
public class TextureCanvas extends TextureAtlasSprite {

	public TextureCanvas(String spriteName) {
		super(spriteName);
	}
	
	@Override
    public void updateAnimation()
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.theWorld != null && minecraft.thePlayer != null && minecraft.thePlayer.getHeldItem() != null && minecraft.thePlayer.getHeldItem().getItem().equals(TSM.canvasItem))
        {
            this.updateCanvasImage(minecraft.thePlayer.getHeldItem());
        }
        else
        {
            this.updateCanvasImage(null);
        }
    }
    
    public void updateCanvasImage(ItemStack item) {
    	if (item != null) {
    		UUID id = UUID.fromString(item.getTagCompound().getString("UUID"));
    		TextureUtil.uploadTexture(TSM.canvasGLTextures.get(id), item.getTagCompound().getIntArray("PixelData"), 64, 64);
    	}
    	
    }

}
