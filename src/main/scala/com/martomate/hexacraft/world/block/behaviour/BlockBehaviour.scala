package com.martomate.hexacraft.world.block.behaviour

import com.martomate.hexacraft.world.block.setget.BlockSetAndGet
import com.martomate.hexacraft.world.coord.integer.BlockRelWorld

trait BlockBehaviour {
  def onUpdated(coords: BlockRelWorld, world: BlockSetAndGet): Unit
}