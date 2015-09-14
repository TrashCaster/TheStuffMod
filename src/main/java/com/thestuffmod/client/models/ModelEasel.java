package com.thestuffmod.client.models;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.thestuffmod.TSM;
import com.thestuffmod.entity.item.EntityEasel;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.util.Constants;


public class ModelEasel extends ModelBase
{
  //fields
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Beam;
    ModelRenderer Rung1;
    ModelRenderer Rung2;
    ModelRenderer Rung3;
    ModelRenderer Shelf;
    public ModelRenderer Canvas;
  
  public ModelEasel()
  {
    textureWidth = 64;
    textureHeight = 32;
    
      Leg1 = new ModelRenderer(this, 0, 0);
      Leg1.addBox(-3.5F, 0F, -1.1F, 1, 25, 1);
      Leg1.setRotationPoint(0F, 0F, 0F);
      Leg1.setTextureSize(64, 32);
      Leg1.mirror = true;
      setRotation(Leg1, -0.1745329F, 0F, 0.1745329F);
      Leg2 = new ModelRenderer(this, 0, 0);
      Leg2.addBox(2.5F, 0F, -1.1F, 1, 25, 1);
      Leg2.setRotationPoint(0F, 0F, 0F);
      Leg2.setTextureSize(64, 32);
      Leg2.mirror = true;
      setRotation(Leg2, -0.1745329F, 0F, -0.1745329F);
      Leg3 = new ModelRenderer(this, 4, 0);
      Leg3.addBox(-0.5F, 0F, -1.1F, 1, 24, 1);
      Leg3.setRotationPoint(0F, 0F, 0F);
      Leg3.setTextureSize(64, 32);
      Leg3.mirror = true;
      setRotation(Leg3, 0.1745329F, 0F, 0F);
      Beam = new ModelRenderer(this, 4, 0);
      Beam.addBox(-0.5F, 0F, -1.1F, 1, 20, 1);
      Beam.setRotationPoint(0F, 0F, 0F);
      Beam.setTextureSize(64, 32);
      Beam.mirror = true;
      setRotation(Beam, -0.1745329F, 0F, 0F);
      Rung1 = new ModelRenderer(this, 8, 13);
      Rung1.addBox(-4F, 4F, -1.6F, 8, 1, 1);
      Rung1.setRotationPoint(0F, 0F, 0F);
      Rung1.setTextureSize(64, 32);
      Rung1.mirror = true;
      setRotation(Rung1, 0F, 0F, 0F);
      Rung2 = new ModelRenderer(this, 8, 15);
      Rung2.addBox(-5F, 11F, -3.1F, 10, 1, 1);
      Rung2.setRotationPoint(0F, 0F, 0F);
      Rung2.setTextureSize(64, 32);
      Rung2.mirror = true;
      setRotation(Rung2, 0F, 0F, 0F);
      Rung3 = new ModelRenderer(this, 8, 17);
      Rung3.addBox(-6F, 19F, -4.6F, 12, 1, 1);
      Rung3.setRotationPoint(0F, 0F, 0F);
      Rung3.setTextureSize(64, 32);
      Rung3.mirror = true;
      setRotation(Rung3, 0F, 0F, 0F);
      Shelf = new ModelRenderer(this, 8, 19);
      Shelf.addBox(-6.966667F, 9F, -2.6F, 14, 1, 2);
      Shelf.setRotationPoint(0F, 0F, 0F);
      Shelf.setTextureSize(64, 32);
      Shelf.mirror = true;
      setRotation(Shelf, -0.1745329F, 0F, 0F);
      Canvas = new ModelRenderer(this, 8, 0);
      Canvas.addBox(-6F, -3F, -1.4F, 12, 12, 1);
      Canvas.setRotationPoint(0F, 0F, 0F);
      Canvas.setTextureSize(64, 32);
      Canvas.mirror = true;
      setRotation(Canvas, -0.2617994F, 0F, 0F);
  }
  
  @Override
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
	  render((EntityEasel)entity,f,f1,f2,f3,f4,f5);
  }
  
  public void render(EntityEasel entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		Leg1.render(f5);
		Leg2.render(f5);
		Leg3.render(f5);
		Beam.render(f5);
		Rung1.render(f5);
		Rung2.render(f5);
		Rung3.render(f5);
		Shelf.render(f5);
		if (entity.getCanvas() != null && entity.getCanvas().getItem().equals(TSM.canvasItem)) {
			GL11.glPushMatrix();
			GL11.glScaled(1.3d, 1.3d, 1.3d);
			GL11.glRotated(5d, 1d, 0d, 0d);
			GL11.glTranslated(0d, -0.13d, 0d);
			Canvas.render(f5);
			GL11.glPopMatrix();
		}
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
