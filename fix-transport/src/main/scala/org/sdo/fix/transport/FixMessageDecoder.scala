package org.sdo.fix.transport

import java.util

import io.netty.buffer.{ByteBuf, ByteBufProcessor}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
  * Created by dsemenov
  * Date: 5/6/16.
  */
class FixMessageDecoder extends ByteToMessageDecoder {

  class FixScan extends ByteBufProcessor {
    val matcher = "\u000110=".getBytes()
    var matches = 0

    override def process(value: Byte): Boolean = {
      matches = if (value == matcher(matches)) {
        matches + 1
      } else {
        0
      }
      matches != matcher.length
    }

    def reset = {
      matches = 0
    }
  }

  val dfa = new FixScan

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
    val footerPos: Int = in.forEachByte(dfa)
    if (footerPos > -1) {
      val endOfMessage = in.forEachByte(footerPos, 5, new ByteBufProcessor {
        override def process(value: Byte): Boolean = value != 0x01
      }) + 1
      out.add(in.readSlice(endOfMessage - in.readerIndex()).retain())
      dfa.reset
    }
  }
}
