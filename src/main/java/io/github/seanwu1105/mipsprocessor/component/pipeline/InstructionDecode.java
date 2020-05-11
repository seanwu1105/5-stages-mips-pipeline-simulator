package io.github.seanwu1105.mipsprocessor.component.pipeline;

import io.github.seanwu1105.mipsprocessor.component.HazardDetectionUnit;
import io.github.seanwu1105.mipsprocessor.component.Register;
import io.github.seanwu1105.mipsprocessor.controller.MainController;
import io.github.seanwu1105.mipsprocessor.signal.FunctionCode;
import io.github.seanwu1105.mipsprocessor.signal.Instruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class InstructionDecode implements Stage {

    @NotNull
    private final InstructionFetchToInstructionDecodeRegister ifId;

    @NotNull
    private final MainController mainController;

    @NotNull
    private final Register register;

    @Nullable
    private HazardDetectionUnit hazardDetectionUnit;

    private int programCounter;

    @NotNull
    private Instruction currentInstruction = Instruction.NOP;

    public InstructionDecode(
            @NotNull final InstructionFetchToInstructionDecodeRegister ifId,
            @NotNull final MainController mainController,
            @NotNull final Register register
    ) {
        this.ifId = ifId;
        this.mainController = mainController;
        this.register = register;
    }

    @Override
    public void run() {
        assert hazardDetectionUnit != null;
        if (hazardDetectionUnit.needStalling())
            currentInstruction = Instruction.NOP;
        else
            currentInstruction = ifId.getInstruction();
        mainController.setInstruction(currentInstruction);
        programCounter = ifId.getProgramCounter();
    }

    @Override
    public boolean hasInstruction() {
        return currentInstruction != Instruction.NOP;
    }

    public void setHazardDetectionUnit(@NotNull final HazardDetectionUnit hazardDetectionUnit) {
        this.hazardDetectionUnit = hazardDetectionUnit;
    }

    @NotNull
    public MainController.RegisterDestination getRegisterDestination() {
        return mainController.getRegisterDestination();
    }

    @NotNull
    public MainController.AluOperation getAluOperation() {
        return mainController.getAluOperation();
    }

    @NotNull
    public MainController.AluSource getAluSource() {
        return mainController.getAluSource();
    }

    @NotNull
    public MainController.Branch getBranch() {
        return mainController.getBranch();
    }

    @NotNull
    public MainController.MemoryRead getMemoryRead() {
        return mainController.getMemoryRead();
    }

    @NotNull
    public MainController.MemoryWrite getMemoryWrite() {
        return mainController.getMemoryWrite();
    }

    @NotNull
    public MainController.RegisterWrite getRegisterWrite() {
        return mainController.getRegisterWrite();
    }

    @NotNull
    public MainController.MemoryToRegister getMemoryToRegister() {
        return mainController.getMemoryToRegister();
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public int getRegisterData1() {
        return readRegister(currentInstruction.getRs());
    }

    public int getRegisterData2() {
        return readRegister(currentInstruction.getRt());
    }

    public int getImmediate() {
        return currentInstruction.getImmediate();
    }

    @Nullable
    public FunctionCode getFunctionCode() {
        try {
            return currentInstruction.getFunctionCode();
        } catch (final IllegalStateException e) {
            return null;
        }
    }

    int getRs() {
        return currentInstruction.getRs();
    }

    public int getRt() {
        return currentInstruction.getRt();
    }

    public int getRd() {
        return currentInstruction.getRd();
    }

    public int readRegister(final int address) {
        register.setReadAddress1(address);
        return register.readData1();
    }

    @NotNull
    public Set<Integer> getWrittenRegisterAddresses() {
        return register.getWrittenAddresses();
    }
}
