import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  "Core" should "work" in {
    test(new Core) { dut =>
      dut.io.debug_memory_write.poke(true.B);
      dut.io.debug_memory_write_address.poke(0.U(8.W));
      dut.io.debug_memory_write_data.poke(0b00001010.U(8.W));
      
      println("[CoreTest]=====");
      dut.clock.step(1);

      dut.io.debug_memory_write.poke(true.B);
      dut.io.debug_memory_write_address.poke(1.U(8.W));
      dut.io.debug_memory_write_data.poke(0b01000000.U(8.W));
    
      println("[CoreTest]=====");
      dut.clock.step(1);

      dut.io.debug_memory_write.poke(true.B);
      dut.io.debug_memory_write_address.poke(2.U(8.W));
      dut.io.debug_memory_write_data.poke(0b00000000.U(8.W));

      println("[CoreTest]=====");
      dut.clock.step(1);

      dut.io.execute.poke(true.B);

      println("[CoreTest]=====");
      dut.clock.step(1);

      println("[CoreTest]=====");
      dut.clock.step(1);

      println("[CoreTest]=====");
      dut.clock.step(1);

      println("[CoreTest]=====");
      dut.clock.step(1);

      println("[CoreTest]=====");
      dut.clock.step(1);

      println("[CoreTest]=====");
      dut.clock.step(1);

      println("[CoreTest]=====");
      dut.clock.step(1);
    }
  }
}
