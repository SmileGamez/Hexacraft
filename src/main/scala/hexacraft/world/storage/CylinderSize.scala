package hexacraft.world.storage

import hexacraft.world.coord.CoordUtils

/**
  * The real cylinder size (the number of chunks around the cylinder) is:<br>
  * <code>ringSize = 2&#94;sizeExponent</code>
  *
  * @param worldSize the size exponent, <b>max-value: 20</b>
  */
class CylinderSize(val worldSize: Int) {
  /** The number of chunks around the cylinder */
  def ringSize: Int = 1 << worldSize

  /** ringSize - 1 */
  def ringSizeMask: Int = ringSize - 1

  /** The number of blocks around the cylinder */
  def totalSize: Int = 16 * ringSize

  /** totalSize - 1 */
  def totalSizeMask: Int = totalSize - 1

  /** The angle (in radians) of half a block seen from the center of the cylinder */
  def hexAngle: Double = (2 * math.Pi) / totalSize

  /** The radius of the cylinder */
  def radius: Double = CoordUtils.y60 / hexAngle

  /** The circumference of the cylinder.<br><br>This is NOT the number of blocks, for that see <code>totalSize</code>. */
  def circumference: Double = totalSize * CoordUtils.y60
}