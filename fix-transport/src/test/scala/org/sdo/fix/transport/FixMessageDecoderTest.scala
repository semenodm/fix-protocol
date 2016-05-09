package org.sdo.fix.transport

import java.util

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.embedded.EmbeddedChannel
import org.scalatest.FlatSpec

/**
  * Created by dsemenov
  * Date: 5/6/16.
  */
class FixMessageDecoderTest extends FlatSpec {
  behavior of "A FIX message Decoder"
  val fixMessage = "8=FIX.4.0\u00019=215\u000135=D\u000149=SENDER\u000156=TARGET\u000134=68\u000150=SENDER_SUB\u000152=20150112-13:43:28\u000143=N\u000197=N\u000111=12345\u00011=ACT1\u000121=1\u0001100=N\u000155=APPL\u000148=seqid1\u000122=2\u0001106=Universal Corporation\u0001107=Universal Corporation\u000154=1\u000138=60\u000140=2\u000144=41\u000115=USD\u000159=0\u000110=051\u0001"

  it should "decode single FIX message" in {
    val decoder = new FixMessageDecoder

    val channel = new EmbeddedChannel(decoder)


    channel.writeInbound(Unpooled.wrappedBuffer(fixMessage.getBytes))

    val messages: util.Queue[AnyRef] = channel.inboundMessages()
    val message = messages.poll().asInstanceOf[ByteBuf]
    val bytes = new Array[Byte](message.readableBytes())
    message.readBytes(bytes)
    assert(new String(bytes) == fixMessage)
  }

  it should "decode single FIX message from byte buffer with one and following part of the message" in {
    val decoder = new FixMessageDecoder

    val channel = new EmbeddedChannel(decoder)
    channel.writeInbound(Unpooled.wrappedBuffer((fixMessage + fixMessage.substring(0, 10)).getBytes))

    val messages: util.Queue[AnyRef] = channel.inboundMessages()
    val message = messages.poll().asInstanceOf[ByteBuf]
    val bytes = new Array[Byte](message.readableBytes())
    message.readBytes(bytes)
    assert(new String(bytes) == fixMessage)
  }

  it should "decode buffer with partial message followed with remaining part" in {
    val decoder = new FixMessageDecoder

    val channel = new EmbeddedChannel(decoder)
    val firstPart = fixMessage.substring(0, 10)
    val secondPart = fixMessage.substring(10, fixMessage.length) + fixMessage.substring(0, 15)

    channel.writeInbound(Unpooled.wrappedBuffer(firstPart.getBytes))
    channel.writeInbound(Unpooled.wrappedBuffer(secondPart.getBytes))


    val messages: util.Queue[AnyRef] = channel.inboundMessages()
    val message = messages.poll().asInstanceOf[ByteBuf]
    val bytes = new Array[Byte](message.readableBytes())
    message.readBytes(bytes)
    assert(new String(bytes) == fixMessage)
  }
  it should "handle more then one message in one byte buffer" in {
    val decoder = new FixMessageDecoder

    val channel = new EmbeddedChannel(decoder)
    val firstPart = fixMessage + fixMessage

    channel.writeInbound(Unpooled.wrappedBuffer(firstPart.getBytes))


    val messages: util.Queue[AnyRef] = channel.inboundMessages()
    var message = messages.poll().asInstanceOf[ByteBuf]
    var bytes = new Array[Byte](message.readableBytes())
    message.readBytes(bytes)
    assert(new String(bytes) == fixMessage)

    message = messages.poll().asInstanceOf[ByteBuf]
    bytes = new Array[Byte](message.readableBytes())
    message.readBytes(bytes)
    assert(new String(bytes) == fixMessage)

  }
}
