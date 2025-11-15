import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LsuTest extends AnyFlatSpec with ChiselScalatestTester {
  "Lsu Read" should "work" in {
    test(new Lsu) { dut =>
      dut.io.read.poke(true.B);
      dut.io.address.poke(1.U);

      dut.clock.step(1);

      dut.io.state.expect(LsuState.Requesting);
      dut.io.read_requested.expect(true.B);
      dut.io.read_address.expect(1.U);

      dut.clock.step(1);
      dut.clock.step(1);

      dut.io.state.expect(LsuState.Requesting);
      dut.io.read_ready.poke(true.B);
      dut.io.read_data.poke(5.U);

      dut.clock.step(1);

      dut.io.state.expect(LsuState.Idle);
      dut.io.output.expect(5.U);
    }
  }

  "Lsu Write" should "work" in {
    test(new Lsu) { dut =>
      dut.io.write.poke(true.B);
      dut.io.address.poke(1.U);
      dut.io.data.poke(2.U);

      dut.clock.step(1);

      dut.io.state.expect(LsuState.Requesting);
      dut.io.write_requested.expect(true.B);
      dut.io.write_address.expect(1.U);
      dut.io.write_data.expect(2.U);

      dut.clock.step(1);
      dut.clock.step(1);

      dut.io.state.expect(LsuState.Requesting);

      dut.io.write_ready.poke(true.B);

      dut.clock.step(1);

      dut.io.state.expect(LsuState.Idle);
    }
  }
}
