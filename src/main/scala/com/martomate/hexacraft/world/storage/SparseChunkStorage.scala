package com.martomate.hexacraft.world.storage

import com.flowpowered.nbt.{ByteArrayTag, CompoundTag, Tag}
import com.martomate.hexacraft.util.{ConstantSeq, CylinderSize, NBTUtil}
import com.martomate.hexacraft.world.block.state.BlockState
import com.martomate.hexacraft.world.block.{Block, Blocks}
import com.martomate.hexacraft.world.coord.integer.{BlockRelChunk, ChunkRelWorld}

import scala.collection.mutable

class SparseChunkStorage(_chunkCoords: ChunkRelWorld)(implicit cylSize: CylinderSize) extends ChunkStorage(_chunkCoords) {
  def this(storage: ChunkStorage)(implicit cylSize: CylinderSize) = {
    this(storage.chunkCoords)
    for ((i, b) <- storage.allBlocks) setBlock(i, b)
  }

  private val blocks = mutable.LongMap.empty[BlockState]

  def blockType(coords: BlockRelChunk): Block = blocks.get(coords.value.toShort).map(_.blockType).getOrElse(Blocks.Air)
  def getBlock(coords: BlockRelChunk): BlockState = blocks.getOrElse(coords.value.toShort, BlockState.Air)
  def mapBlock[T](coords: BlockRelChunk, func: (Block, Byte) => T): T = {
    val s = blocks.getOrElse(coords.value, BlockState.Air)
    func(s.blockType, s.metadata)
  }
  def setBlock(coords: BlockRelChunk, block: BlockState): Unit = {
    if (block.blockType != Blocks.Air) blocks(coords.value) = block
    else removeBlock(coords)
  }
  def removeBlock(coords: BlockRelChunk): Unit = blocks -= coords.value
  def allBlocks: Seq[(BlockRelChunk, BlockState)] = blocks.toSeq.map(t => (BlockRelChunk(t._1.toInt), t._2))
  def numBlocks: Int = blocks.size
  def isDense: Boolean = false

  def fromNBT(nbt: CompoundTag): Unit = {
    val blocks = NBTUtil.getByteArray(nbt, "blocks").getOrElse(new ConstantSeq[Byte](16*16*16, 0))
    val meta = NBTUtil.getByteArray(nbt, "metadata").getOrElse(new ConstantSeq[Byte](16*16*16, 0))

    for (i <- blocks.indices) {
      if (blocks(i) != 0) {
        setBlock(BlockRelChunk(i), new BlockState(Block.byId(blocks(i)), meta(i)))
//        chunk.requestBlockUpdate(BlockRelChunk(i, chunk.world))
      }
    }
  }

  def toNBT: Seq[Tag[_]] = {
    val ids = Array.tabulate[Byte](16*16*16)(i => blocks.get(i.toShort).map(_.blockType.id).getOrElse(0))
    val meta = Array.tabulate[Byte](16*16*16)(i => blocks.get(i.toShort).map(_.metadata).getOrElse(0))
    Seq(new ByteArrayTag("blocks", ids),
        new ByteArrayTag("metadata", meta))
  }
}
