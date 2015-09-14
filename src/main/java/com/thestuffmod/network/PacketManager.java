package com.thestuffmod.network;

import java.util.*;

import org.apache.logging.log4j.Level;

import com.thestuffmod.network.packet.IPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet pipeline class. Directs all registered packet data to be handled by
 * the packets themselves.
 * 
 * @author sirgingalot some code from: cpw
 */
@ChannelHandler.Sharable
public class PacketManager extends FMLIndexedMessageToMessageCodec<IPacket> {
	public EnumMap<Side, FMLEmbeddedChannel> channels;
	private LinkedList<Class<? extends IPacket>> packets = new LinkedList<Class<? extends IPacket>>();
	private boolean isPostInitialised = false;
	private final String logger;
	private final String channelName;

	public PacketManager(String logName, String modChannel) {
		this.logger = logName;
		this.channelName = modChannel;
	}

	public boolean registerPacket(Class<? extends IPacket> clazz) {
		if (this.packets.size() > 256) {
			FMLLog.log(
					this.logger,
					Level.ERROR,
					"Cannot register more than 256 packets! Packet %s not registered!",
					clazz.getName());
			return false;
		}

		if (this.packets.contains(clazz)) {
			FMLLog.log(this.logger, Level.ERROR,
					"Packet %s already registered!", clazz.getName());
			return false;
		}

		if (this.isPostInitialised) {
			FMLLog.log(
					this.logger,
					Level.ERROR,
					"Packet %s cannot be registered during / after Post-Initialization!",
					clazz.getName());
			return false;
		}

		this.addDiscriminator(this.packets.size(), clazz);
		this.packets.add(clazz);
		return true;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, IPacket msg,
			ByteBuf target) throws Exception {
		msg.writeBytes(target);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source,
			IPacket msg) {
		msg.readBytes(source);
		INetHandler netHandler = ctx.channel()
				.attr(NetworkRegistry.NET_HANDLER).get();
		if (netHandler instanceof NetHandlerPlayServer) {
			msg.handleServerSide((NetHandlerPlayServer) netHandler);
		} else if (netHandler instanceof NetHandlerPlayClient) {
			msg.handleClientSide((NetHandlerPlayClient) netHandler);
		}
	}

	public void initialise() {
		this.channels = NetworkRegistry.INSTANCE.newChannel(this.channelName, this);
	}

	public void postInitialise() {
		if (this.isPostInitialised) {
			return;
		}

		this.isPostInitialised = true;
		Collections.sort(this.packets,
				new Comparator<Class<? extends IPacket>>() {
					@Override
					public int compare(Class<? extends IPacket> clazz1,
							Class<? extends IPacket> clazz2) {
						int com = String.CASE_INSENSITIVE_ORDER.compare(
								clazz1.getCanonicalName(),
								clazz2.getCanonicalName());
						if (com == 0) {
							com = clazz1.getCanonicalName().compareTo(
									clazz2.getCanonicalName());
						}

						return com;
					}
				});
	}

	@SideOnly(Side.CLIENT)
	private EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	/**
	 * Send this message to everyone.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 * 
	 * @param message
	 *            The message to send
	 */
	public void sendToAll(IPacket message) {
		this.channels.get(Side.SERVER)
				.attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.ALL);
		this.channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to the specified player.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 * 
	 * @param message
	 *            The message to send
	 * @param player
	 *            The player to send it to
	 */
	public void sendTo(IPacket message, EntityPlayerMP player) {
		this.channels.get(Side.SERVER)
				.attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.PLAYER);
		this.channels.get(Side.SERVER)
				.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		this.channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to everyone within a certain range of a point.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 * 
	 * @param message
	 *            The message to send
	 * @param point
	 *            The
	 *            {@link cpw.mods.fml.common.network.NetworkRegistry.TargetPoint}
	 *            around which to send
	 */
	public void sendToAllAround(IPacket message,
			NetworkRegistry.TargetPoint point) {
		this.channels.get(Side.SERVER)
				.attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
		this.channels.get(Side.SERVER)
				.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
		this.channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to everyone within the supplied dimension.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 * 
	 * @param message
	 *            The message to send
	 * @param dimensionId
	 *            The dimension id to target
	 */
	public void sendToDimension(IPacket message, int dimensionId) {
		this.channels.get(Side.SERVER)
				.attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.DIMENSION);
		this.channels.get(Side.SERVER)
				.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
				.set(dimensionId);
		this.channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to the server.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 * 
	 * @param message
	 *            The message to send
	 */
	public void sendToServer(IPacket message) {
		this.channels.get(Side.CLIENT)
				.attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		this.channels.get(Side.CLIENT).writeAndFlush(message);
	}
}