import org.scalameter.api._

object RangeMicrobenchmark extends Bench.OfflineReport {
  val sizes: Gen[Int] = Gen.range("size")(300000, 1500000, 300000)

  val ranges: Gen[Range] = for {
    size <- sizes
  } yield 0 until size

  performance of "Range" in {
    measure method "map" in {
      using(ranges) config (
        exec.benchRuns -> 15
      ) in {
        r => r.map(_ + 1)
      }
    }
  }
}

object RegressionTest extends Bench.OfflineReport {
  val sizes = Gen.range("size")(1000000, 5000000, 2000000)
  val arrays = for (sz <- sizes) yield (0 until sz).toArray

  performance of "Array" in {
    measure method "foreach" in {
      using(arrays) config (
        exec.independentSamples -> 6
      ) in { xs =>
        var sum = 0
        xs.foreach(x => sum += x)
      }
    }
  }
}

object MemoryTest extends Bench.OfflineReport {
  override def persistor = new SerializationPersistor
  override def measurer = new Executor.Measurer.MemoryFootprint

  val sizes = Gen.range("size")(1000000, 5000000, 2000000)

  performance of "MemoryFootprint" in {
    performance of "Array" in {
      using(sizes) config (
        exec.benchRuns -> 10,
        exec.independentSamples -> 2
      ) in { sz =>
        (0 until sz).toArray
      }
    }
  }
}
