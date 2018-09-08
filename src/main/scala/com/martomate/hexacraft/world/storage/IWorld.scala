package com.martomate.hexacraft.world.storage

import com.martomate.hexacraft.world.CylinderSize

trait IWorld extends ChunkEventListener with BlockSetAndGet with BlocksInWorld {
  def size: CylinderSize
  def worldSettings: WorldSettingsProvider
  def worldGenerator: WorldGenerator
  def renderDistance: Double

  def getHeight(x: Int, z: Int): Int

  private[storage] def chunkAddedOrRemovedListeners: Iterable[ChunkAddedOrRemovedListener]
  def addChunkAddedOrRemovedListener(listener: ChunkAddedOrRemovedListener): Unit
}
