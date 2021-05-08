import java.io.*;
import java.util.*;

import static java.lang.Math.*;

public class SingleISA {
    public static class CodeLine {
        public String oxab[] = new String[4];
        public StringBuilder binaryCodeLine;
        public StringBuilder hexCodeLine;
    }

    public static class Operations {

        public class Info {
            public final String assemblyCode;
            public String readInstruction;
            public String writeInstruction;
            public String binaryCode;
            public String hexCode;
            public int wordCount=0;

            Info(String assemblyCode, String binaryCode, String readInstruction, String writeInstruction) {
                this.readInstruction = readInstruction;
                this.writeInstruction = writeInstruction;
                this.assemblyCode = assemblyCode;
                this.binaryCode = binaryCode;
                this.hexCode = Integer.toString(Integer.parseInt(binaryCode, 2), 16).toUpperCase();
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

    public static class Storage {
        private int bit;
        private int size;
        private int t_size;
        private int s_size;
        private String paddingBinary;

        public class Info {
            public String assemblyCode;
            public String binaryCode;
            public String hexCode;

            Info(String assemblyCode, String binaryCode) {
                this.assemblyCode = assemblyCode;
                this.binaryCode = binaryCode;
                this.hexCode=Integer.toString(Integer.parseInt(binaryCode,2),16).toUpperCase();
            }
        }

        public Map<String, Info> map;

        Storage(int bit, int tempSize) {
            setBit(bit);
            setT_size(tempSize);
            map = new HashMap<>();
            map = new HashMap<>();
            map.put("$zero", new Info("$zero", paddingBinary));
            for (int i = 1; i < size; i++) {
                StringBuilder sb = new StringBuilder();
                for (int n = bit - 1; n >= 0; n--) {
                    sb.append((((i & (1 << n)) != 0) ? '1' : '0'));
                }
                String assembly = (i <= getT_size()) ? "$t" + (i - 1) : "$s" + (i - 1 - getT_size());
                map.put(assembly, new Info(assembly, sb.toString()));
            }
        }

        public int getBit() {
            return bit;
        }

        private void setBit(int bit) {
            this.bit = bit;
            setSize(1 << bit);
            setPaddingBinary("0".repeat(bit));
        }

        public int getSize() {
            return size;
        }

        private void setSize(int size) {
            this.size = size;
        }

        public int getT_size() {
            return t_size;
        }

        private void setT_size(int t_size) {
            this.t_size = t_size;
            setS_size(size - t_size - 1);
        }

        public int getS_size() {
            return s_size;
        }

        private void setS_size(int s_size) {
            this.s_size = s_size;
        }

        private void setPaddingBinary(String padding) {
            this.paddingBinary = padding;
        }

        public String getPaddingBinary(){
            return this.paddingBinary;
        }
    }

    public static class ISA {

        private Operations operations;
        private Storage storage;

        private void readOpcode(String opsFilePath_txt) throws FileNotFoundException {
            Scanner sc = new Scanner(new FileInputStream(opsFilePath_txt));
            ArrayList<ArrayList<String>> sup = new ArrayList<>();
            ArrayList<String> sub = new ArrayList<>();
            boolean found = sc.hasNextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.length() == 0) {
                    sup.add(sub);
                    sub = new ArrayList<>();
                    continue;
                }
                String[] split = line.split(" ");
                sub.addAll(Arrays.asList(split));
            }
            if (found) {
                sup.add(sub);
            }
            sc.close();
            operations = new Operations(sup, storage.getBit());
        }

        ArrayList<CodeLine> codeLines;

        void readAssemblyCodeFromFile(String AssemblyFilePath_txt) throws FileNotFoundException {
            Scanner sc;
            try {
                sc = new Scanner(new FileInputStream(AssemblyFilePath_txt));
            } catch (Exception e) {
                sc = new Scanner(System.in);
            }
            codeLines = new ArrayList<>();
            boolean found = sc.hasNextLine();
            while (true) {
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine();
                if (line.length() == 0) break;
                String split[] = line.trim().split(" ");
                int at = 0;
                CodeLine currentLine = new CodeLine();
                for (int i = 0; i < split.length; i++) {
                    if (split[i].length() > 0 && operations.map.containsKey(split[i])) {
                        currentLine.oxab[0] = split[i];
                        at = i + 1;
                        break;
                    }
                }
                if (at != 0) {
                    Operations.Info info = operations.map.get(currentLine.oxab[0]);
                    int wordcount = info.wordCount - 1, wordfound = 0;
                    String[] xab = new String[wordcount];
                    for (int i = at; i < split.length && wordfound < wordcount; i++) {
                        int firstBracketIndex = split[i].indexOf('(');
                        if (firstBracketIndex != -1) {
                            String outsizeBracket = split[i].substring(0, firstBracketIndex);
                            xab[wordfound++] = outsizeBracket;
                            int lastBracketIndex = split[i].indexOf(')', firstBracketIndex + 1);
                            String insideBracket = split[i].substring(firstBracketIndex + 1, lastBracketIndex);
                            xab[wordfound++] = insideBracket;
                        } else {
                            xab[wordfound++] = split[i];
                        }
                    }
                    if (wordfound == wordcount) {
                        if (wordcount == 1) {
                            currentLine.oxab[1] = xab[0];
                        } else {
                            currentLine.oxab[1] = xab[0];
                            currentLine.oxab[2] = xab[2];
                            currentLine.oxab[3] = xab[1];
                        }
                    }
                    codeLines.add(currentLine);
                    writeTheAssemblyCode();
                }
            }
            sc.close();
        }

        private void writeTheAssemblyCode() {
            CodeLine currentLine = codeLines.get(codeLines.size() - 1);
            Operations.Info info = operations.map.get(currentLine.oxab[0]);
            //TODO fun starts
            currentLine.binaryCodeLine = new StringBuilder();
            currentLine.hexCodeLine = new StringBuilder();
            String lastBufferBinary = "";
            String lastBufferHex = "";
            int overflow = 5 - info.readInstruction.length();
            int looping = 0;
            for (int i = 0; i < info.readInstruction.length(); i++) {
                if (looping++ > 5) {
                    System.err.println("You made spelling mistake maybe");
                    break;
                }
                if (info.readInstruction.charAt(i) == 'o') {
                    currentLine.binaryCodeLine.append(info.binaryCode);
                    currentLine.hexCodeLine.append(info.hexCode);
                } else if (info.readInstruction.charAt(i) == 'v') {
                    char c = info.writeInstruction.charAt(i);
                    if (c != 'x') {
                        String str = currentLine.oxab[(int) (c - '0')];
                        Storage.Info Info = storage.map.get(str);
                        if (Info == null) {
                            String temp = currentLine.oxab[c - '0'];
                            currentLine.oxab[c - '0'] = currentLine.oxab[3];
                            currentLine.oxab[3] = temp;
                            i = i - 1;
                            continue;
                        }
                        currentLine.binaryCodeLine.append(Info.binaryCode);
                        currentLine.hexCodeLine.append(Info.hexCode);
                    } else {

                    }
                } else if (info.readInstruction.charAt(i) == 'n') {
                    char c = info.writeInstruction.charAt(i);
                    if (c == '-') {
                        lastBufferBinary = paddedStringBinary(currentLine.oxab[3], overflow);
                        lastBufferHex = paddedStringHex(currentLine.oxab[3], overflow);
                        continue;
                    }
                    String str = currentLine.oxab[(int) c - '0'];
                    currentLine.binaryCodeLine.append(paddedStringBinary(str, overflow));
                    currentLine.hexCodeLine.append(paddedStringHex(str, overflow));
                }
            }
            currentLine.binaryCodeLine.append(lastBufferBinary);
            currentLine.hexCodeLine.append(lastBufferHex);
            System.out.println(currentLine.hexCodeLine);
        }


        private String paddedStringBinary(String str, int overflow) {
            String binary = Integer.toBinaryString(Integer.parseInt(str));
            if (binary.length() <= overflow * storage.getBit()) {
                String paddedBinary = (storage.getPaddingBinary().repeat(overflow) + binary).substring(binary.length());
                return paddedBinary;
            }
            return binary.substring(storage.getBit());
        }

        private String paddedStringHex(String str, int overflow) {
            String hex = Integer.toHexString(Integer.parseInt(str));
            String hexCap=("0".repeat(overflow)+hex).substring(hex.length()).toUpperCase();
            return hexCap;
        }

        public void printBinary() {
            for (int i = 0; i < codeLines.size(); i++) {
                System.out.println(codeLines.get(i).binaryCodeLine);
            }
        }

        public void printHex() {
            for (int i = 0; i < codeLines.size(); i++) {
                System.out.println(codeLines.get(i).hexCodeLine);
            }
        }

        public void printBinaryHex() {
            for (int i = 0; i < codeLines.size(); i++) {
                System.out.println(codeLines.get(i).binaryCodeLine + " " + codeLines.get(i).hexCodeLine);
            }
        }

        public void printHexToFile() {
            String string = "HexCode.txt";
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(string));
                for (int i = 0; i < codeLines.size(); i++) {
                    out.write(String.valueOf(codeLines.get(i).hexCodeLine + ((codeLines.size() - 1 == i) ? "" : "\n")));
                }
                out.close();
                System.err.println("Hex File created successfully" + System.getProperty("user.dir") + "\\" + string);
            } catch (IOException e) {
            }
        }

        public void printBinaryToFile() {
            String string = "BinaryCode.txt";
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(string));
                for (int i = 0; i < codeLines.size(); i++) {
                    out.write(String.valueOf(codeLines.get(i).binaryCodeLine + ((codeLines.size() - 1 == i) ? "" : "\n")));
                }
                out.close();
                System.err.println("Binary File created successfully " + System.getProperty("user.dir") + "\\" + string);
            } catch (IOException e) {
            }
        }

        private void builder(int bit, int totalTempSize, String OpcodePath_txt) throws FileNotFoundException {
            storage = new Storage(bit, totalTempSize);
            readOpcode(OpcodePath_txt);
        }


        public ISA(int bit, int totalTempSize, String OpcodePath_txt, String AssemblyCodePath_txt) throws FileNotFoundException {
            builder(bit, totalTempSize, OpcodePath_txt);
            readAssemblyCodeFromFile(AssemblyCodePath_txt);
        }
    }

    public static void main(String[] args) throws IOException {
        ISA isa = new ISA(4, 7,"Opcode.txt","AssemblyCode.txt");
        isa.printBinaryToFile();
        isa.printHexToFile();
    }
}