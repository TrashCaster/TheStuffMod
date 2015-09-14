package com.thestuffmod.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiColorSlider extends GuiButton
{
    private float sliderValue;
    public boolean dragging;
    private final float minValue;
    private final float maxValue;
    private String label = "";

    public GuiColorSlider(int id, int x, int y, String label)
    {
        this(id, x, y, label, 0.0F, 1.0F);
    }

    public GuiColorSlider(int id, int x, int y, String label, float min, float max)
    {
        super(id, x, y, 80, 20, label);
    	this.label = label;
        this.sliderValue = 1.0F;
        this.minValue = min;
        this.maxValue = max;
        Minecraft minecraft = Minecraft.getMinecraft();
        this.sliderValue = max;
    	this.displayString = label.concat(" ")+this.getNormalisedValue();
    }
    
    public int getNormalisedValue() {
    	return (int)(this.sliderValue*255f);
    }

    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            	this.displayString = label.concat(" ")+this.getNormalisedValue();
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
    	this.displayString = label.concat(" ")+this.getNormalisedValue();
    	super.drawButton(mc,mouseX,mouseY);
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
        	this.displayString = label.concat(" ")+this.getNormalisedValue();
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY)
    {
        this.displayString = label.concat(" "+this.sliderValue);
        this.dragging = false;
    }
}