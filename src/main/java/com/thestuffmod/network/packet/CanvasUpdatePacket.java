package com.thestuffmod.network.packet;

import io.netty.buffer.ByteBuf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.thestuffmod.TSM;
import com.thestuffmod.entity.item.EntityEasel;

public class CanvasUpdatePacket implements IPacket {

	int[] pixelData;
	UUID paintingID = null;

	public CanvasUpdatePacket() {

	}

	public CanvasUpdatePacket(int[] pixelData, UUID paintingID) {
		this.pixelData = pixelData;
		this.paintingID = paintingID;
	}

	@Override
	public void readBytes(ByteBuf bytes) {
		int pixelDataLength = bytes.readInt(); // should always be 64*64
		this.pixelData = new int[pixelDataLength];
		for (int i = 0; i < pixelDataLength; i++) {
			pixelData[i] = bytes.readInt();
		}
		int idLength = bytes.readInt();
		char[] idChars = new char[idLength];
		
		for (int i=0; i<idLength; i++) {
			idChars[i] = bytes.readChar();
		}
		
		String id = new String(idChars);
		
		if (id != null && UUID.fromString(id) != null) {
			this.paintingID = UUID.fromString(id);
		}
	}

	@Override
	public void writeBytes(ByteBuf bytes) {
		int pixelDataLength = pixelData.length; // should always be 64*64
		bytes.writeInt(pixelDataLength);
		for (int i = 0; i < pixelDataLength; i++) {
			bytes.writeInt(pixelData[i]);
		}
		String id = this.paintingID.toString();
		int idLength = id.length(); // should always be 64*64
		bytes.writeInt(idLength);
		for (int i = 0; i < idLength; i++) {
			bytes.writeChar(id.charAt(i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(NetHandlerPlayClient nhClient) {
		UUID id = this.paintingID;
		TSM.proxy.updateImage(id, pixelData);
		TSM.canvasUpdateQueue.add(id);
	}

	@Override
	public void handleServerSide(final NetHandlerPlayServer nhServer) {
		List<EntityEasel> easels = nhServer.playerEntity.worldObj.getEntities(
				EntityEasel.class, new Predicate() {
					public boolean apply(EntityEasel entity) {
						return entity.getPositionVector().distanceTo(
								nhServer.playerEntity.getPositionVector()) < 8f;
					}

					public boolean apply(Object object) {
						return this.apply((EntityEasel) object);
					}
				});
		EntityEasel easel = null;
		for (EntityEasel e : easels) {
			if (e.getEditedByPlayer() != null
					&& e.getEditedByPlayer().getUniqueID()
							.equals(nhServer.playerEntity.getUniqueID())) {
				easel = e;
				break;
			}
		}
		if (easel != null) {
			if (easel.getCanvas() != null) {
				ItemStack stack = easel.getCanvas().copy();
				NBTTagCompound tag = stack.getTagCompound();
				if (tag == null) {
					tag = new NBTTagCompound();
				}
				tag.setIntArray("PixelData", this.pixelData);
				tag.setBoolean("Update", true);
				stack.setTagCompound(tag);
				stack.getItem().onUpdate(stack, nhServer.playerEntity.worldObj,
						null, 0, false);
				easel.setCanvas(stack);
				easel.setEditedByPlayer(null);
				nhServer.playerEntity.closeContainer();
				TSM.packetManager.sendToAll(new CanvasUpdatePacket(this.pixelData,this.paintingID));
			}
		}

	}

}
