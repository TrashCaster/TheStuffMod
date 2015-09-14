package com.thestuffmod.network.packet;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;


public interface IPacket
{
    public void readBytes(ByteBuf bytes);
    public void writeBytes(ByteBuf bytes);
    @SideOnly(Side.CLIENT)
    public void handleClientSide(NetHandlerPlayClient nhClient);
    public void handleServerSide(NetHandlerPlayServer nhServer);
}
