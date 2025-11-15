import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class ProgramPointer extends Module {
  val io = IO(new Bundle {
    val store_nzp = Input(Bool());
    val nzp = Input(UInt(3.W));
    
    val update = Input(Bool());
    val branch = Input(Bool());
    val jump_location = Input(UInt(16.W));
    val target_nzp = Input(UInt(3.W));
  
    val pointer = Output(UInt(16.W)); 
  })

  val nzp = RegInit(0.U(3.W));

  val pointer = RegInit(0.U(16.W));
  io.pointer := pointer;

  when(io.store_nzp) {
    nzp := io.nzp;
  }

  when(io.update) {
    when(io.branch && (nzp & io.target_nzp) =/= 0.U) {
      pointer := io.jump_location;
    }.otherwise {
      pointer := pointer + 1.U;
    }
  }
}
