package com.martomate.hexacraft.world.render

import java.nio.ByteBuffer

import scala.collection.mutable.ArrayBuffer

abstract class BufferHandler[T <: RenderBuffer](bufferFactory: RenderBufferFactory[T]) {
  private val InstancesPerBuffer: Int = 100000
  protected val BufSize: Int = bufferFactory.bytesPerInstance * InstancesPerBuffer

  protected val buffers: ArrayBuffer[T] = ArrayBuffer.empty

  def set(seg: Segment, data: ByteBuffer): Unit = {
    require(seg.length <= data.remaining())
    val startBuf = seg.start / BufSize
    val endBuf = (seg.start + seg.length - 1) / BufSize
    var left = seg.length
    var pos = seg.start
    for (i <- startBuf to endBuf) {
      val start = pos % BufSize
      val amt = math.min(left, BufSize - start)

      pos += amt
      left -= amt

      if (i >= buffers.length)
        buffers += bufferFactory.makeBuffer(InstancesPerBuffer)

      buffers(i).set(start, amt, data)
    }
  }

  /** This probably only works if to <= from */
  def copy(from: Int, to: Int, len: Int): Unit = {
    var fromBuffer: Int = from / BufSize
    var fromIdx: Int = from % BufSize
    var toBuffer: Int = to / BufSize
    var toIdx: Int = to % BufSize
    var left: Int = len

    while (left > 0) {
      val leftF = BufSize - fromIdx
      val leftT = BufSize - toIdx
      val len = math.min(math.min(leftF, leftT), left)

      copyInternal(fromBuffer, fromIdx, toBuffer, toIdx, len)

      fromIdx = (fromIdx + len) % BufSize
      toIdx = (toIdx + len) % BufSize

      if (leftF == len) fromBuffer += 1
      if (leftT == len) toBuffer += 1

      left -= len
    }
  }

  protected def copyInternal(fromBuffer: Int, fromIdx: Int, toBuffer: Int, toIdx: Int, len: Int): Unit

  def render(length: Int): Unit = {
    for (i <- 0 to (length - 1) / BufSize) {
      buffers(i).render(math.min(length - i * BufSize, BufSize))
    }
  }

  def unload(): Unit = ()
}
