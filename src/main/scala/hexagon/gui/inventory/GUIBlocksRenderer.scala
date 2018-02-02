package hexagon.gui.inventory

import hexagon.block.Block
import hexagon.gui.comp.{Component, GUITransformation}
import hexagon.resource.{Shader, TextureArray}
import hexagon.world.render.{BlockRendererCollection, FlatBlockRenderer}
import org.joml.Matrix4f

class GUIBlocksRenderer(w: Int, h: Int = 1, separation: Float = 0.2f, xOff: Float = 0, yOff: Float = 0)(blockProvider: Int => Block) {
  private val guiBlockRenderer = new BlockRendererCollection(s => new FlatBlockRenderer(s, 0))
  private val guiBlockShader: Shader = Shader.get("gui_block").get
  private val guiBlockSideShader: Shader = Shader.get("gui_blockSide").get
  private val blockTexture: TextureArray = TextureArray.getTextureArray("blocks")

  private var viewMatrix = new Matrix4f
  def setViewMatrix(matr: Matrix4f): Unit = viewMatrix = matr

  updateContent()

  def render(transformation: GUITransformation): Unit = {
    guiBlockShader.setUniformMat4("viewMatrix", viewMatrix)
    guiBlockSideShader.setUniformMat4("viewMatrix", viewMatrix)
    blockTexture.bind()

    for (side <- 0 until 8) {
      val sh = if (side < 2) guiBlockShader else guiBlockSideShader
      sh.enable()
      sh.setUniform1i("side", side)
      guiBlockRenderer.renderBlockSide(side)
    }
  }

  def updateContent(): Unit = {
    for (side <- 0 until 8) guiBlockRenderer.updateContent(side, 9*9, buf => {
      for (y <- 0 until h) {
        for (x <- 0 until w) {
          val blockToDraw = blockProvider(x)
          buf.putFloat(x * separation + xOff).putFloat(y * separation + yOff).putInt(blockToDraw.blockTex(side)).putFloat(1.0f) //blockInHand.blockHeight(new BlockState(BlockRelWorld(0, 0, 0, world), blockInHand)))
        }
      }
    })
  }
}