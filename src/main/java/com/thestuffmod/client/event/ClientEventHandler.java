package com.thestuffmod.client.event;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.thestuffmod.TSM;
import com.thestuffmod.client.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientEventHandler {
	

	private static final String[] desc = {"key.itemInfo"};
	private static final String[] category = {"key.categories.inventory"};
	private static final int[] keyValues = {Keyboard.CHAR_NONE};
	private final KeyBinding[] keys;
	
	public ClientEventHandler() {
		keys = new KeyBinding[desc.length];
		for (int i = 0; i < desc.length; ++i) {
			keys[i] = new KeyBinding(desc[i], keyValues[i], category[i]);
			ClientRegistry.registerKeyBinding(keys[i]);
		}
	}


	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Post event) {
		System.out.println("Re-mapped builtin/canvas");
		//event.map.setTextureEntry("builtin/canvas", new TextureCanvas("builtin/canvas"));
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderPlayer(RenderPlayerEvent.Pre event) {
//        event.setCanceled(true);
		if (TSM.lastChat.get(event.entityPlayer.getUniqueID()) != null && TSM.lastChatTime.get(event.entityPlayer.getUniqueID()) != null) {
			String chat = (String)TSM.lastChat.get(event.entityPlayer.getUniqueID()).replace("\u00a7r", "");

			chat = "\u00a7e"+chat;
			long time = System.currentTimeMillis();
			if (time < TSM.lastChatTime.get(event.entityPlayer.getUniqueID())+(chat.length()*500L)) {
	            renderLabel(event.entityPlayer,chat, 0d, 0.4d, 0d, 64);
			} else {
				TSM.lastChat.remove(event.entityPlayer.getUniqueID());
				TSM.lastChatTime.remove(event.entityPlayer.getUniqueID());
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onChatReceived(ClientChatReceivedEvent event) {
		try {
        if (event.type == 0) {
        	UUID player = null;
        	Pattern p = Pattern.compile("\\<(.*?)\\>");
        	Matcher m = p.matcher(event.message.getUnformattedText());
        	while (m.find()) {
        	    String s = m.group(1);
        	    for (EntityPlayer ep:(List<EntityPlayer>)Minecraft.getMinecraft().theWorld.playerEntities) {
        	    	if (ep.getName().equals(s)) {
        	    		player = ep.getUniqueID();
        	    		break;
        	    	}
        	    }
        	}
        	if (player != null && event.message.getFormattedText() != null) {
        		TSM.lastChat.put(player, event.message.getFormattedText().replaceFirst("\\<(.*?)\\> ", ""));
        		TSM.lastChatTime.put(player, System.currentTimeMillis());
        	}
        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemToolTip(ItemTooltipEvent event) {
		if (event.itemStack != null && event.itemStack.getItem() != null && keys[0].getKeyCode() != Keyboard.CHAR_NONE) {
			if (Keyboard.isKeyDown(keys[0].getKeyCode())) {
				String unlocalized = event.itemStack.getUnlocalizedName()+".info";
				if (StatCollector.canTranslate(unlocalized)) {
			        String translated = StatCollector.translateToLocal(unlocalized);
			        ArrayList<String> strings = new ArrayList<String>();
			        for (String s:translated.split("\n")) {
			            strings.add(s);
			        }
			        event.toolTip.addAll(1, strings);
				} else {
			        String translated = StatCollector.translateToLocal(event.itemStack.getUnlocalizedName()+".name");
			        String translated2 = StatCollector.translateToLocalFormatted("tooltip.pressed.nofind", new Object[] {translated});
			        event.toolTip.add(1, translated2);
				}
			} else {
		        String translated = StatCollector.translateToLocalFormatted("tooltip.unpressed", new Object[] {GameSettings.getKeyDisplayString(keys[0].getKeyCode())});
			    event.toolTip.add(1, translated);
			}
		}
	}
	

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onHudRender(RenderGameOverlayEvent.Pre event) {
		if (event.type.equals(ElementType.HOTBAR)) {
//		if (Minecraft.getMinecraft().thePlayer.getHeldItem() != null && Minecraft.getMinecraft().thePlayer.getHeldItem().getItem().equals(TSM.canvasItem)) {
//			ItemStack item = Minecraft.getMinecraft().thePlayer.getHeldItem();
//			UUID id = UUID.fromString(item.getTagCompound().getString("UUID"));
//			if (id != null) {
//				item.getItem().onUpdate(item, Minecraft.getMinecraft().theWorld, null, 0, false);
//			}
//			if (id != null && TSM.canvasGLTextures.get(id) != null) {
//				GL11.glPushMatrix();
//		        GlStateManager.enableRescaleNormal();
//			    GL11.glRotatef(0f, 1f, 0f, 0f);
//			    GL11.glTranslatef(event.resolution.getScaledWidth()/2, event.resolution.getScaledHeight()-48, -100f);
//				GL11.glColor3f(1,1,1);
//				GlStateManager.bindTexture(TSM.canvasGLTextures.get(id).intValue());
//				GlStateManager.disablePolygonOffset();
//				Tessellator t = Tessellator.getInstance();
//				t.getWorldRenderer().startDrawingQuads();
//				t.getWorldRenderer().addVertexWithUV(-64d, -64d, 0d, 0d, 0d);
//				t.getWorldRenderer().addVertexWithUV(-64d, 64d, 0d, 0d, 1d);
//				t.getWorldRenderer().addVertexWithUV(64d, 64d, 0d, 1d, 1d);
//				t.getWorldRenderer().addVertexWithUV(64d, -64d, 0d, 1d, 0d);
//				t.draw();
//				GL11.glPopMatrix();
//			}
//		}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderItemInFrame(RenderItemInFrameEvent event) {
		if (event.item != null && event.item.getItem().equals(TSM.canvasItem)) {
		    event.setCanceled(true);
		    Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.easelTexture);
		    GL11.glPushMatrix();
		    GL11.glRotatef(180f+event.entityItemFrame.getRotation()*45, 0f, 0f, 1f);
		    GL11.glTranslatef(0f, -0.25f, 0.1f);
		    GL11.glScalef(1.335f, 1.335f, 1.335f);
		    GL11.glRotatef(15f, 1f, 0f, 0f);
		    ClientProxy.easelModel.Canvas.render(0.0625f);
		    GL11.glPopMatrix();
	        try {
	    		if (event.item.hasTagCompound()) {
	    			NBTTagCompound tag = event.item.getTagCompound();
	    			if (tag.hasKey("UUID",Constants.NBT.TAG_STRING)) {
	    				UUID id = UUID.fromString(tag.getString("UUID"));

	    				if (id != null) {
	    					event.item.getItem().onUpdate(event.item, event.entityItemFrame.worldObj, null, 0, false);
	    				}
	    				
	    				if (id != null && TSM.canvasGLTextures.get(id) != null) {
	    					GL11.glPushMatrix();
	    			        GlStateManager.enableRescaleNormal();
	    				    GL11.glRotatef(180f+event.entityItemFrame.getRotation()*45, 0f, 0f, 1f);
	    				    GL11.glTranslatef(-0.5f, -0.5f, -0.025f);
	    					GL11.glColor3f(1,1,1);
	    					GlStateManager.bindTexture(TSM.canvasGLTextures.get(id).intValue());
	    					GlStateManager.disablePolygonOffset();
	    					Tessellator t = Tessellator.getInstance();
	    					t.getWorldRenderer().startDrawingQuads();
	    					t.getWorldRenderer().addVertexWithUV(0d, 0d, 0d, 0d, 0d);
	    					t.getWorldRenderer().addVertexWithUV(0d, 1d, 0d, 0d, 1d);
	    					t.getWorldRenderer().addVertexWithUV(1d, 1d, 0d, 1d, 1d);
	    					t.getWorldRenderer().addVertexWithUV(1d, 0d, 0d, 1d, 0d);
	    					t.draw();
	    					GL11.glPopMatrix();
	    				}
	    			}
	    		}
	            } catch (Exception e) {
	            	
	            }
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(KeyInputEvent event) {
		if (Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().currentScreen == null) {

		}
	}
	
	private void renderHeldCanvas(EntityPlayer player, UUID id, float partialTicks) {

	}
	
	protected void renderLabel(Entity entity, String string, double x, double y, double z, int distance) {
    	RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        double d3 = entity.getDistanceSqToEntity(renderManager.livingPlayer);

        if (d3 <= (double)(distance * distance)) {
            FontRenderer fontrenderer = renderManager.getFontRenderer();
            float f = 1.6F;
            float f1 = 0.016666668F * f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x + 0.0F, (float)y + entity.height + 0.5F, (float)z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-f1, -f1, f1);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            byte b0 = 0;

            if (string.equals("deadmau5"))
            {
                b0 = -10;
            }

            GlStateManager.disableTexture2D();
            worldrenderer.startDrawingQuads();
            int j = fontrenderer.getStringWidth(string) / 2;
            worldrenderer.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
            worldrenderer.addVertex((double)(-j - 1), (double)(-1 + b0), 0.0D);
            worldrenderer.addVertex((double)(-j - 1), (double)(8 + b0), 0.0D);
            worldrenderer.addVertex((double)(j + 1), (double)(8 + b0), 0.0D);
            worldrenderer.addVertex((double)(j + 1), (double)(-1 + b0), 0.0D);
            tessellator.draw();
            GlStateManager.enableTexture2D();
            fontrenderer.drawString(string, -fontrenderer.getStringWidth(string) / 2, b0, 553648127);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontrenderer.drawString(string, -fontrenderer.getStringWidth(string) / 2, b0, -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}
