package io.github.seanwu1105.mipsprocessor.component.pipeline;

import io.github.seanwu1105.mipsprocessor.component.HazardDetectionUnit;
import io.github.seanwu1105.mipsprocessor.component.Memory;
import io.github.seanwu1105.mipsprocessor.controller.MainController;
import io.github.seanwu1105.mipsprocessor.signal.Instruction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InstructionFetchTest {

    @NotNull
    private Memory instructionMemory;

    @NotNull
    private InstructionFetch instructionFetch;

    @NotNull
    private HazardDetectionUnit hazardDetectionUnit;

    @BeforeEach
    void buildUp() {
        instructionMemory = new Memory();
        instructionMemory.setMemoryWrite(MainController.MemoryWrite.TRUE);

        hazardDetectionUnit = mock(HazardDetectionUnit.class);
        when(hazardDetectionUnit.needStalling()).thenReturn(false);

        instructionFetch = new InstructionFetch(instructionMemory);
        instructionFetch.setHazardDetectionUnit(hazardDetectionUnit);
    }

    @Test
    void testInstructionFetchRun() {
        final var instructions = List.of(
                new Instruction("10001101000000010000000000000011"),
                new Instruction("00000000000000100001100000100000")
        );

        setInstructions(instructions);

        var number = 0;
        for (final var instruction : instructions) {
            instructionFetch.run();
            assertEquals((number + 1) * 4, instructionFetch.getProgramCounter());
            assertEquals(instruction, instructionFetch.getInstruction());
            number++;
        }
    }

    private void setInstructions(@NotNull final Iterable<Instruction> instructions) {
        final var initProgramCounter = 0;
        var number = 0;
        for (final var instruction : instructions) {
            instructionMemory.setAddress(initProgramCounter + number * 4);
            instructionMemory.write(instruction);
            number++;
        }
    }

    @Test
    void testStalling() {
        final var instructions = List.of(
                new Instruction("000000 00001 00000 00000 00000 100000"), // add $1, $0, $0
                new Instruction("000000 00010 00000 00000 00000 100000")  // add $2, $0, $0
        );

        setInstructions(instructions);

        when(hazardDetectionUnit.needStalling()).thenReturn(true);

        final var expectedProgramCounter = 0;
        instructionFetch.run();
        assertEquals(expectedProgramCounter, instructionFetch.getProgramCounter());
    }

    @AfterEach
    void tearDown() {
        instructionMemory.setMemoryWrite(MainController.MemoryWrite.FALSE);
    }
}
