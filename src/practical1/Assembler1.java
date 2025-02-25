package practical1;
 
 
 
import java.io.BufferedReader; 
import java.io.FileInputStream; 
import java.io.FileWriter; 
import java.io.InputStreamReader; 
import java.io.PrintWriter; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.LinkedHashMap; 
import java.util.Map; 
import java.util.StringTokenizer; 
 
class Tuple { 
    String mnemonic, m_class, opcode; 
    int length; 
 
    Tuple() {} 
 
    Tuple(String s1, String s2, String s3, String s4) { 
        mnemonic = s1; 
        m_class = s2; 
        opcode = s3; 
        length = Integer.parseInt(s4); 
    } 
} 
 
class SymTuple { 
    String symbol, address; 
    int length; 
 
    SymTuple(String s1, String s2, int i1) { 
        symbol = s1; 
        address = s2; 
        length = i1; 
    } 
} 
 
class LitTuple { 
    String literal, address; 
    int length; 
 
    LitTuple() {} 
 
    LitTuple(String s1, String s2, int i1) { 
        literal = s1; 
        address = s2; 
        length = i1; 
    } 
} 
 
public class Assembler1 { 
    static int lc, iSymTabPtr = 0, iLitTabPtr = 0, iPoolTabPtr = 0; 
    static int[] poolTable = new int[10]; 
    static Map<String, Tuple> MOT; 
    static Map<String, SymTuple> symtable; 
    static ArrayList<LitTuple> littable; 
    static Map<String, String> regAddressTable; 
    static PrintWriter out_pass1; 
    static int line_no; 
 
    static String processLTORG() { 
        LitTuple litTuple; 
        StringBuilder intermediateStr = new StringBuilder(); 
        for (int i = poolTable[iPoolTabPtr - 1]; i < littable.size(); i++) { 
            litTuple = littable.get(i); 
            litTuple.address = lc + ""; 
            intermediateStr.append(lc).append(" (DL,02)  (C,") 
                    .append(litTuple.literal).append(") \n"); 
            lc++; 
        } 
        poolTable[iPoolTabPtr] = iLitTabPtr; 
        iPoolTabPtr++; 
        return intermediateStr.toString(); 
    } 
 
    static void pass1() throws Exception { 
        BufferedReader input = new BufferedReader(new InputStreamReader(new 
FileInputStream("src/practical1/INPUT.TXT"))); 
        out_pass1 = new PrintWriter(new FileWriter("src/practical1/output_pass1.txt"), true); 
        PrintWriter out_symtable = new PrintWriter(new FileWriter("src/practical1/Symtable.txt"), true); 
        PrintWriter out_littable = new PrintWriter(new FileWriter("src/practical1/Littable.txt"), true); 
        String s; 
        lc = 0; 
        while ((s = input.readLine()) != null) { 
            StringTokenizer st = new StringTokenizer(s, " ", false); 
            String[] s_arr = new String[st.countTokens()]; 
            for (int i = 0; i < s_arr.length; i++) { 
                s_arr[i] = st.nextToken(); 
            } 
            if (s_arr.length == 0) { 
                continue; 
            } 
            int curIndex = 0; 
            if (s_arr.length == 3) { 
                String label = s_arr[0]; 
                insertIntoSymTab(label, lc + ""); 
                curIndex = 1; 
            } 
            String curToken = s_arr[curIndex]; 
            Tuple curTuple = MOT.get(curToken); 
            StringBuilder intermediateStr = new StringBuilder(); 
            if (curTuple.m_class.equalsIgnoreCase("IS")) { 
                intermediateStr.append(lc).append(" (") 
                        .append(curTuple.m_class).append(",") 
                        .append(curTuple.opcode).append(" ) "); 
                lc += curTuple.length; 
                intermediateStr.append(processOperands(s_arr[curIndex + 1])); 
            } else if (curTuple.m_class.equalsIgnoreCase("AD")) { 
                if (curTuple.mnemonic.equalsIgnoreCase("START")) { 
                    intermediateStr.append(lc).append(" (") 
                            .append(curTuple.m_class).append(",") 
                            .append(curTuple.opcode).append(") "); 
                    lc = Integer.parseInt(s_arr[curIndex + 1]); 
                    intermediateStr.append("(C,").append(s_arr[curIndex + 1]).append(") "); 
                } else if (curTuple.mnemonic.equalsIgnoreCase("LTORG")) { 
                    intermediateStr.append(processLTORG()); 
                } else if (curTuple.mnemonic.equalsIgnoreCase("END")) { 
                    intermediateStr.append(lc).append(" (") 
                            .append(curTuple.m_class).append(",") 
                            .append(curTuple.opcode).append(")  \n"); 
                    intermediateStr.append(processLTORG()); 
                } 
            } else if (curTuple.m_class.equalsIgnoreCase("DL")) { 
                intermediateStr.append(lc).append(" (") 
                        .append(curTuple.m_class).append(",") 
                        .append(curTuple.opcode).append(") "); 
                if (curTuple.mnemonic.equalsIgnoreCase("DS")) { 
                    lc += Integer.parseInt(s_arr[curIndex + 1]); 
                } else if (curTuple.mnemonic.equalsIgnoreCase("DC")) { 
                    lc += curTuple.length; 
                } 
                intermediateStr.append("(C,").append(s_arr[curIndex + 1]).append(") "); 
            } 
            System.out.println(intermediateStr); 
 
            out_pass1.println(intermediateStr); 
        } 
        out_pass1.flush(); 
        out_pass1.close(); 
 
        System.out.println("====== Symbol Table ======"); 
        SymTuple tuple; 
        for (Map.Entry<String, SymTuple> entry : symtable.entrySet()) { 
            tuple = entry.getValue(); 
            String tableEntry = tuple.symbol + "\t" + tuple.address; 
            out_symtable.println(tableEntry); 
            System.out.println(tableEntry); 
        } 
        out_symtable.flush(); 
        out_symtable.close(); 
 
        System.out.println("===== Literal Table ======"); 
        LitTuple litTuple; 
        for (LitTuple value : littable) { 
            litTuple = value; 
            String tableEntry = litTuple.literal + "\t" + litTuple.address; 
            out_littable.println(tableEntry); 
            System.out.println(tableEntry); 
        } 
        out_littable.flush(); 
        out_littable.close(); 
    } 
 
    static String processOperands(String operands) { 
        StringTokenizer st = new StringTokenizer(operands, ",", false); 
        String[] s_arr = new String[st.countTokens()]; 
        for (int i = 0; i < s_arr.length; i++) { 
            s_arr[i] = st.nextToken(); 
        } 
        StringBuilder intermediateStr = new StringBuilder(); 
        for (String curToken : s_arr) { 
            if (curToken.startsWith("=")) { 
                StringTokenizer str = new StringTokenizer(curToken, "'", false); 
                String[] tokens = new String[str.countTokens()]; 
                for (int j = 0; j < tokens.length; j++) { 
                    tokens[j] = str.nextToken(); 
                } 
                String literal = tokens[1]; 
                insertIntoTab(literal, ""); 
                intermediateStr.append("(L,").append(iLitTabPtr - 1).append(")"); 
            } else if (regAddressTable.containsKey(curToken)) { 
                intermediateStr.append("(RG,").append(regAddressTable.get(curToken)).append(") "); 
            } else { 
                insertIntoSymTab(curToken, ""); 
                intermediateStr.append("(S,").append(iSymTabPtr - 1).append(")"); 
            } 
        } 
        return intermediateStr.toString(); 
    } 
 
    static void insertIntoSymTab(String symbol, String address) { 
        if (symtable.containsKey(symbol)) { 
            // If the symbol already exists, update the address only if the address is empty 
            SymTuple s = symtable.get(symbol); 
            if (s.address.equals("") && !address.equals("")) { 
                s.address = address; 
            } 
        } else { 
            // If the symbol does not exist, insert it with the provided address 
            symtable.put(symbol, new SymTuple(symbol, address.equals("") ? lc + "" : address, 1)); 
        } 
        iSymTabPtr++; 
    } 
     
     
 
    static void insertIntoTab(String literal, String address) { 
        littable.add(iLitTabPtr, new LitTuple(literal, address, 1)); 
        iLitTabPtr++; 
    } 
 
    static void initializeTables() throws Exception { 
        symtable = new LinkedHashMap<>(); 
        littable = new ArrayList<>(); 
        regAddressTable = new HashMap<>(); 
        MOT = new HashMap<>(); 
        String s, mnemonic; 
        BufferedReader br; 
        br = new BufferedReader(new InputStreamReader(new 
FileInputStream("src/practical1/MOT.TXT"))); 
        while ((s = br.readLine()) != null) { 
            StringTokenizer st = new StringTokenizer(s, " ", false); 
            mnemonic = st.nextToken(); 
            MOT.put(mnemonic, new Tuple(mnemonic, st.nextToken(), st.nextToken(), st.nextToken())); 
        } 
        br.close(); 
        regAddressTable.put("AREG", "1"); 
        regAddressTable.put("BREG", "2"); 
        regAddressTable.put("CREG", "3"); 
        regAddressTable.put("DREG", "4"); 
 
        poolTable[iPoolTabPtr] = iLitTabPtr; 
        iPoolTabPtr++; 
    } 
 
    public static void main(String[] args) throws Exception { 
        initializeTables(); 
        System.out.println("====== Pass 1 OUTPUT =====\n"); 
        pass1(); 
    } 
} 




 
 
 
 
 
 