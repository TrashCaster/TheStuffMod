package com.thestuffmod.client.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.scene.shape.Line;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.thestuffmod.TSM;
import com.thestuffmod.network.packet.CanvasUpdatePacket;

public class GuiEditCanvas extends GuiScreen {

	private ItemStack canvas;
	private GuiButton doneBtn;
	private GuiButton modeButton;
	private GuiButton gridToggle;
	private GuiButton brushButton;
	private GuiButton importButton;

	private GuiColorSlider redSlider;
	private GuiColorSlider greenSlider;
	private GuiColorSlider blueSlider;
	private GuiNoiseSlider noiseSlider;
	private int soundWait = 0;

	private ResourceLocation canvasTexture = new ResourceLocation(TSM.MODID,
			"textures/gui/canvas/canvas.png");
	private ResourceLocation drawTexture = new ResourceLocation(TSM.MODID,
			"textures/gui/canvas/draw.png");
	private ResourceLocation eraseTexture = new ResourceLocation(TSM.MODID,
			"textures/gui/canvas/erase.png");
	private ResourceLocation fillTexture = new ResourceLocation(TSM.MODID,
			"textures/gui/canvas/fill.png");

	private Color current = Color.WHITE;
	private Color none = new Color(0, 0, 0, 0);
	private Point cursor = new Point(0, 0);

	int brushMode = 0;
	int brushSize = 1;
	int brushButtonState = 0;
	ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
	boolean grid = false;

	long noiseSeed = (new Random()).nextLong();

	private int[] pixels = null;

	public GuiEditCanvas(ItemStack canvas) {
		this.canvas = canvas;
		if (canvas.hasTagCompound()) {
			if (canvas.getTagCompound().hasKey("PixelData",
					Constants.NBT.TAG_INT_ARRAY)) {
				this.pixels = canvas.getTagCompound().getIntArray("PixelData");
			}
		}
	}

	public void initGui() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.add(this.doneBtn = new GuiButton(0,
				this.width / 2 - 100, this.height - this.height / 8, I18n
				.format("gui.done", new Object[0])));
		this.buttonList.add(this.modeButton = new GuiButton(1, 20,
				this.height / 2 + 85, 80, 20, ""));
		this.buttonList.add(this.brushButton = new GuiButton(2, 20,
				this.height / 2 + 25, 80, 20, "Brush Size: 1"));
		this.buttonList.add(this.gridToggle = new GuiButton(3,
				20, this.height / 2 + 55, 80, 20,
				I18n.format("Grid: OFF", new Object[0])));
		this.buttonList.add(this.redSlider = new GuiColorSlider(4, 20,
				this.height / 2 - 55, "Red"));
		this.buttonList.add(this.greenSlider = new GuiColorSlider(5, 20,
				this.height / 2 - 30, "Green"));
		this.buttonList.add(this.blueSlider = new GuiColorSlider(6, 20,
				this.height / 2 - 5, "Blue"));
		this.buttonList.add(this.noiseSlider = new GuiNoiseSlider(7, 20,
				this.height / 2 - 5, "Noise"));
		this.buttonList.add(this.importButton = new GuiButton(8,
				20, this.height / 2 + 55, 80, 20,
				I18n.format("Choose Image", new Object[0])));
		this.brushButton.displayString = "Brush Size: " + brushSize;
		this.gridToggle.displayString = "Grid: "+(grid ? "ON":"OFF");
		String mode = "NULL";
		this.importButton.visible = false;
		this.noiseSlider.visible = false;
		switch (this.brushMode) {
		case 0:
			mode = "Draw";
			break;
		case 1:
			mode = "Erase";
			break;
		case 2:
			mode = "Fill";
			break;
		case 3:
			mode = "Noise";
		case 4:
			mode = "Import";
			break;
		}
		this.modeButton.displayString = "\u00a7bMode: \u00a7a\u00a7n"+mode;
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		TSM.packetManager
		.sendToServer(new CanvasUpdatePacket(this.pixels, UUID
				.fromString(this.canvas.getTagCompound().getString(
						"UUID"))));
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (soundWait > 0) {
			soundWait -= 1;
		}
		cursor.setLocation(this.getSelectedPixelX(mouseX),
				this.getSelectedPixelY(mouseY));
		selectedIndexes.clear();
		if (grid && brushMode != 2) {
			cursor.setLocation(
					(int) Math.floor((float) this.getSelectedPixelX(mouseX)
							/ (float) brushSize)
					* brushSize,
					(int) Math.floor((float) this.getSelectedPixelY(mouseY)
							/ (float) brushSize)
					* brushSize);
		}
		if (cursor.x >=0 && cursor.x < 64 && cursor.y >= 0 && cursor.y < 64) {
			for (int x=0; x<brushSize; x++) {
				for (int y=0; y<brushSize; y++) {
					selectedIndexes.add(cursor.x+x+((cursor.y+y)*64));
				}
			}
		}
		try {
			this.drawDefaultBackground();
			ArrayList<String> textLines = new ArrayList<String>();
			if (this.brushMode == 0) {
				textLines.add("\u00a7eDraw Mode");
				textLines.add("");
				textLines.add("Draw a color onto the");
				textLines.add("canvas");
				textLines.add("");
				textLines.add("Use grid mode to snap");
				textLines.add("the brush to the");
				textLines.add("grid boundary");
				textLines.add("");
				textLines.add("The grid size boundaries");
				textLines.add("are based on brush size");
			}
			if (this.brushMode == 1) {
				textLines.add("\u00a7eErase Mode");
				textLines.add("");
				textLines.add("Remove color from the");
				textLines.add("canvas");
				textLines.add("");
				textLines.add("Use grid mode to snap");
				textLines.add("the brush to the");
				textLines.add("grid boundary");
				textLines.add("");
				textLines.add("The grid size boundaries");
				textLines.add("are based on brush size");
			}
			if (this.brushMode == 2) {
				textLines.add("\u00a7eFill Mode");
				textLines.add("");
				textLines.add("Flood fill color to the");
				textLines.add("canvas");
				textLines.add("");
				textLines.add("This will replace an area");
				textLines.add("of color with another");
				textLines.add("color");
				textLines.add("");
				textLines.add("Brush size doesn't");
				textLines.add("affect this tool");
			}
			if (this.brushMode == 3) {
				textLines.add("\u00a7eNoise Mode");
				textLines.add("");
				textLines.add("Offset a color on the");
				textLines.add("canvas by a random");
				textLines.add("amount");
				textLines.add("");
				textLines.add("To prevent excess noise");
				textLines.add("change this tool is limited");
				textLines.add("to per-click use");
				textLines.add("");
				textLines.add("Use grid mode to snap");
				textLines.add("the brush to the");
				textLines.add("grid boundary");
				textLines.add("");
				textLines.add("The grid size boundaries");
				textLines.add("are based on brush size");
			}
			if (this.brushMode == 4) {
				textLines.add("\u00a7eImport Mode");
				textLines.add("");
				textLines.add("Choose an image from");
				textLines.add("your computer to use as");
				textLines.add("the painting");
				textLines.add("");
				textLines.add("It will automatically be");
				textLines.add("rescaled to 64x64");
				textLines.add("without maintaining");
				textLines.add("aspect ratio");
			}

			int maxWidth = 0;
			for (String s:textLines) {
				if (this.fontRendererObj.getStringWidth(s) > maxWidth) {
					maxWidth = this.fontRendererObj.getStringWidth(s);
				}
			}

			if (this.brushMode == 0 || this.brushMode == 2) {
				this.current = new Color(this.redSlider.getNormalisedValue(),
						this.greenSlider.getNormalisedValue(),
						this.blueSlider.getNormalisedValue(), 255);
				this.drawRect(20, this.height / 2 - 80, 100,
						this.height / 2 - 60,
						this.current.getRGB());
			}

			mc.renderEngine.bindTexture(canvasTexture);
			GL11.glPushMatrix();
			GL11.glColor3f(1f, 1f, 1f);
			this.drawModalRectWithCustomSizedTexture(this.width / 2 - 64,
					this.height / 2 - 64, 0, 0, 128, 128, 128, 128);
			GL11.glPopMatrix();
			Random rand = new Random(noiseSeed);
			for (int x = 0; x < 64; x++) {
				for (int y = 0; y < 64; y++) {
					int index = x + (y * 64);
					if (this.pixels[index] != none.getRGB() && (this.brushMode != 1 || selectedIndexes.isEmpty() || !selectedIndexes.contains(Integer.valueOf(index)))) {
						Color c = new Color(pixels[index], true);
						int r = c.getRed();
						int g = c.getGreen();
						int b = c.getBlue();
						int a = c.getAlpha();

						if (this.brushMode == 3 && x >= cursor.x && y >= cursor.y && x < cursor.x+brushSize && y < cursor.y + brushSize && cursor.x >= 0 && cursor.y >= 0 && cursor.x < 64 && cursor.y < 64) {
							double amtChange = (-1d+(rand.nextDouble()*2))*((double)(rand.nextInt(noiseSlider.getNormalisedValue())+1)*10d);
							r += (int)amtChange;
							g += (int)amtChange;
							b += (int)amtChange;
							r = Math.min(Math.max(0, r), 255);
							g = Math.min(Math.max(0, g), 255);
							b = Math.min(Math.max(0, b), 255);
							c = new Color(r,g,b,a);
						}
						this.drawRect(this.width / 2 - 64 + x * 2, this.height / 2 - 64 + y * 2, this.width / 2 - 64 + x * 2 + 2, this.height / 2 - 64 + y * 2 + 2,
								c.getRGB());
					}
				}
			}
			if (grid) {
				int offX = this.width / 2 - 64;
				int offY = this.height / 2 - 64;
				this.drawHorizontalLine(offX, offX + 128, offY,
						Color.gray.getRGB());
				this.drawHorizontalLine(offX, offX + 128, offY + 128,
						Color.gray.getRGB());
				this.drawVerticalLine(offX, offY, offY + 128,
						Color.gray.getRGB());
				this.drawVerticalLine(offX + 128, offY, offY + 128,
						Color.gray.getRGB());
				for (int x = 0; x < 64; x += this.brushSize) {
					for (int y = 0; y < 64; y += this.brushSize) {
						int startX = offX + (x * 2);
						int startY = offY + (y * 2);
						int endX = offX + ((x + brushSize) * 2);
						int endY = offY + ((y + brushSize) * 2);
						this.drawHorizontalLine(startX, endX, startY,
								Color.gray.getRGB());
						this.drawHorizontalLine(startX, endX, endY,
								Color.gray.getRGB());
						this.drawVerticalLine(startX, startY, endY,
								Color.gray.getRGB());
						this.drawVerticalLine(endX, startY, endY,
								Color.gray.getRGB());
					}
				}
			}
			super.drawScreen(mouseX, mouseY, partialTicks);
			if (cursor.x >= 0 && this.cursor.y >= 0 && cursor.x <= 63
					&& cursor.y <= 63 && this.brushMode == 0) {
				this.drawRect(this.width / 2 - 64 + (cursor.x + brushSize / 2) * 2 - (brushSize - 1), this.height / 2 - 64 + (cursor.y + brushSize / 2) * 2 - (brushSize - 1),
						this.width / 2 - 64 + (cursor.x + brushSize / 2) * 2
						+ brushSize, this.height / 2 - 64
						+ (cursor.y + brushSize / 2) * 2 + brushSize,
						current.getRGB());
			}
			if (this.brushMode == 0 && mouseX > 120 && mouseX < width - 120 && mouseY  > 20 && mouseY < height-height/8) {
				mc.renderEngine.bindTexture(drawTexture);
				GL11.glPushMatrix();
				GL11.glColor3f((float) current.getRed() / 255f,
						(float) current.getGreen() / 255f,
						(float) current.getBlue() / 255f);
				this.drawModalRectWithCustomSizedTexture(mouseX + 1,
						mouseY - 12, 0, 0, 12, 12, 12, 12);
				GL11.glPopMatrix();
			} else if (this.brushMode == 1 && mouseX > 120
					&& mouseX < width - 120 && mouseY  > 20 && mouseY < height-height/8) {
				mc.renderEngine.bindTexture(eraseTexture);
				GL11.glPushMatrix();
				GL11.glColor3f(1f, 1f, 1f);
				this.drawModalRectWithCustomSizedTexture(mouseX + 1,
						mouseY - 12, 0, 0, 12, 12, 12, 12);
				GL11.glPopMatrix();
			} else if (this.brushMode == 2 && mouseX > 120
					&& mouseX < width - 120 && mouseY  > 20 && mouseY < height-height/8) {
				mc.renderEngine.bindTexture(fillTexture);
				GL11.glPushMatrix();
				GL11.glColor3f((float) current.getRed() / 255f,
						(float) current.getGreen() / 255f,
						(float) current.getBlue() / 255f);
				this.drawModalRectWithCustomSizedTexture(mouseX + 1,
						mouseY - 12, 0, 0, 12, 12, 12, 12);
				GL11.glPopMatrix();
			}
			this.drawHoveringText(textLines, this.width+10, 20);
		} catch (Exception e) {
		}
	}

	protected void mouseClicked(int x, int y, int mouseButton)
			throws IOException {
		try {
			super.mouseClicked(x, y, mouseButton);
			int mouseX = cursor.x + brushSize / 2;
			int mouseY = cursor.y + brushSize / 2;
			Random r = new Random(noiseSeed);

			if (cursor.x >= 0 && cursor.y >= 0 && cursor.x <= 63 && cursor.y <= 63) {
				if (brushMode == 2) {
					Minecraft.getMinecraft().thePlayer.playSound("tsm:easel.fill", 0.5f, 1f);
					Color c = new Color(pixels[cursor.x+(cursor.y*64)], true);
					this.fillSpreadingColor(cursor.x, cursor.y, c, current);
				} else {
					for (int xx = (-brushSize - 1) / 2; xx < brushSize / 2; xx++) {
						for (int yy = (-brushSize - 1) / 2; yy < brushSize / 2; yy++) {
							int index = mouseX + xx + ((mouseY + yy) * 64);
							if (index < 0 || index > pixels.length - 1) {
								continue;
							}
							System.out.println("BrushMode: "+brushMode);
							switch (this.brushMode) {
							case 0:
								pixels[index] = current.getRGB();
								break;
							case 1:
								pixels[index] = none.getRGB();
								break;
							case 3:
								int colorI = pixels[index];
								if (colorI != none.getRGB()) {
									Color color = new Color(colorI, true);
									double amtChange = (-1d+(r.nextDouble()*2))*((double)(r.nextInt(noiseSlider.getNormalisedValue())+1)*10d);
									int red = color.getRed()+(int)amtChange;
									int green = color.getGreen()+(int)amtChange;
									int blue = color.getBlue()+(int)amtChange;
									red = Math.min(Math.max(0, red), 255);
									green = Math.min(Math.max(0, green), 255);
									blue = Math.min(Math.max(0, blue), 255);
									Color newColor = new Color(red,green,blue, 255);
									pixels[index] = newColor.getRGB();
								}
								break;
							}
						}
					}
				}
				noiseSeed = new Random().nextLong();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mouseClickMove(int x, int y, int clickMouseButton,
			long timeSince) {
		int mouseX = cursor.x + brushSize / 2;
		int mouseY = cursor.y + brushSize / 2;

		if (cursor.x >= 0 && cursor.y >= 0 && cursor.x <= 63 && cursor.y <= 63) {
			int dX = Mouse.getDX();
			int dY = Mouse.getDY();

			int dist = (int)Math.sqrt((dX*dX)+(dY*dY));
			int angle = (int)Math.toDegrees(Math.atan2(dY, dX));
			int maxDist = 64;

			double ratio = ((double)dist/(double)maxDist);
			if (ratio < 0d) ratio = 0d;
			if (ratio > 2d) ratio = 2d;
			if (this.brushMode == 0 && dist > 4) {
				if (angle > 0 && angle < 180) {
					Minecraft.getMinecraft().thePlayer.playSound("tsm:easel.draw.positive", 0.5f, (float)ratio/4f);
				} else {
					Minecraft.getMinecraft().thePlayer.playSound("tsm:easel.draw.negative", 0.5f, (float)ratio/4f);
				}
			}
			if (this.brushMode == 1 && dist > 4) {
				if (angle > 0 && angle < 180) {
					Minecraft.getMinecraft().thePlayer.playSound("tsm:easel.erase.positive", 0.5f, (float)ratio*2f);
				} else {
					Minecraft.getMinecraft().thePlayer.playSound("tsm:easel.erase.negative", 0.5f, (float)ratio*2f);
				}
			}
			for (int xx = (-brushSize - 1) / 2; xx < brushSize / 2; xx++) {
				for (int yy = (-brushSize - 1) / 2; yy < brushSize / 2; yy++) {
					int index = mouseX + xx + ((mouseY + yy) * 64);
					if (index < 0 || index > pixels.length - 1
							|| mouseX + xx < 0 || mouseX + xx >= 64
							|| mouseY + yy < 0 || mouseY + yy >= 64) {
						continue;
					}
					pixels[index] = this.brushMode == 0 ? current.getRGB()
							: this.brushMode == 1 ? none.getRGB()
									: pixels[index];
				}
			}
		}
	}

	public int getSelectedPixelX(int mouseX) {
		int x = -1;
		if (mouseX >= this.width / 2 - 64 && mouseX <= this.width / 2 + 64) {
			x = (mouseX - (this.width / 2 - 64)) / 2;
		}
		return x;
	}

	public int getSelectedPixelY(int mouseY) {
		int y = -1;
		if (mouseY >= this.height / 2 - 64 && mouseY <= this.height / 2 + 64) {
			y = (mouseY - (this.height / 2 - 64)) / 2;
		}
		return y;
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) { // Done button
			this.mc.displayGuiScreen((GuiScreen) null);
		}
		if (button.id == 1) { // Mode button
			String mode = "NULL";
			this.brushMode = (this.brushMode+1) % 5;
			switch (this.brushMode) {
			case 0: // draw
				mode = "Draw";
				this.redSlider.visible = true;
				this.greenSlider.visible = true;
				this.blueSlider.visible = true;
				this.brushButton.visible = true;
				this.gridToggle.visible = true;
				this.noiseSlider.visible = false;
				this.importButton.visible = false;
				break;
			case 1: // erase
				mode = "Erase";
				this.redSlider.visible = false;
				this.greenSlider.visible = false;
				this.blueSlider.visible = false;
				this.brushButton.visible = true;
				this.gridToggle.visible = true;
				this.noiseSlider.visible = false;
				this.importButton.visible = false;
				break;
			case 2: // fill
				mode = "Fill";
				this.redSlider.visible = true;
				this.greenSlider.visible = true;
				this.blueSlider.visible = true;
				this.brushButton.visible = false;
				this.gridToggle.visible = false;
				this.noiseSlider.visible = false;
				this.importButton.visible = false;
				break;
			case 3: // noise
				mode = "Noise";
				this.redSlider.visible = false;
				this.greenSlider.visible = false;
				this.blueSlider.visible = false;
				this.brushButton.visible = true;
				this.gridToggle.visible = true;
				this.noiseSlider.visible = true;
				this.importButton.visible = false;
				break;
			case 4: // import
				mode = "Import";
				this.redSlider.visible = false;
				this.greenSlider.visible = false;
				this.blueSlider.visible = false;
				this.brushButton.visible = false;
				this.gridToggle.visible = false;
				this.noiseSlider.visible = false;
				this.importButton.visible = true;
				break;
			}
			this.modeButton.displayString = "\u00a7bMode: \u00a7a\u00a7n"+mode;
		}
		if (button.id == 2) { // Brush button
			this.brushButtonState = (brushButtonState + 1) % 6;
			switch (this.brushButtonState) {
			case 0:
				this.brushSize = 1;
				break;
			case 1:
				this.brushSize = 2;
				break;
			case 2:
				this.brushSize = 4;
				break;
			case 3:
				this.brushSize = 8;
				break;
			case 4:
				this.brushSize = 16;
				break;
			case 5:
				this.brushSize = 32;
				break;
			}
			this.brushButton.displayString = "Brush Size: " + brushSize;
		}
		if (button.id == 3) { // Grid button
			this.grid = !grid;
			this.gridToggle.displayString = "Grid: "+(grid?"ON":"OFF");
		}
		if (button.id == 8) { // Import button
			JFileChooser chooser = new JFileChooser();
			FileFilter filter = new FileFilter(){

				@Override
				public boolean accept(File f) {
					if (f.isFile() && f.getPath().toLowerCase().endsWith(".png")) {
						return true;
					}
					if (f.isFile() && f.getPath().toLowerCase().endsWith(".gif")) {
						return true;
					}
					if (f.isFile() && f.getPath().toLowerCase().endsWith(".jpg")) {
						return true;
					}
					if (f.isFile() && f.getPath().toLowerCase().endsWith(".bmp")) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "Supported Image Files (*.png, *.gif, *.jpg, *.bmp)";
				}}; 
				chooser.setFileFilter(filter);
				chooser.setMultiSelectionEnabled(false);
				int action = chooser.showOpenDialog(Display.getParent());
				if (action == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					BufferedImage image = resize(ImageIO.read(file), 64, 64);
					for (int x = 0; x<64; x++) {
						for (int y = 0; y<64; y++) {
							Color c = new Color(image.getRGB(x, y), true);
							int r = c.getRed();
							int g = c.getGreen();
							int b = c.getBlue();
							int a = (int)(Math.round((double)c.getAlpha()/255d)*255d);

							if (a == 0) {
								r = 0;
								g = 0;
								b = 0;
							}

							pixels[x+(y*64)] = new Color(r,g,b,a).getRGB();
						}
					}
				}
				Minecraft.getMinecraft().thePlayer.playSound("tsm:easel.import", 1f, 1f);
		}
	}

	public void fillSpreadingColor(int x, int y, Color test, Color set) {
		if (x >= 0 && x < 64 && y >= 0 && y < 64) {
			if (test.getRGB() == set.getRGB()) {
				return;
			}
			if (pixels[x+(y*64)] != test.getRGB()) {
				return;
			}
			pixels[x+(y*64)] = set.getRGB();
			fillSpreadingColor(x-1,y,test,set);
			fillSpreadingColor(x+1,y,test,set);
			fillSpreadingColor(x,y-1,test,set);
			fillSpreadingColor(x,y+1,test,set);
		}
	}

	private BufferedImage resize(BufferedImage img, int newW, int newH) {  
		int w = img.getWidth();  
		int h = img.getHeight();  
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());  
		Graphics2D g = dimg.createGraphics();  
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);  
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
		g.dispose();  
		return dimg;  
	}  

}
