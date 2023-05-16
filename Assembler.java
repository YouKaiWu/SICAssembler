import java.util.ArrayList;
import java.util.HashMap;

public class Assembler {

    static HashMap<String, Integer> symTab = new HashMap<>(); // key: symbol, value: locCtr
    static HashMap<String, String> opTab = new HashMap<>() {
        {

            put("ADD", "18");
            put("ADDF", "58");
            put("ADDR", "90");
            put("AND", "40");
            put("CLEAR", "B4");
            put("COMP", "28");
            put("COMPF", "88");
            put("COMPR", "A0");
            put("DIV", "24");
            put("DIVF", "64");
            put("DIVR", "9C");
            put("FIX", "C4");
            put("FLOAT", "C0");
            put("HIO", "F4");
            put("J", "3c");
            put("JEQ", "30");
            put("JGT", "34");
            put("JLT", "38");
            put("JSUB", "48");
            put("LDA", "00");
            put("LDB", "68");
            put("LDCH", "50");
            put("LDF", "70");
            put("LDL", "08");
            put("LDS", "6C");
            put("LDT", "74");
            put("LDX", "04");
            put("LPS", "D0");
            put("MUL", "20");
            put("MULF", "60");
            put("MULR", "98");
            put("NORM", "c8");
            put("OR", "44");
            put("RD", "D8");
            put("RMO", "AC");
            put("RSUB", "4C");
            put("SHIFTL", "A4");
            put("SHIFTR", "A8");
            put("SIO", "F0");
            put("SSK", "EC");
            put("STA", "0C");
            put("STB", "78");
            put("STCH", "54");
            put("STF", "80");
            put("STI", "D4");
            put("STL", "14");
            put("STS", "7C");
            put("STSW", "E8");
            put("STT", "84");
            put("STX", "10");
            put("SUB", "1C");
            put("SUBF", "5C");
            put("SUBR", "94");
            put("SVC", "B0");
            put("TD", "E0");
            put("TIO", "F8");
            put("TIX", "2C");
            put("TIXR", "B8");
            put("WD", "DC");
        }

    }; // key: mnemonic, value: opCode

    public static void main(String[] args) {
        passOne(FileManage.load(args[0]));
        passTwo(FileManage.load(args[0]));
    }

    public static boolean isComment(String[] line) {
        return line[0].equals(".");
    }

    public static boolean hasLabel(String[] line) {
        if (line.length > 2)
            return true;
        return false;
    }

    public static String getLabel(String[] line) {
        if (hasLabel(line))
            return line[0];
        return "";
    }

    public static String getOpcode(String[] line) {
        if (hasLabel(line))
            return line[1];
        return line[0];
    }

    public static String getOperand(String[] line) {
        if (hasLabel(line))
            return line[2];
        if (line.length == 1)
            return "";
        return line[1];
    }

    public static void setErrorFlag(String errorMessage) {
        System.out.println(errorMessage);
    }

    public static void passOne(ArrayList<String> sourceCode) {
        String[] firstLine = sourceCode.get(0).trim().split("\\s+");
        int locCtr = 0;
        boolean hasStartLabel = false;
        if (getOpcode(firstLine).equals("START")) {
            locCtr = Integer.parseInt(getOperand(firstLine), 16);
            hasStartLabel = true;
        }
        int startingAddress = locCtr;
        StringBuilder content = new StringBuilder("");
        for (int i = 0; i < sourceCode.size(); i++) {
            String[] line = sourceCode.get(i).trim().split("\\s+");

            if (isComment(line)) {
                content.append(sourceCode.get(i) + '\n');
                continue;
            }

            String loc = Integer.toHexString(locCtr).toUpperCase();

            if (hasStartLabel && i == 0) {
                content.append(loc + "\t" + sourceCode.get(i) + '\n');
                continue;
            }

            if (hasLabel(line)) {
                String label = getLabel(line);
                if (symTab.containsKey(label)) {
                    setErrorFlag("duplicate symbol");
                } else {
                    symTab.put(label, locCtr);
                }
            }

            String opCode = getOpcode(line);
            if (opCode.equals("END")) {
                String programLength = Integer.toHexString(locCtr - startingAddress).toUpperCase();
                content.append("program length:" + programLength);
                continue;
            }

            if (opTab.containsKey(opCode)) {
                locCtr += 3;
            } else if (opCode.equals("WORD")) {
                locCtr += 3;
            } else if (opCode.equals("RESW")) {
                int operand = Integer.parseInt(getOperand(line));
                locCtr += 3 * operand;
            } else if (opCode.equals("RESB")) {
                int operand = Integer.parseInt(getOperand(line));
                locCtr += operand;
            } else if (opCode.equals("BYTE")) {
                String operand = getOperand(line);
                int chLen = operand.length() - 3;

                if (operand.charAt(0) == 'C') {
                    locCtr += chLen;
                } else if (operand.charAt(0) == 'X') {
                    locCtr += chLen / 2;
                }
            } else {
                setErrorFlag("invalid operation code");
            }

            content.append(loc + "\t" + sourceCode.get(i) + '\n');
        }
        FileManage.save("intermediateFile.txt", content.toString());
    }

    public static StringBuilder writeHeaderRecord(String programName, String startingAddress, String programLength) {
        StringBuilder content = new StringBuilder("");
        content.append("H" + programName + "  " + startingAddress + programLength);
        return content;
    }

    public static void passTwo(ArrayList<String> intermediateFile) {
        String[] firstLine = intermediateFile.get(0).trim().split("\\s+");
        String[] lastLine = intermediateFile.get(intermediateFile.size() - 1).trim().split("\\s+");
        int locCtr = 0;
        boolean hasStartLabel = false;
        StringBuilder content = new StringBuilder("");
        if (getOpcode(firstLine).equals("START")) {
            locCtr = Integer.parseInt(getOperand(firstLine), 16);
            hasStartLabel = true;
            String loc = Integer.toHexString(locCtr).toUpperCase();
            content.append(writeHeaderRecord(getLabel(firstLine), loc, getOperand(lastLine)));
        }
        int startingAddress = locCtr;

        for (int i = 0; i < intermediateFile.size(); i++) {
            String[] line = intermediateFile.get(i).trim().split("\\s+");

            if (isComment(line)) {
                content.append(intermediateFile.get(i) + '\n');
                continue;
            }

            String loc = Integer.toHexString(locCtr).toUpperCase();

            if (hasStartLabel && i == 0) {
                content.append(loc + "\t" + intermediateFile.get(i) + '\n');
                continue;
            }

            if (hasLabel(line)) {
                String label = getLabel(line);
                if (symTab.containsKey(label)) {
                    setErrorFlag("duplicate symbol");
                } else {
                    symTab.put(label, locCtr);
                }
            }

            String opCode = getOpcode(line);
            if (opCode.equals("END")) {
                String programLength = Integer.toHexString(locCtr - startingAddress).toUpperCase();
                content.append("program length:" + programLength);
                continue;
            }

            if (opTab.containsKey(opCode)) {
                locCtr += 3;
            } else if (opCode.equals("WORD")) {
                locCtr += 3;
            } else if (opCode.equals("RESW")) {
                int operand = Integer.parseInt(getOperand(line));
                locCtr += 3 * operand;
            } else if (opCode.equals("RESB")) {
                int operand = Integer.parseInt(getOperand(line));
                locCtr += operand;
            } else if (opCode.equals("BYTE")) {
                String operand = getOperand(line);
                int chLen = operand.length() - 3;

                if (operand.charAt(0) == 'C') {
                    locCtr += chLen;
                } else if (operand.charAt(0) == 'X') {
                    locCtr += chLen / 2;
                }
            } else {
                setErrorFlag("invalid operation code");
            }

            content.append(loc + "\t" + intermediateFile.get(i) + '\n');
        }
    }
}