package io.github.seanwu1105.mipsprocessor.controller;

import io.github.seanwu1105.mipsprocessor.component.Alu;
import io.github.seanwu1105.mipsprocessor.signal.FunctionCode;
import org.junit.jupiter.api.Test;

import static io.github.seanwu1105.mipsprocessor.controller.MainController.AluOperation.BRANCH;
import static io.github.seanwu1105.mipsprocessor.controller.MainController.AluOperation.MEMORY_REFERENCE;
import static io.github.seanwu1105.mipsprocessor.controller.MainController.AluOperation.R_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AluControllerTest {

    @Test
    void testGetAluControl() {
        assertEquals(Alu.AluControl.ADD, AluController.getAluControl(R_TYPE, FunctionCode.ADD));
        assertEquals(Alu.AluControl.SUBTRACT, AluController.getAluControl(R_TYPE, FunctionCode.SUBTRACT));
        assertEquals(Alu.AluControl.AND, AluController.getAluControl(R_TYPE, FunctionCode.AND));
        assertEquals(Alu.AluControl.OR, AluController.getAluControl(R_TYPE, FunctionCode.OR));
        assertEquals(Alu.AluControl.SET_ON_LESS_THAN, AluController.getAluControl(R_TYPE, FunctionCode.SET_ON_LESS_THAN));
        assertEquals(Alu.AluControl.ADD, AluController.getAluControl(MEMORY_REFERENCE, FunctionCode.OR /* don't care */));
        assertEquals(Alu.AluControl.ADD, AluController.getAluControl(MEMORY_REFERENCE, FunctionCode.OR /* don't care */));
        assertEquals(Alu.AluControl.SUBTRACT, AluController.getAluControl(BRANCH, FunctionCode.OR /* don't care */));
    }
}
