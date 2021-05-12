import java.io.*;
import java.util.*;

public class ISA {

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
            String line = sc.nextLine().toLowerCase();
            System.out.print(line+"\t\t");
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
                System.out.println("You made spelling mistake maybe");
                return;
            }
            if(currentLine.oxab[1].equals("$zero")){
                System.out.println("You can't put something in zero, silly");
                return;
            }
            if (info.readInstruction.charAt(i) == 'o') {
                currentLine.binaryCodeLine.append(info.binaryCode);
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
                }
            } else if (info.readInstruction.charAt(i) == 'n') {
                char c = info.writeInstruction.charAt(i);
                if (c == '-') {
                    lastBufferBinary = paddedStringBinary(currentLine.oxab[3], overflow);
                    continue;
                }
                String str = currentLine.oxab[(int) c - '0'];
                currentLine.binaryCodeLine.append(paddedStringBinary(str, overflow));
            }
        }
        currentLine.binaryCodeLine.append(lastBufferBinary);
        currentLine.hexCodeLine.append(hexit(currentLine.binaryCodeLine.toString()));
        System.out.print(currentLine.binaryCodeLine + " ");
        System.out.println(currentLine.hexCodeLine);
    }


    private String paddedStringBinary(String str, int overflow) {
        String binary = Integer.toBinaryString(Integer.parseInt(str));
        String overflowStr = storage.getPaddingBinary().repeat(overflow);
        String binaryStr = new String();
        if (binary.length() > storage.getBit()) {
            binaryStr = binary.substring(binary.length()-storage.getBit());
        } else {
            binaryStr = (storage.getPaddingBinary() + binary).substring(storage.getBit());
        }
        String paddedBinary = (overflowStr + binaryStr).substring(binaryStr.length());
        return paddedBinary;
    }

    public String hexit(String bin) {
        String hex = Integer.toString(Integer.parseInt(bin, 2), 16);
        return ("0".repeat(storage.getBit()) + hex).substring(hex.length()).toUpperCase();
    }

    private String paddedStringHex(String str, int overflow) {
        String hex = Integer.toHexString(Integer.parseInt(str));
        String hexCap = ("0".repeat(overflow) + hex).substring(hex.length()).toUpperCase();
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
            System.out.println("Hex File created successfully" + System.getProperty("user.dir") + "\\" + string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printHexToFileLogisim() {
        String string = "Load";
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(string));
            out.write("v2.0 raw\n");
            for (int i = 0; i < codeLines.size(); i++) {
                out.write(String.valueOf(codeLines.get(i).hexCodeLine + ((codeLines.size() - 1 == i) ? "" : "\n")));
            }
            out.close();
            System.out.println("Logisim File created successfully" + System.getProperty("user.dir") + "\\" + string);
        } catch (IOException e) {
            e.printStackTrace();
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
            System.out.println("Binary File created successfully " + System.getProperty("user.dir") + "\\" + string);
        } catch (IOException e) {
            e.printStackTrace();
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
