package com.martomate.hexacraft.world.storage

import com.flowpowered.nbt.ByteArrayTag
import com.martomate.hexacraft.util.{CylinderSize, NBTUtil}
import com.martomate.hexacraft.world.block.Blocks
import com.martomate.hexacraft.world.block.state.BlockState
import com.martomate.hexacraft.world.coord.integer.{BlockRelChunk, BlockRelWorld, ChunkRelWorld}
import org.scalatest.FunSuite

abstract class ChunkStorageTest(protected val storageFactory: (ChunkRelWorld, CylinderSize) => ChunkStorage) extends FunSuite {
  protected val cylSize: CylinderSize = new CylinderSize(4)
  import cylSize.impl

  def makeStorage(coords: ChunkRelWorld = ChunkRelWorld(0)): ChunkStorage = storageFactory(coords, cylSize)

  test("No blocks") {
    val storage = makeStorage()
    assertResult(0)(storage.numBlocks)
  }

  test("One block") {
    val storage = makeStorage()
    storage.setBlock(coords350.getBlockRelChunk, new BlockState(Blocks.Dirt))
    storage.setBlock(coords350.getBlockRelChunk, new BlockState(Blocks.Dirt))
    assertResult(1)(storage.numBlocks)
  }

  test("Many blocks") {
    val storage = makeStorage()
    for (i <- 0 until 16; j <- 0 until 16; k <- 0 until 16)
      storage.setBlock(BlockRelChunk(i, j, k), new BlockState(Blocks.Dirt))

    assertResult(16*16*16)(storage.numBlocks)
  }

  test("Air doesn't count as a block") {
    val storage = makeStorage()
    storage.setBlock(coords350.getBlockRelChunk, new BlockState(Blocks.Dirt))
    storage.setBlock(coords350.getBlockRelChunk, new BlockState(Blocks.Air))
    assertResult(0)(storage.numBlocks)
  }

  test("blockType for existing block") {
    val storage: ChunkStorage = makeStorage_Dirt359_Stone350

    assertResult(Blocks.Stone)(storage.blockType(coords350.getBlockRelChunk))
  }

  test("blockType for non-existing block") {
    val storage: ChunkStorage = makeStorage_Dirt359_Stone350


    assertResult(Blocks.Air)(storage.blockType(coords351.getBlockRelChunk))
  }

  test("Remove existing block") {
    val storage: ChunkStorage = makeStorage_Dirt359_Stone350

    storage.removeBlock(coords350.getBlockRelChunk)
    assertResult(Blocks.Air)(storage.blockType(coords350.getBlockRelChunk))
    assertResult(1)(storage.numBlocks)
  }

  test("Remove non-existing block") {
    val storage: ChunkStorage = makeStorage_Dirt359_Stone350

    storage.removeBlock(coords351.getBlockRelChunk)
    assertResult(Blocks.Stone)(storage.blockType(coords350.getBlockRelChunk))
    assertResult(2)(storage.numBlocks)
  }

  test("getBlock for existing block") {
    val storage = makeStorage_Dirt359_Stone350
    val block = storage.getBlock(coords350.getBlockRelChunk)
    assertResult(Blocks.Stone)(block.blockType)
    assertResult(2)(block.metadata)
    assertResult(2)(block.metadata)
  }

  test("getBlock for non-existing block") {
    val storage = makeStorage_Dirt359_Stone350
    val block = storage.getBlock(coords351.getBlockRelChunk)
    assertResult(Blocks.Air)(block.blockType)
  }

  test("allBlocks returns all blocks") {
    val storage = makeStorage_Dirt359_Stone350
    val storageSize = storage.numBlocks

    val all = storage.allBlocks
    assertResult(storageSize)(all.size)
    for ((c, b) <- all) assertResult(b)(storage.getBlock(c))
  }

  test("fromNBT with correct tag") {
    val tag = NBTUtil.makeCompoundTag("", Seq(
      new ByteArrayTag("blocks", Array.tabulate(16*16*16) {
        case 0 => Blocks.Dirt.id
        case 1 => Blocks.Stone.id
        case _ => 0
      }),
      new ByteArrayTag("metadata", Array.tabulate(16*16*16) {
        case 0 => 6
        case 1 => 2
        case _ => 0
      })
    ))
    val storage = makeStorage(ChunkRelWorld(0))
    storage.fromNBT(tag)
    assertResult(2)(storage.numBlocks)
    assertResult(Blocks.Stone)(storage.blockType(coordsAt(0, 0, 1).getBlockRelChunk))
  }

  test("fromNBT without metadata") {
    val tag = NBTUtil.makeCompoundTag("", Seq(
      new ByteArrayTag("blocks", Array.tabulate(16*16*16) {
        case 0 => Blocks.Dirt.id
        case 1 => Blocks.Stone.id
        case _ => 0
      })
    ))
    val storage = makeStorage(ChunkRelWorld(0))
    storage.fromNBT(tag)
    assertResult(2)(storage.numBlocks)
    assertResult(Blocks.Stone)(storage.blockType(coordsAt(0, 0, 1).getBlockRelChunk))
  }

  test("fromNBT without blocks") {
    val tag = NBTUtil.makeCompoundTag("", Seq(
      new ByteArrayTag("metadata", Array.tabulate(16*16*16) {
        case 0 => 6
        case 1 => 2
        case _ => 0
      })
    ))
    val storage = makeStorage(ChunkRelWorld(0))
    storage.fromNBT(tag)
    assertResult(0)(storage.numBlocks)
    assertResult(Blocks.Air)(storage.blockType(coordsAt(0, 0, 1).getBlockRelChunk))
  }

  test("toNBT works") {
    val storage = makeStorage_Dirt359_Stone350
    val nbt = storage.toNBT
    assertResult(2)(nbt.size)

    assertResult("blocks")(nbt(0).getName)
    assert(nbt(0).isInstanceOf[ByteArrayTag])
    val blocksArray = nbt(0).asInstanceOf[ByteArrayTag].getValue
    assertResult(16*16*16)(blocksArray.length)

    assertResult("metadata")(nbt(1).getName)
    assert(nbt(1).isInstanceOf[ByteArrayTag])
    val metadataArray = nbt(1).asInstanceOf[ByteArrayTag].getValue
    assertResult(16*16*16)(metadataArray.length)

    assertResult(Blocks.Dirt.id)(blocksArray(coords359.getBlockRelChunk.value))
    assertResult(Blocks.Stone.id)(blocksArray(coords350.getBlockRelChunk.value))
    assertResult(6)(metadataArray(coords359.getBlockRelChunk.value))
    assertResult(2)(metadataArray(coords350.getBlockRelChunk.value))
  }
  
  protected def coords350: BlockRelWorld = coordsAt(3, 5, 0)
  protected def coords351: BlockRelWorld = coordsAt(3, 5, 1)
  protected def coords359: BlockRelWorld = coordsAt(3, 5, 9)
  protected def coordsAt(x: Int, y: Int, z: Int): BlockRelWorld = BlockRelWorld(x, y, z)
  protected def cc0: ChunkRelWorld = ChunkRelWorld(0)

  protected def makeStorage_Dirt359_Stone350: ChunkStorage = {
    val storage = makeStorage(ChunkRelWorld(0))
    fillStorage_Dirt359_Stone350(storage)
    storage
  }

  protected def fillStorage_Dirt359_Stone350(storage: ChunkStorage): Unit = {
    storage.setBlock(coords359.getBlockRelChunk, new BlockState(Blocks.Dirt, 6))
    storage.setBlock(coords350.getBlockRelChunk, new BlockState(Blocks.Stone, 2))
  }
}
