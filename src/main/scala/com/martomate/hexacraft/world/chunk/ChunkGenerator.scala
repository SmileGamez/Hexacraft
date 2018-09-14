package com.martomate.hexacraft.world.chunk

import com.flowpowered.nbt.CompoundTag
import com.martomate.hexacraft.world.block.{BlockState, Blocks}
import com.martomate.hexacraft.world.coord.integer.{BlockRelChunk, ChunkRelWorld}
import com.martomate.hexacraft.world.storage.chunk.{ChunkStorage, DenseChunkStorage}
import com.martomate.hexacraft.world.storage.IWorld

class ChunkGenerator(coords: ChunkRelWorld, world: IWorld) {
  private def filePath: String = "data/" + coords.getColumnRelWorld.value + "/" + coords.getChunkRelColumn.value + ".dat"

  def loadData(): ChunkData = {
    val nbt = world.worldSettings.loadState(filePath)

    val storage: ChunkStorage = new DenseChunkStorage(coords)
    if (!nbt.getValue.isEmpty) {
      storage.fromNBT(nbt)
    } else {
      val column = world.getColumn(coords.getColumnRelWorld).get

      val blockNoise = world.worldGenerator.getBlockInterpolator(coords)

      for (i <- 0 until 16; j <- 0 until 16; k <- 0 until 16) {
        val noise = blockNoise(i, j, k)
        val yToGo = coords.Y * 16 + j - column.generatedHeightMap(i)(k)
        val limit = limitForBlockNoise(yToGo)
        if (noise > limit) storage.setBlock(BlockRelChunk(i, j, k, coords.cylSize), new BlockState(getBlockAtDepth(yToGo)))
      }
    }
    val data = new ChunkData
    data.storage = storage
    data
  }

  def saveData(data: CompoundTag): Unit = {
    world.worldSettings.saveState(data, filePath)
  }

  private def getBlockAtDepth(yToGo: Int) = {
    if (yToGo < -5) Blocks.Stone
    else if (yToGo < -1) Blocks.Dirt
    else Blocks.Grass
  }

  private def limitForBlockNoise(yToGo: Int): Double = {
    if (yToGo < -6) -0.4
    else if (yToGo < 0) -0.4 - (6 + yToGo) * 0.025
    else 4
  }
}
