import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ProgramPointerTest extends AnyFlatSpec with ChiselScalatestTester {
  "Program Pointer Increment" should "work" in {
    test(new ProgramPointer) { dut =>
      dut.io.update.poke(true.B);

      dut.clock.step(1);

      dut.io.pointer.expect(1.U);
    }
  }

  "Program Pointer Jump" should "work" in {
    test(new ProgramPointer) { dut =>
      dut.io.store_nzp.poke(true.B);
      dut.io.nzp.poke(1.U);

      dut.clock.step(1);

      dut.io.update.poke(true.B);
      dut.io.branch.poke(true.B);
      dut.io.jump_location.poke(8.U);
      dut.io.target_nzp.poke(1.U);

      dut.clock.step(1);

      dut.io.pointer.expect(8.U);
    }
  }

  "Program Pointer Jump Fail" should "work" in {
    test(new ProgramPointer) { dut =>
      dut.io.store_nzp.poke(true.B);
      dut.io.nzp.poke(1.U);

      dut.clock.step(1);

      dut.io.update.poke(true.B);
      dut.io.branch.poke(true.B);
      dut.io.jump_location.poke(8.U);
      dut.io.target_nzp.poke(2.U);

      dut.clock.step(1);

      dut.io.pointer.expect(1.U);
    }
  }
}
