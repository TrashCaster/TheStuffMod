package com.thestuffmod.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLContainer;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;

import org.lwjgl.opengl.GL11;

import com.thestuffmod.TSM;
import com.thestuffmod.client.event.ClientEventHandler;
import com.thestuffmod.client.models.ModelEasel;
import com.thestuffmod.client.renderer.entity.RenderEasel;
import com.thestuffmod.common.CommonProxy;
import com.thestuffmod.entity.item.EntityEasel;
import com.thestuffmod.item.ItemCanvas;
import com.thestuffmod.item.ItemEasel;
import com.thestuffmod.item.ItemPalette;

public class ClientProxy extends CommonProxy {
	
	public static ModelEasel easelModel = new ModelEasel();
	public static ResourceLocation easelTexture = new ResourceLocation(TSM.MODID, "textures/entity/easel.png");
	public static ClientEventHandler eventHandler;
	
	@Override
	public void preInit() {
		super.preInit();
	}
	
	public void init() {
		super.init();
		eventHandler = new ClientEventHandler();
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(
				TSM.canvasItem,
				0,
				new ModelResourceLocation(TSM.MODID + ":"
						+ (ItemCanvas.getName()), "inventory"));
		renderItem.getItemModelMesher().register(
				TSM.easelItem,
				0,
				new ModelResourceLocation(TSM.MODID + ":"
						+ (ItemEasel.getName()), "inventory"));
		renderItem.getItemModelMesher().register(
				TSM.paletteItem,
				0,
				new ModelResourceLocation(TSM.MODID + ":"
						+ (ItemPalette.getName()), "inventory"));

		RenderingRegistry
				.registerEntityRenderingHandler(EntityEasel.class,
						new RenderEasel(Minecraft.getMinecraft()
								.getRenderManager()));

		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLCommonHandler.instance().bus().register(eventHandler);
		
	}
	
	@Override
	public void updateImage(UUID id, int[] pixelData) {
		BufferedImage bi = new BufferedImage(64, 64,
				BufferedImage.TYPE_INT_ARGB);
		Color none = new Color(0f, 0f, 0f, 0f);
		Graphics2D g = bi.createGraphics();
		g.setBackground(none);
		g.clearRect(0, 0, 64, 64);
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 64; y++) {
				int px = pixelData[x + (y * 64)];

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
			try {
				image.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ImageIO.write(bi, "PNG", image);
		} catch (IOException e1) {
		}
		
		boolean openGL = false;
		try {
			Class.forName("org.lwjgl.opengl.GL11");
			openGL = true;
		} catch (ClassNotFoundException e) {
			
		}
		try {
			if (image.exists() && openGL) {
				try {
					bi = ImageIO.read(image);
				} catch (IOException e) {
				}
				if (bi != null) {
					int i = -1;
					if (TSM.canvasGLTextures.get(id) != null) {
						i = TSM.canvasGLTextures.get(id).intValue();
					} else {
						i = GL11.glGenTextures();
						TSM.canvasGLTextures.put(id,
								Integer.valueOf(i));
					}
					TextureUtil.uploadTextureImageAllocate(i, bi,
							false, false);
				}
		}
		} catch (RuntimeException e) {
			
		}
	}

}
