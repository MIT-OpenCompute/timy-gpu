import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Core extends Module {
  val io = IO(new Bundle {
    val execute = Input(Bool());

    val debug_memory_write = Input(Bool());
    val debug_memory_write_address = Input(UInt(8.W));
    val debug_memory_write_data_0 = Input(UInt(8.W));
    val debug_memory_write_data_1 = Input(UInt(8.W));
    val debug_memory_write_data_2 = Input(UInt(8.W));

    val debug_dispatcher_opcode = Output(Operation());
    val debug_dispatcher_program_pointer = Output(UInt(8.W));

    val debug_thread_debug_output = Output(UInt(8.W));
  });

  val memory = Module(new Memory());
  val dispatcher = Module(new Dispatcher());
  val thread = Module(new Thread());

  memory.io.readPorts(0).enable := dispatcher.io.read_requested;
  memory.io.readPorts(0).address := 3.U * dispatcher.io.read_program_pointer;
  memory.io.readPorts(1).enable := dispatcher.io.read_requested;
  memory.io
    .readPorts(1)
    .address := 3.U * dispatcher.io.read_program_pointer + 1.U;
  memory.io.readPorts(2).enable := dispatcher.io.read_requested;
  memory.io
    .readPorts(2)
    .address := 3.U * dispatcher.io.read_program_pointer + 2.U;

  memory.io.writePorts(0).enable := io.debug_memory_write;
  memory.io.writePorts(0).address := io.debug_memory_write_address * 3.U;
  memory.io.writePorts(0).data := io.debug_memory_write_data_0;
  memory.io.writePorts(1).enable := io.debug_memory_write;
  memory.io.writePorts(1).address := io.debug_memory_write_address * 3.U + 1.U;
  memory.io.writePorts(1).data := io.debug_memory_write_data_1;
  memory.io.writePorts(2).enable := io.debug_memory_write;
  memory.io.writePorts(2).address := io.debug_memory_write_address * 3.U + 2.U;
  memory.io.writePorts(2).data := io.debug_memory_write_data_2;

  dispatcher.io.thread_requesting_opcode := thread.io.idle && io.execute;
  dispatcher.io.thread_program_pointer := thread.io.program_pointer;

  val read_ready_delayed = RegNext(dispatcher.io.read_requested, false.B);
  dispatcher.io.read_ready := read_ready_delayed;
  dispatcher.io.read_opcode := memory.io.readPorts(0).data(7, 0);
  dispatcher.io.read_immediate_l := memory.io.readPorts(1).data(7, 0);
  dispatcher.io.read_immediate_u := memory.io.readPorts(2).data(7, 0);

  when(true.B) {
    printf(p"\t[Core]=====");
    printf(
      p"\n\t\tdispatcher.io.read_requested=${dispatcher.io.read_requested}"
    );
    printf(
      p"\n\t\tdispatcher.io.read_program_pointer=${dispatcher.io.read_program_pointer}"
    );
    printf(p"\n\t\tdispatcher.io.read_opcode=${dispatcher.io.read_opcode}");
    // printf(p"\n\t\tread_ready_delayed=${read_ready_delayed}");
    printf(p"\n\t\tdispatcher_read_ready=${dispatcher.io.read_ready}");
    printf(p"\n\t\tdebug_dispatcher_opcode=${io.debug_dispatcher_opcode}");
    printf(
      p"\n\t\tdebug_dispatcher_program_pointer=${io.debug_dispatcher_program_pointer}"
    );
    printf(p"\n\t\texecute=${io.execute}");
    printf(p"\n\n");
  }

  io.debug_dispatcher_opcode := dispatcher.io.opcode;
  io.debug_dispatcher_program_pointer := dispatcher.io.program_pointer;

  thread.io.operation := dispatcher.io.opcode;
  thread.io.operation_pointer := dispatcher.io.program_pointer;
  thread.io.src_register := dispatcher.io.src_register;
  thread.io.dst_register := dispatcher.io.dst_register;
  thread.io.immediate := Cat(
    dispatcher.io.read_immediate_u,
    dispatcher.io.read_immediate_l
  );
  thread.io.operation_loaded := dispatcher.io.opcode_loaded;

  io.debug_thread_debug_output := thread.io.debug_output;
}
