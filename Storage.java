import java.util.*;

public class Storage {
    private int bit;
    private int size;
    private int t_size;
    private int s_size;
    private String paddingBinary;

    public class Info {
        public String assemblyCode;
        public String binaryCode;

        Info(String assemblyCode, String binaryCode) {
            this.assemblyCode = assemblyCode;
            this.binaryCode = binaryCode;
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