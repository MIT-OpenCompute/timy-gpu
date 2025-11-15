import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Thread extends Module {
  val io = IO(new Bundle {
    val dispatcher_opcode_loaded = Input(Bool());
    val dispatcher_program_pointer = Input(UInt(8.W));

    val operation = Input(Operation());
    val src_register = Input(Register());
    val dst_register = Input(Register());
    val immediate = Input(UInt(16.W));

    val program_pointer = Output(UInt(8.W));
    val end_of_program = Output(Bool());
    val idle = Output(Bool());
    val debug_output = Output(UInt(8.W));
  })

  val end_of_program = RegInit(false.B);

  val register_a = RegInit(0.U(16.W));
  val register_b = RegInit(0.U(16.W));
  val register_c = RegInit(0.U(16.W));

  val alu = Module(new Alu())
  alu.io.execute := false.B;
  alu.io.operation := Operation.NoOp;
  alu.io.compare := false.B;
  alu.io.rs := 0.U(8.W);
  alu.io.rt := 0.U(8.W);

  io.debug_output := alu.io.output;

  val lsu = Module(new Lsu())
  lsu.io.read := false.B;
  lsu.io.write := false.B
  lsu.io.address := 0.U;
  lsu.io.data := 0.U;

  val executing_load_write = RegInit(false.B);
  val load_write_operation = RegInit(Operation.NoOp);
  val load_write_address = RegInit(0.U(8.W));
  val write_value = RegInit(0.U(8.W));

  val program_counter = Module(new ProgramCounter())
  program_counter.io.store_nzp := false.B;
  program_counter.io.nzp := 0.U(3.W);
  program_counter.io.update := false.B;
  program_counter.io.branch := false.B;
  program_counter.io.jump_location := 0.U(8.W);
  program_counter.io.target_nzp := 0.U(3.W);

  io.program_pointer := program_counter.io.program_counter;

  io.end_of_program := end_of_program;
  io.idle := false.B;

  when(
    io.dispatcher_opcode_loaded && io.dispatcher_program_pointer === program_counter.io.program_counter
  ) {
    when(
      io.operation === Operation.Add || io.operation === Operation.Mul || io.operation === Operation.Compare
    ) {
      alu.io.execute := true.B;
      alu.io.operation := io.operation;

      when(io.src_register === Register.A) {
        alu.io.rs := register_a;
      }

      when(io.src_register === Register.B) {
        alu.io.rs := register_b;
      }

      when(io.src_register === Register.C) {
        alu.io.rs := register_c;
      }

      when(io.dst_register === Register.A) {
        alu.io.rt := register_a;
        register_a := alu.io.output
      }

      when(io.dst_register === Register.B) {
        alu.io.rt := register_b;
        register_b := alu.io.output
      }

      when(io.dst_register === Register.C) {
        alu.io.rt := register_c;
        register_c := alu.io.output
      }

      program_counter.io.update := true.B;
      program_counter.io.branch := false.B;

      io.idle := false.B;
    }

    when(io.operation === Operation.MoveImmediate) {
      when(io.dst_register === Register.A) {
        register_a := io.immediate
      }

      when(io.dst_register === Register.B) {
        register_b := io.immediate
      }

      when(io.dst_register === Register.C) {
        register_c := io.immediate
      }

      program_counter.io.update := true.B;
      program_counter.io.branch := false.B;

      io.idle := false.B;
    }

    when(io.operation === Operation.MoveRegister) {
      when(io.src_register === Register.A && io.dst_register === Register.B) {
        register_a := register_b
      }

      when(io.src_register === Register.A && io.dst_register === Register.C) {
        register_a := register_c
      }

      when(io.src_register === Register.B && io.dst_register === Register.A) {
        register_b := register_a
      }

      when(io.src_register === Register.B && io.dst_register === Register.C) {
        register_b := register_c
      }

      when(io.src_register === Register.C && io.dst_register === Register.A) {
        register_c := register_a
      }

      when(io.src_register === Register.C && io.dst_register === Register.B) {
        register_c := register_b
      }

      program_counter.io.update := true.B;
      program_counter.io.branch := false.B;

      io.idle := false.B;
    }

    when(
      io.operation === Operation.Write || io.operation === Operation.Load && !executing_load_write
    ) {
      io.idle := false.B;
      executing_load_write := true.B;

      val operation = WireInit(Operation.NoOp);
      val address = WireInit(0.U(8.W));
      val value = WireInit(0.U(8.W));

      when(executing_load_write) {
        operation := load_write_operation;
        address := load_write_address;
        value := write_value;
      }.otherwise {
        load_write_operation := io.operation;
        operation := io.operation;

        load_write_address := io.immediate;
        address := io.immediate;

        switch(io.src_register) {
          is(Register.A) {
            value := register_a;
            write_value := register_a;
          }
          is(Register.B) {
            value := register_b;
            write_value := register_b;
          }
          is(Register.C) {
            value := register_c;
            write_value := register_c;
          }
        }
      }

      lsu.io.write := operation === Operation.Write;
      lsu.io.read := operation === Operation.Load;

      lsu.io

      // when(io.operation)

      when(executing_load_write) {}
    }
  }

  when(true.B) {
    printf(p"\t[Thread]=====");
    printf(p"\n\t\tio.operation=${io.operation}");
    printf(p"\n\t\tprogram_pointer=${program_counter.io.program_counter}");
    printf(p"\n\t\tidle=${idle}");
    printf(p"\n\t\tio.idle=${io.idle}");
    printf(p"\n\t\tio.debug_output=${io.debug_output}");
    printf(p"\n\t\ta=${register_a}");
    printf(p"\n\t\tb=${register_b}");
    printf(p"\n\t\tc=${register_c}");
    printf(p"\n\t\tSrc Register=${io.src_register}");
    printf(p"\n\t\tDst Register=${io.dst_register}");
    printf(p"\n\n");
  }
}
