package component.pipeline;

import component.Register;
import controller.MainController;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WriteBack implements Stage {

    @NotNull
    private final MemoryAccessToWriteBackRegister memWb;

    @NotNull
    private final Register register;

    public WriteBack(@NotNull MemoryAccessToWriteBackRegister memWb, @NotNull Register register) {
        this.memWb = memWb;
        this.register = register;
    }

    @Override
    public void run() {
        register.setRegisterWrite(Objects.requireNonNull(memWb.getRegisterWrite()));
        if (memWb.getRegisterWrite() == MainController.RegisterWrite.TRUE)
            register.setWriteAddress(memWb.getWriteRegisterAddress());

        if (memWb.getMemoryToRegister() == MainController.MemoryToRegister.FROM_ALU_RESULT)
            register.write(memWb.getAluResult());
        else if (memWb.getMemoryToRegister() == MainController.MemoryToRegister.FROM_MEMORY)
            register.write(memWb.getMemoryReadData());
    }
}
