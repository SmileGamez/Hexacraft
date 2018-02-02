package hexagon.world

import java.util.Observable

import hexagon.block.Block

class Inventory extends Observable {
  private val slots = Seq(Block.Dirt, Block.Grass, Block.Sand, Block.Stone, Block.Water) ++ Seq.fill(4)(Block.Air)

  def apply(idx: Int): Block = slots(idx)

  def setHasChanged(): Unit = {
    setChanged()
    notifyObservers()
  }
}