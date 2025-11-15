import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Thread extends Module {
  val io = IO(new Bundle {
    val operation_pointer = Input(UInt(16.W));
    val operation = Input(Operation());
    val src_register = Input(Register());
    val dst_register = Input(Register());
    val immediate = Input(UInt(16.W));
    val operation_loaded = Input(Bool());

    val program_pointer = Output(UInt(8.W));
    val end_of_program = Output(Bool());
    val idle = Output(Bool());
    val debug_output = Output(UInt(8.W));
  })

  val program_pointer = Module(new ProgramPointer())
  program_pointer.io.store_nzp := false.B;
  program_pointer.io.nzp := 0.U(3.W);
  program_pointer.io.update := false.B;
  program_pointer.io.branch := false.B;
  program_pointer.io.jump_location := 0.U(8.W);
  program_pointer.io.target_nzp := 0.U(3.W);

  io.program_pointer := program_pointer.io.pointer;

  val operation_register = RegInit(Operation.NoOp);
  val operation = WireInit(Operation.NoOp);
  val operation_pointer_register = RegInit(0.U(16.W));
  val operation_pointer = WireInit(0.U(16.W));
  val src_register_register = RegInit(Register.A);
  val src_register = WireInit(Register.A);
  val dst_register_register = RegInit(Register.B);
  val dst_register = WireInit(Register.B);
  val immediate_register = RegInit(0.U(16.W));
  val immediate = WireInit(0.U(16.W));
  val operation_loaded_register = RegInit(false.B);
  val operation_loaded = WireInit(false.B);

  when(program_pointer.io.pointer === io.operation_pointer && io.operation_loaded) {
    operation_register := io.operation;
    operation_pointer_register := io.operation_pointer;
    src_register_register := io.src_register;
    dst_register_register := io.dst_register;
    immediate_register := io.immediate;
    operation_loaded_register := true.B;

    operation := io.operation;
    operation_pointer := io.operation_pointer;
    src_register := io.src_register;
    dst_register := io.dst_register;
    immediate := io.immediate;
    operation_loaded := true.B; 
  }.otherwise {
    operation := operation_register;
    operation_pointer := operation_pointer_register;
    src_register := src_register_register;
    dst_register := dst_register_register;
    immediate := immediate_register;
    operation_loaded := operation_loaded_register; 
  }
  
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

  // val lsu = Module(new Lsu())
  // lsu.io.read := false.B;
  // lsu.io.write := false.B
  // lsu.io.address := 0.U;
  // lsu.io.data := 0.U;

  val executing_load_write = RegInit(false.B);
  val load_write_operation = RegInit(Operation.NoOp);
  val load_write_address = RegInit(0.U(8.W));
  val write_value = RegInit(0.U(8.W));

  io.end_of_program := end_of_program;
  io.idle := true.B;

  when(
    operation_loaded && operation_pointer === program_pointer.io.pointer
  ) {
    when(
      operation === Operation.Add || operation === Operation.Mul || operation === Operation.Compare
    ) {
      alu.io.execute := true.B;
      alu.io.operation := operation;

      when(src_register === Register.A) {
        alu.io.rs := register_a;
      }

      when(src_register === Register.B) {
        alu.io.rs := register_b;
      }

      when(src_register === Register.C) {
        alu.io.rs := register_c;
      }

      when(dst_register === Register.A) {
        alu.io.rt := register_a;
        register_a := alu.io.output
      }

      when(dst_register === Register.B) {
        alu.io.rt := register_b;
        register_b := alu.io.output
      }

      when(dst_register === Register.C) {
        alu.io.rt := register_c;
        register_c := alu.io.output
      }

      program_pointer.io.update := true.B;
      program_pointer.io.branch := false.B;

      io.idle := false.B;
    }

    when(operation === Operation.MoveImmediate) {
      when(dst_register === Register.A) {
        register_a := immediate
      }

      when(dst_register === Register.B) {
        register_b := immediate
      }

      when(dst_register === Register.C) {
        register_c := immediate
      }

      program_pointer.io.update := true.B;
      program_pointer.io.branch := false.B;

      io.idle := false.B;
    }

    when(operation === Operation.MoveRegister) {
      when(src_register === Register.A && dst_register === Register.B) {
        register_a := register_b
      }

      when(src_register === Register.A && dst_register === Register.C) {
        register_a := register_c
      }

      when(src_register === Register.B && dst_register === Register.A) {
        register_b := register_a
      }

      when(src_register === Register.B && dst_register === Register.C) {
        register_b := register_c
      }

      when(src_register === Register.C && dst_register === Register.A) {
        register_c := register_a
      }

      when(src_register === Register.C && dst_register === Register.B) {
        register_c := register_b
      }

      program_pointer.io.update := true.B;
      program_pointer.io.branch := false.B;

      io.idle := false.B;
    }

    // when(
    //   io.operation === Operation.Write || io.operation === Operation.Load && !executing_load_write
    // ) {
    //   io.idle := false.B;
    //   executing_load_write := true.B;

    //   val operation = WireInit(Operation.NoOp);
    //   val address = WireInit(0.U(8.W));
    //   val value = WireInit(0.U(8.W));

    //   when(executing_load_write) {
    //     operation := load_write_operation;
    //     address := load_write_address;
    //     value := write_value;
    //   }.otherwise {
    //     load_write_operation := io.operation;
    //     operation := io.operation;

    //     load_write_address := io.immediate;
    //     address := io.immediate;

    //     switch(io.src_register) {
    //       is(Register.A) {
    //         value := register_a;
    //         write_value := register_a;
    //       }
    //       is(Register.B) {
    //         value := register_b;
    //         write_value := register_b;
    //       }
    //       is(Register.C) {
    //         value := register_c;
    //         write_value := register_c;
    //       }
    //     }
    //   }

    //   lsu.io.write := operation === Operation.Write;
    //   lsu.io.read := operation === Operation.Load;

    //   // when(io.operation)

    //   when(executing_load_write) {}
    // }
  }

  when(true.B) {
    printf(p"\t[Thread]=====");
    printf(p"\n\t\tio.operation=${io.operation}");
    printf(p"\n\t\tio.operation_pointer=${io.operation_pointer}");
    printf(p"\n\t\tio.operation_loaded=${io.operation_loaded}");
    printf(p"\n\t\tprogram_pointer=${program_pointer.io.pointer}");
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
