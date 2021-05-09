import java.io.*;
import java.util.*;

import static java.lang.Math.*;

public class Assembler {
    public static void main(String[] args) throws IOException {
        ISA isa = new ISA(4, 7,"Opcode.txt","Assemblycode.txt");
        isa.printBinaryToFile();
        isa.printHexToFile();
    }
}