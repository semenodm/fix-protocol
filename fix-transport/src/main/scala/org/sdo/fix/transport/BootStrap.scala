package org.sdo.fix.transport

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

/**
  * Created by dsemenov
  * Date: 5/3/16.
  */

@Sharable
class MyMessageHandler extends SimpleChannelInboundHandler[ByteBuf] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf): Unit = {
    val readableBytes: Int = msg.readableBytes()
    val b = new Array[Byte](readableBytes)
    val bytes: ByteBuf = msg.readBytes(b, 0, readableBytes)
    println(new String(b, "UTF-8"))
    ctx.writeAndFlush(Unpooled.buffer().writeBytes("OK!!!!".getBytes("UTF-8")))

  }
}

object BootStrap extends App {

  val bossGroup = new NioEventLoopGroup(1)
  val eventGroup = new NioEventLoopGroup


  try {
    val b = new ServerBootstrap()
    b.group(bossGroup, eventGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new MyMessageHandler)

    val serverStartFuture: ChannelFuture = b.bind(8130).sync()
    serverStartFuture.channel().closeFuture().sync()
  } finally {
    eventGroup.shutdownGracefully()
    bossGroup.shutdownGracefully()
  }

}
