package com.thestuffmod;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.thestuffmod.client.renderer.entity.RenderEasel;
import com.thestuffmod.common.CommonProxy;
import com.thestuffmod.common.event.CommonEventHandler;
import com.thestuffmod.entity.item.EntityEasel;
import com.thestuffmod.item.ItemCanvas;
import com.thestuffmod.item.ItemEasel;
import com.thestuffmod.item.ItemPalette;
import com.thestuffmod.network.PacketManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = TSM.MODID, name = TSM.NAME, version = TSM.VERSION)
public class TSM {
	public static final String MODID = "tsm";
	public static final String NAME = "The Stuff Mod";
	public static final String VERSION = "1.0";

	@Instance(value = MODID)
	public static TSM instance;

	@SidedProxy(clientSide = "com.thestuffmod.client.ClientProxy", serverSide = "com.thestuffmod.common.CommonProxy")
	public static CommonProxy proxy;

	public static PacketManager packetManager = new PacketManager(NAME,MODID);
	public static CommonEventHandler eventHandler = new CommonEventHandler();

	public static Item canvasItem;
	public static Item easelItem;
	public static Item paletteItem;

	public static ArrayList<UUID> canvasUniqueIDs = new ArrayList<UUID>();
	public static ArrayList<UUID> canvasUpdateQueue = new ArrayList<UUID>();
	public static HashMap<UUID, Integer> canvasGLTextures = new HashMap<UUID, Integer>();
	public static HashMap<UUID, String> lastChat = new HashMap<UUID, String>();
	public static HashMap<UUID, Long> lastChatTime = new HashMap<UUID, Long>();

	public static File configDir;

	public static final int CANVAS_GUI = 0;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
		String path = event.getModConfigurationDirectory().getAbsolutePath();
		configDir = new File(path.substring(0, path.lastIndexOf("config"))
				.concat("mods/" + NAME + "/"));
		loadCanvasIDs();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
		packetManager.initialise();
    	NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		packetManager.postInitialise();
	}

	public static synchronized void saveCanvasIDs() {
		File f = new File(configDir, "registered-canvas-list.keep");
		try {
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
		} catch (IOException e) {
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));

			for (UUID id : canvasUniqueIDs) {
				bw.write(id.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized void loadCanvasIDs() {
		File f = new File(configDir, "registered-canvas-list.keep");
		try {
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			BufferedReader br = new BufferedReader(new FileReader(f));

			String s = null;
			while ((s = br.readLine()) != null) {
				UUID id = UUID.fromString(s);

				if (id != null) {
					canvasUniqueIDs.add(id);
					canvasUpdateQueue.add(id);
				}
			}
			br.close();
		} catch (IOException e) {
		}
	}

}
