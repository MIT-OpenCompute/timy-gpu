# Timy GPU
The goal of this Timy GPU project is to develop a miniature gpu capable of parallel processing. Specifically the ability to execute programs similar to "compute shaders". The project will be written in the chisel HDL with the goal of testing the design on a physical FPGA.
## Design
The GPU will consist of memory, a single core with many thread, and logic to load programs into memory and dispatch programs to execute.
### Memory
A single memory block will be shared between cores. A memory controller will manage read and write requests from the multiple cores at once. Memory will have 16 bits of addressable space. Memory will include both program data and application data. There will also be a stack with a size of 16 bits.
### GPU State
The GPU can either be idle, program load, or execute states. During program load state, the gpu will read in data and address location from external wires and load the data into memory at the addresses.
```cs
in byte state // idle | program load | execute

out bool writeReady
in byte writeData
in byte writeAddress
in bool write

out bool readReady
out byte readData
in byte readAddress
in bool read

in byte startPointer
```
### Core
Theoretically the GPU could be expanded to allow the execution of multiple programs at once by adding more cores, however for simplicity's sake I'm aiming to only have a single core for the moment. Although once we get a single core working I don't imagine supporting more cores to be very difficult. A core will consist of a memory access, dispatcher / synchronizer, and a number of threads. The dispatcher / synchronizer manages loading the program from memory and wiring this to threads for threads to execute. (This is already somewhat working as of 10-16).
### Thread
A single thread contains a program counter, ALU, LSU, and registers. Thread take in loaded operations from their parent core and execute the operation. Once more development is done with register's, I'll have a clearer idea of how many / which registers a thread needs, but I'm imagining initially we'll have a few 16 bit registers:
1. stack register
2. a, b, and c register
### Instruction Set
The instruction set is 24 bits wide. 8 for opcode and 16 additional bits for immediate. The first 5 opcode bits specify instruction. The next 3 specify target or source / destination registers if an instruction uses it.
#### Opcodes
- move
	`00001` + target --> moves immediate into register
	`00010` + src/dst --> moves value in register to register
- load
	`00011` + src/dst --> takes address from register and loads memory into other register. The src and dst registers must not be the same register.
- add
	`00100` + src/dst --> add value in src to dst and store in dst
- mul
	`00101` + src/dst --> multiplies value in src by dst and store in dst
- cmp
	`00110` + src/dst --> compares value in src to dst and stores result in nzp flag of alu
- jmp
	`00111` + target --> jumps program pointer to value specified in register
	`01000` + target --> jumps program pointer to value specified in register if negative flag is set
	`01001` + target --> jumps program pointer to value specified in register if positive flag is set
	`01010` + target --> jumps program pointer to value specified in register if zero flag is set
	`01011` + target --> jumps program pointer to value specified in register if not zero flag is set
- or
	`01100` + src/dst --> does bitwise or of src and dst registers and stores result in dst
- and
	`01101` + src/dst --> does bitwise and of src and dst registers and stores result in dst
- xor
	`01110` + src/dst --> does bitwise xor of src and dst registers and stores result in dst
- not
	`01111` + src/dst --> does bitwise not of src and dst registers and stores result in dst
- shift R
	`10000` + target --> shifts bits to the right and pads 0s at beginning of target register
- shift L
	`10001` + target --> shifts bits to the left and pads 0s at end of target register
- push
	`10010` + target --> pushes value in target register to stack
- pop
	`10011` + target --> pops value on top of stack into register
- sync (experimental?)
	`10100` + target --> tells the core dispatcher to not dispatch any threads until all threads have reached the program pointer specified in the specified register
- term
	`10101` --> signals that the thread has finished execution
- store
	`10110` + src/dst --> takes address from src register and stores the value in dst register into memory
#### Register Codes
`000` --> A --> B
`001` --> A --> C
`010` --> B --> A
`011` --> B --> C
`100` --> C --> A
`101` --> C --> B

Potentially we may need more instructions but I can't think of any more that we need right now?
# How To Run
`sbt run test`
