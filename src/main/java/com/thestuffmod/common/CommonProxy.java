package com.thestuffmod.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.thestuffmod.TSM;
import com.thestuffmod.client.gui.GuiEditCanvas;
import com.thestuffmod.entity.item.EntityEasel;
import com.thestuffmod.inventory.DummyContainer;
import com.thestuffmod.item.ItemCanvas;
import com.thestuffmod.item.ItemEasel;
import com.thestuffmod.item.ItemPalette;
import com.thestuffmod.network.packet.CanvasUpdatePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameData;

public class CommonProxy implements IGuiHandler {

    public void preInit() {        
    	TSM.canvasItem = new ItemCanvas();
    	TSM.easelItem = new ItemEasel();
    	TSM.paletteItem = new ItemPalette();
        

    	int easelID = 31;
		EntityRegistry.registerModEntity(EntityEasel.class, "easel", easelID, "tsm", 64, 20, false);
		EntityList.addMapping(EntityEasel.class, "Easel", easelID);
    }
    
    public void init() {
    	TSM.packetManager.registerPacket(CanvasUpdatePacket.class);
    	MinecraftForge.EVENT_BUS.register(TSM.eventHandler);
    }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == TSM.CANVAS_GUI) {
			return new DummyContainer();
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == TSM.CANVAS_GUI) {
			Entity e = Minecraft.getMinecraft().objectMouseOver.entityHit;
			if (e != null && e instanceof EntityEasel) {
				EntityEasel easel = (EntityEasel)e;
				return new GuiEditCanvas(easel.getCanvas());
			}
		}
		return null;
	}
	
	public void updateImage(UUID id, int[] pixelData) {
		BufferedImage bi = new BufferedImage(64, 64,BufferedImage.TYPE_INT_ARGB);
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
		File image = new File(TSM.configDir.getAbsolutePath().concat("/canvas-art/"), id.toString() + ".png");
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
	}
}
