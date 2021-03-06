package com.martomate.hexacraft.world

import com.martomate.hexacraft.util.NBTUtil
import com.martomate.hexacraft.world.block.Block
import com.martomate.hexacraft.world.block.state.BlockState
import com.martomate.hexacraft.world.chunk._
import com.martomate.hexacraft.world.coord.integer.{BlockRelChunk, BlockRelWorld, ChunkRelWorld}
import com.martomate.hexacraft.world.lighting.{ChunkLighting, LightPropagator}
import com.martomate.hexacraft.world.storage.{ChunkData, ChunkStorage}

import scala.collection.mutable.ArrayBuffer

class Chunk(val coords: ChunkRelWorld, generator: IChunkGenerator, lightPropagator: LightPropagator) extends IChunk {
  private val chunkData: ChunkData = generator.loadData()

  private def storage: ChunkStorage = chunkData.storage
  private var needsToSave = false

  private val eventListeners: ArrayBuffer[ChunkEventListener] = ArrayBuffer.empty
  def addEventListener(listener: ChunkEventListener): Unit = eventListeners += listener
  def removeEventListener(listener: ChunkEventListener): Unit = eventListeners -= listener

  private val blockEventListeners: ArrayBuffer[ChunkBlockListener] = ArrayBuffer.empty
  def addBlockEventListener(listener: ChunkBlockListener): Unit = blockEventListeners += listener
  def removeBlockEventListener(listener: ChunkBlockListener): Unit = blockEventListeners -= listener

  val lighting: IChunkLighting = new ChunkLighting(this, lightPropagator)
  override def entities: EntitiesInChunk = chunkData.entities

  def init(): Unit = {
    requestRenderUpdate()
    requestRenderUpdateForAllNeighbors()
  }

  def blocks: ChunkStorage = storage

  def getBlock(coords: BlockRelChunk): BlockState = storage.getBlock(coords)

  def mapBlock[T](coords: BlockRelChunk, func: (Block, Byte) => T): T = storage.mapBlock(coords, func)

  def setBlock(blockCoords: BlockRelChunk, block: BlockState): Unit = {
    val before = getBlock(blockCoords)
    if (before != block) {
      storage.setBlock(blockCoords, block)
      needsToSave = true

      for (listener <- blockEventListeners) {
        listener.onSetBlock(BlockRelWorld.fromChunk(blockCoords, coords), before, block)
      }

      handleLightingOnSetBlock(blockCoords, block)
    }
  }

  def removeBlock(coords: BlockRelChunk): Unit = setBlock(coords, BlockState.Air)

  private def handleLightingOnSetBlock(blockCoords: BlockRelChunk, block: BlockState): Unit = {
    lightPropagator.removeTorchlight(this, blockCoords)
    lightPropagator.removeSunlight(this, blockCoords)
    if (block.blockType.lightEmitted != 0) {
      lightPropagator.addTorchlight(this, blockCoords, block.blockType.lightEmitted)
    }
  }

  def requestBlockUpdate(coords: BlockRelChunk): Unit = eventListeners.foreach(_.onBlockNeedsUpdate(BlockRelWorld.fromChunk(coords, this.coords)))

  def requestRenderUpdate(): Unit = eventListeners.foreach(_.onChunkNeedsRenderUpdate(coords))

  private def requestRenderUpdateForAllNeighbors(): Unit =
    for (side <- 0 until 8)
      eventListeners.foreach(_.onChunksNeighborNeedsRenderUpdate(coords, side))

  def tick(): Unit = {
    chunkData.optimizeStorage()

    for (ent <- entities.allEntities) {
      ent.tick()
    }
  }

  def hasNoBlocks: Boolean = storage.numBlocks == 0

  def saveIfNeeded(): Unit = {
    if (needsToSave || entities.needsToSave) {
      save()
    }
  }

  def unload(): Unit = {
    saveIfNeeded()

    requestRenderUpdateForAllNeighbors()

    blockEventListeners.clear()
    eventListeners.clear()
  }

  private def save(): Unit = {
    val chunkTag = NBTUtil.makeCompoundTag("chunk", chunkData.toNBT) // Add more tags with ++
    generator.saveData(chunkTag)
    needsToSave = false
  }

  override def isDecorated: Boolean = chunkData.isDecorated
  override def setDecorated(): Unit = {
    if (!chunkData.isDecorated) {
      chunkData.isDecorated = true
      needsToSave = true
      save()
    }
  }
}
