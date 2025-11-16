import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Memory extends Module {
  val io = IO(new SRAMInterface(1024, UInt(8.W), 5, 3, 0));

  val memory = SRAM(1024, UInt(8.W), 5, 3, 0);

  io.readPorts(0) <> memory.readPorts(0);
  io.readPorts(1) <> memory.readPorts(1);
  io.readPorts(2) <> memory.readPorts(2);
  io.readPorts(3) <> memory.readPorts(3);
  io.readPorts(4) <> memory.readPorts(4);

  io.writePorts(0) <> memory.writePorts(0);
  io.writePorts(1) <> memory.writePorts(1);
  io.writePorts(2) <> memory.writePorts(2);
}
