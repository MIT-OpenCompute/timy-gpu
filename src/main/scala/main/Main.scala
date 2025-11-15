import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.RawTester.test
import _root_.circt.stage.ChiselStage

object Main extends App {
    emitVerilog(new Alu(), Array("--target-dir", "generated"));
    emitVerilog(new Lsu(), Array("--target-dir", "generated"));
    emitVerilog(new ProgramPointer(), Array("--target-dir", "generated"));

    test(new Lsu) { dut =>
        dut.io.write.poke(true.B);
        dut.io.address.poke(1.U);
        dut.io.data.poke(2.U);

        dut.clock.step(1);
    }
}