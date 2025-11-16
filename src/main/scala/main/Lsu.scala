import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

object LsuState extends ChiselEnum {
  val Idle, Requesting, Done = Value
}

class Lsu extends Module {
  val io = IO(new Bundle {
    val read = Input(Bool());
    val write = Input(Bool());

    val address = Input(UInt(16.W));
    val data = Input(UInt(16.W));

    val read_requested = Output(Bool());
    val read_address = Output(UInt(16.W));
    val read_ready = Input(Bool());
    val read_data = Input(UInt(16.W));

    val write_requested = Output(Bool());
    val write_data = Output(UInt(16.W));
    val write_address = Output(UInt(16.W));
    val write_ready = Input(Bool());

    val state = Output(LsuState());
    val output = Output(UInt(16.W));
  })

  val state = RegInit(LsuState.Idle);
  io.state := state;

  val output = RegInit(0.U(16.W));
  io.output := output;

  val read_requested = RegInit(false.B);
  io.read_requested := read_requested;

  val read_address = RegInit(false.B);
  io.read_address := read_address;

  val write_requested = RegInit(false.B);
  io.write_requested := write_requested;

  val write_address = RegInit(0.U(16.W));
  io.write_address := write_address;

  val write_data = RegInit(0.U(16.W));
  io.write_data := write_data;

  when(io.read) {
    switch(io.state) {
      is(LsuState.Idle) {
        state := LsuState.Requesting;
        read_requested := true.B;
        read_address := io.address;
      }

      is(LsuState.Requesting) {
        when(io.read_ready) {
          state := LsuState.Done;
          read_requested := false.B;
          output := io.read_data;
        }
      }

      is(LsuState.Done) {
        state := LsuState.Idle;
      }
    }
  }

  when(io.write) {
    switch(io.state) {
      is(LsuState.Idle) {
        state := LsuState.Requesting;
        write_requested := true.B;
        write_address := io.address;
        write_data := io.data
      }

      is(LsuState.Requesting) {
        when(io.write_ready) {
          state := LsuState.Done;
          write_requested := false.B;
        }
      }

      is(LsuState.Done) {
        state := LsuState.Idle;
      }
    }
  }
}
