import java.util.*;
public class Operations {

    public class Info {
        public final String assemblyCode;
        public String readInstruction;
        public String writeInstruction;
        public String binaryCode;
        public int wordCount=0;

        Info(String assemblyCode, String binaryCode, String readInstruction, String writeInstruction) {
            this.readInstruction = readInstruction;
            this.writeInstruction = writeInstruction;
            this.assemblyCode = assemblyCode;
            this.binaryCode = binaryCode;
            for (int i = 0; i < readInstruction.length(); i++) {
                wordCount++;
            }
        }
    }

    public HashMap<String, Info> map;

    Operations(ArrayList<ArrayList<String>> data,int bit) {
        map = new HashMap<>();
        String padding="0".repeat(bit);
        for (int x = 0; x < data.size(); x++) {
            String readInstruction = data.get(x).get(0);
            String writeInstruction = data.get(x).get(1);
            for (int i = 3; i < data.get(x).size(); i += 2) {
                String code = data.get(x).get(i - 1);
                String binary =data.get(x).get(i);
                String binaryPadded= (padding+binary).substring(binary.length());
                Info info = new Info(code, binaryPadded, readInstruction, writeInstruction);
                map.put(code, info);
            }
        }
    }
}