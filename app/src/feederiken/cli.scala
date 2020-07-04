// This is free and unencumbered software released into the public domain.
//
// Anyone is free to copy, modify, publish, use, compile, sell, or
// distribute this software, either in source code form or as a compiled
// binary, for any purpose, commercial or non-commercial, and by any
// means.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
// IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

package feederiken

import com.monovore.decline._
import cats.syntax.all._
import cats.instances.list._
import cats.data.Chain

sealed abstract class Command
case class Search(j: Option[Int], prefix: Chain[Byte]) extends Command
case class Bench(j: Option[Int], n: Int) extends Command


object CLI {
  import Opts._

  private val j = option[Int]("j", "# of concurrent threads to use").validate("j must be positive")(_ > 0).orNone
  private val n = option[Int]("n", "how many keys to generate for benchmarking").withDefault(10000).validate("n must be positive")(_ > 0)
  private val prefix = option[String]("prefix", "key prefix to look for (hex)").withDefault("feed").mapValidated {
    _.filterNot(_.isWhitespace).grouped(2).toList.foldMap { s =>
      try Chain(Integer.parseUnsignedInt(s, 16).toByte).valid
      catch {
        case _: NumberFormatException => s"Invalid byte: $s".invalidNel
      }
    }
  }

  private def bench = Command[Bench]("bench", "benchmark CPU hashrate") {
    (j, n).mapN(Bench)
  }

  private def search = Command[Search]("search", "search for a vanity key") {
    (j, prefix).mapN(Search)
  }

  def top = Command[Command]("feederiken", "vanity PGP key generator") {
    subcommands(search, bench)
  }
}
