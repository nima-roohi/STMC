
import scala.io.Source
import scala.util.matching.Regex

object Tmp extends App {


  val filename = "../STMC-webpage-dev/public/benchmarks/ctmc-ltl-cluster.json"
//  val filename = "./examples/cluster/cluster.log"
  val source = Source.fromFile(filename)

//  val pattern = """.*\[([.0-9]+),\s*([.0-9]+)\].*""".r
  val pattern = """(.*)standard-error=([.0-9]+).*""".r

  for (line <- source.getLines) {
    line match {
    case pattern(_, err) => println(line.replace(err, (err.toDouble * Math.sqrt(20)).toString))
    case _               => println(line)
    }
  }

  source.close()

}
