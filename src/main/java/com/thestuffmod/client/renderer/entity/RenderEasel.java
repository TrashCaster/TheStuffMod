package com.thestuffmod.client.renderer.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.thestuffmod.TSM;
import com.thestuffmod.client.models.ModelEasel;
import com.thestuffmod.entity.item.EntityEasel;

public class RenderEasel extends Render {
	
	ResourceLocation texture = new ResourceLocation(TSM.MODID+":textures/entity/easel.png");
	ModelEasel easelModel = new ModelEasel();

	public RenderEasel(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}

	@Override
    public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float partialTicks)
    {
        this.doRender((EntityEasel)entity, x, y, z, p_76986_8_, partialTicks);
    }
    

    public void doRender(EntityEasel entity, double x, double y, double z, float p_76986_8_, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.translate((float)x, (float)y+1.45f, (float)z);
        float f2 = 0.0625F;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.rotate(entity.getDirection()-180f, 0f, 1f, 0f);
        GlStateManager.enableAlpha();
        this.bindEntityTexture(entity);
        this.easelModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f2);
        try {
		if (entity.getCanvas() != null && entity.getCanvas().hasTagCompound()) {
			NBTTagCompound tag = entity.getCanvas().getTagCompound();
			if (tag.hasKey("UUID",Constants.NBT.TAG_STRING)) {
				UUID id = UUID.fromString(tag.getString("UUID"));

				if (id != null) {
					entity.getCanvas().getItem().onUpdate(entity.getCanvas(), entity.worldObj, null, 0, false);
					if (TSM.canvasUpdateQueue.contains(id)) {
						TSM.canvasUpdateQueue.remove(id);
					File image = new File(TSM.configDir.getAbsolutePath().concat(
							"/canvas-art/"), id.toString() + ".png");
					if (image.exists()) {
						BufferedImage bi = null;
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
								TSM.canvasGLTextures.put(id,Integer.valueOf(i));
							}
							TextureUtil.uploadTextureImageAllocate(i, bi, false, false);
						}
					}
					}
				}
				
				if (id != null && TSM.canvasGLTextures.get(id) != null) {
					GL11.glPushMatrix();
					GL11.glScaled(1.3d, 1.3d, 1.3d);
					GL11.glRotated(5d, 1d, 0d, 0d);
					GL11.glTranslated(0d, -0.13d, 0d);
					GL11.glRotatef(-15f, 1f, 0f, 0f);
					GL11.glScalef(0.75f,0.75f,0.75f);
					GL11.glTranslatef(-0.5f, -0.25f, -0.117f);
					GL11.glColor3f(1,1,1);
					GlStateManager.disableLighting();
					GlStateManager.bindTexture(TSM.canvasGLTextures.get(id).intValue());
					GlStateManager.disablePolygonOffset();
					Tessellator t = Tessellator.getInstance();
					t.getWorldRenderer().startDrawingQuads();
					t.getWorldRenderer().addVertexWithUV(0d, 0d, 0d, 0d, 0d);
					t.getWorldRenderer().addVertexWithUV(0d, 1d, 0d, 0d, 1d);
					t.getWorldRenderer().addVertexWithUV(1d, 1d, 0d, 1d, 1d);
					t.getWorldRenderer().addVertexWithUV(1d, 0d, 0d, 1d, 0d);
					t.draw();
					GlStateManager.enableLighting();
					GL11.glPopMatrix();
				}
			}
		}
        } catch (Exception e) {
        	
        }
		GL11.glPopMatrix();
        super.doRender(entity, x, y, z, p_76986_8_, partialTicks);
    }
    
	@SideOnly(Side.CLIENT)
	public static int imageToGLTexture(BufferedImage image, UUID id) {

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0,
				image.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth()
				* image.getHeight() * 4);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		buffer.flip();
		int textureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL12.GL_CLAMP_TO_EDGE);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
				image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, buffer);
		return textureID;
	}

}
