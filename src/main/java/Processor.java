import component.Alu;
import component.ForwardingUnit;
import component.Memory;
import component.Register;
import component.pipeline.*;
import controller.MainController;
import org.jetbrains.annotations.NotNull;
import signal.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Processor {

    private final List<Stage> stages = new ArrayList<>();
    private final List<PipelineRegister> pipelineRegisters = new ArrayList<>();
    private final List<ProcessorLogger> loggers = new ArrayList<>();
    private final InstructionFetchToInstructionDecodeRegister ifId;
    private final InstructionDecodeToExecutionRegister idExe;
    private final ExecutionToMemoryAccessRegister exeMem;
    private final MemoryAccessToWriteBackRegister memWb;
    private final InstructionDecode instructionDecode;
    private final MemoryAccess memoryAccess;

    private Processor(
            @NotNull final InstructionFetch instructionFetch,
            @NotNull final InstructionFetchToInstructionDecodeRegister ifId,
            @NotNull final InstructionDecode instructionDecode,
            @NotNull final InstructionDecodeToExecutionRegister idExe,
            @NotNull final Execution execution,
            @NotNull final ExecutionToMemoryAccessRegister exeMem,
            @NotNull final MemoryAccess memoryAccess,
            @NotNull final MemoryAccessToWriteBackRegister memWb,
            @NotNull final WriteBack writeBack
    ) {
        this.ifId = ifId;
        this.idExe = idExe;
        this.exeMem = exeMem;
        this.memWb = memWb;
        this.instructionDecode = instructionDecode;
        this.memoryAccess = memoryAccess;
        stages.add(instructionFetch);
        stages.add(instructionDecode);
        stages.add(execution);
        stages.add(memoryAccess);
        stages.add(writeBack);
        pipelineRegisters.add(ifId);
        pipelineRegisters.add(idExe);
        pipelineRegisters.add(exeMem);
        pipelineRegisters.add(memWb);
    }

    public void run() {
        do {
            stages.forEach(Stage::run);
            pipelineRegisters.forEach(PipelineRegister::update);
            loggers.forEach(printer -> printer.onClockCycleFinished(this, ifId, idExe, exeMem, memWb));
        } while (hasUnfinishedInstructions());
    }

    private boolean hasUnfinishedInstructions() {
        for (final Stage stage : stages)
            if (stage.hasInstruction()) return true;
        return false;
    }

    public void addLogger(@NotNull final ProcessorLogger logger) {
        loggers.add(logger);
    }

    @NotNull
    public Set<Integer> getWrittenRegisterAddresses() {
        return instructionDecode.getWrittenRegisterAddresses();
    }

    public int readRegister(final int address) {
        return instructionDecode.readRegister(address);
    }

    @NotNull
    public Set<Integer> getWrittenDataMemoryAddresses() {
        return memoryAccess.getWrittenDataMemoryAddresses();
    }

    public int readDataMemory(final int address) {
        return memoryAccess.readDataMemory(address);
    }

    public static class Builder {

        @NotNull
        private final Memory instructionMemory = new Memory();
        @NotNull
        private Register register = new Register();
        @NotNull
        private Memory dataMemory = new Memory();

        @NotNull
        Builder setInstructions(@NotNull final List<Instruction> instructions) {
            instructionMemory.setMemoryWrite(MainController.MemoryWrite.TRUE);
            int address = 0x00;
            for (final Instruction instruction : instructions) {
                instructionMemory.setAddress(address);
                instructionMemory.write(instruction);
                address += 4;
            }
            instructionMemory.setMemoryWrite(MainController.MemoryWrite.FALSE);
            return this;
        }

        @NotNull
        Builder setRegister(@NotNull final Register register) {
            this.register = register;
            return this;
        }

        @NotNull
        Builder setDataMemory(@NotNull final Memory dataMemory) {
            this.dataMemory = dataMemory;
            return this;
        }

        @NotNull
        Processor build() {
            final InstructionFetch instructionFetch = new InstructionFetch(instructionMemory);
            final InstructionFetchToInstructionDecodeRegister ifId = new InstructionFetchToInstructionDecodeRegister(instructionFetch);
            final InstructionDecode instructionDecode = new InstructionDecode(ifId, new MainController(), register);
            final InstructionDecodeToExecutionRegister idExe = new InstructionDecodeToExecutionRegister(instructionDecode);
            final Execution execution = new Execution(idExe, new Alu(), new Alu());
            final ExecutionToMemoryAccessRegister exeMem = new ExecutionToMemoryAccessRegister(execution);
            final MemoryAccess memoryAccess = new MemoryAccess(exeMem, dataMemory);
            final MemoryAccessToWriteBackRegister memWb = new MemoryAccessToWriteBackRegister(memoryAccess);
            final WriteBack writeBack = new WriteBack(memWb, register);

            execution.setForwardingUnit(new ForwardingUnit(idExe, exeMem, memWb));
            execution.setExecutionToMemoryAccessRegister(exeMem);
            execution.setMemoryAccessToWriteBackRegister(memWb);

            return new Processor(
                    instructionFetch,
                    ifId,
                    instructionDecode,
                    idExe,
                    execution,
                    exeMem,
                    memoryAccess,
                    memWb,
                    writeBack
            );
        }
    }
}
