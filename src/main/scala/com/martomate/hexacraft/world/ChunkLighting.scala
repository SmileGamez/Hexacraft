package com.martomate.hexacraft.world

import com.martomate.hexacraft.block.BlockState
import com.martomate.hexacraft.world.coord.BlockRelChunk
import com.martomate.hexacraft.world.render.LightPropagator
import com.martomate.hexacraft.world.storage.{Chunk, IWorld}

import scala.collection.mutable

class ChunkLighting(lightPropagator: LightPropagator) {
  private val brightness: Array[Byte] = new Array(16*16*16)
  private var brightnessInitialized: Boolean = false

  def initialized: Boolean = brightnessInitialized

  def init(chunk: Chunk, blocks: Seq[(BlockRelChunk, BlockState)]): Unit = {
    if (!brightnessInitialized) {
      brightnessInitialized = true
      val lights = mutable.HashMap.empty[BlockRelChunk, BlockState]

      for ((c, b) <- blocks) {
        if (b.blockType.lightEmitted != 0) lights(c) = b
      }

      lightPropagator.initBrightnesses(chunk, lights)
    }
  }

  def setSunlight(coords: BlockRelChunk, value: Int): Unit = {
    brightness(coords.value) = (brightness(coords.value) & 0xf | value << 4).toByte
  }

  def getSunlight(coords: BlockRelChunk): Byte = {
    ((brightness(coords.value) >> 4) & 0xf).toByte
  }

  def setTorchlight(coords: BlockRelChunk, value: Int): Unit = {
    brightness(coords.value) = (brightness(coords.value) & 0xf0 | value).toByte
  }

  def getTorchlight(coords: BlockRelChunk): Byte = {
    (brightness(coords.value) & 0xf).toByte
  }

  def getBrightness(block: BlockRelChunk): Float = {
    math.min((getTorchlight(block) + getSunlight(block)) / 15f, 1.0f)
  }
}
