package Engine;

import Misc.LoggerConfigurator;

import java.util.ArrayList;
import java.util.logging.Logger;

// http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm#c16.2.4
// https://www.stmintz.com/ccc/index.php?id=137052
// https://www.chessprogramming.org/Extended_Position_Description

public class EpdInfo {
    
    // Invalid/OutOfBounds values indicate that the field is not set or the logical default.
    public int acd = -1; 
    public int acn = -1;
    public int acs = -1;
    public short[] am = null;
    public short[] bm = null;
    public String[] c = new String[10]; // c0-c9 comments
    public int ce = 32767; 
    public int dm = -1;
    public boolean draw_accept = false;
    public boolean draw_claim = false;
    public boolean draw_offer = false;
    public boolean draw_reject = false;
    public String eco = null;
    public int fmvn = 1;
    public int hmvc = 0;
    public String id = null;
    public String nic = null;
    public short pm = Move.None;
    public short[] pv = null;
    public int rc = 1;
    public boolean resign = false;
    public short sm = Move.None;
    public String[] v = new String[10]; // v0-v9 varation names
    public int tcgs = 0;
    public String[] tcri = new String[2];
    public String[] tcsi = new String[2];
    

    private static Logger logger = LoggerConfigurator.configureLogger(EpdInfo.class);

    public static EpdInfo ParseOperations(String str) {
        EpdInfo epd = new EpdInfo();
        logger.info("Parsing Epd operations: " + str);
        String[] operations = str.split(";");
        ArrayList<String> operands = new ArrayList<String>();
        for (int i = 0; i < operations.length; i++) {
            char[] operation = operations[i].toCharArray();
            int j = 0;
            while (operation[j] == ' ') j++;
            int opcodeBegin = j;
            while (operation[j] != ' ') j++;
            int opcodeLength = j - opcodeBegin;
            
            String opcode = new String(
                operation,
                opcodeBegin,
                opcodeLength
            );
            
            logger.info("Opcode: '" + opcode + "'");
            
            int operandBegin, operandLength;
            while (j < operation.length) {
                while (operation[j] == ' ') j++;
                switch (operation[j]) {
                    // <stringOperand>.
                    case '"':
                        j++;
                        operandBegin = j;
                        while (j < operation.length && operation[j] != '"') j++;
                        operandLength = j - operandBegin;
                        j++;
                        break;
                    // any other operand type will end before a space.
                    default:
                        operandBegin = j;
                        while (j < operation.length && operation[j] != ' ') j++;
                        operandLength = j - operandBegin;
                        break;
                }
                String operand = new String(
                    operation,
                    operandBegin,
                    operandLength
                );
                operands.add(operand);
                logger.info("Operand: '" + operand + "'");
            }
            
            int n;
            
            switch (opcode) {
                case "acd": epd.acs = Integer.parseInt(operands.get(0)); break;
                case "acn": epd.acn = Integer.parseInt(operands.get(0)); break;
                case "acs": epd.acs = Integer.parseInt(operands.get(0)); break;
                
                case "c0": case "c1": case "c2": case "c3": case "c4": case "c5": case "c6": case "c7": case "c8": case "c9":
                    n = opcode.charAt(1) - '0';
                    // First, all comment string registers with an index equal to or greater than N are set to null. (This is the set "cN" though "c9".) 
                    for (int k = n; k <= 9; k++) {
                        epd.c[k] = "";
                    }
                    // Second, and only if a string operand is present, the value of the corresponding comment string register is set equal to the string operand.
                    epd.c[n] = operands.size() > 0 ? operands.get(0) : "";
                    break;

                case "am":
                    epd.am = new short[operands.size()];
                    for (String SANmove : operands) {
                        // parse SAN move
                    }
                    break;
                case "bm":
                    epd.bm = new short[operands.size()];
                    for (String SANmove : operands) {
                        // parse SAN move
                    }
                    break;
                    
                case "ce":
                    epd.ce = Integer.parseInt(operands.get(0));
                    break;
                    
                case "dm":
                    epd.dm = Integer.parseInt(operands.get(0));
                    break;
                    
                case "draw_accept": epd.draw_accept = true; break;
                case "draw_claim": epd.draw_claim = true; break;
                case "draw_offer": epd.draw_offer = true; break;
                case "draw_reject": epd.draw_reject = true; break;

                case "eco":
                    epd.eco = operands.size() > 0 ? operands.get(0) : "";
                    break;
                    
                case "fmvn": epd.fmvn = Integer.parseInt(operands.get(0)); break;
                case "hmvc": epd.hmvc = Integer.parseInt(operands.get(0)); break;
                             
                case "id": 
                    epd.id = operands.get(0);
                    break;
                    
                case "nic":
                    epd.nic = operands.size() > 0 ? operands.get(0) : "";
                    break;

                case "noop":
                    logger.info("Received Noop with the following operands: ");                    
                    for (String operand : operands) {
                        logger.info(operand);                        
                    }
                    break;
                    
                case "pm":
                    // parse exactly one SAN move
                    break;
                
                case "pv":
                    epd.pv = new short[operands.size()];
                    for (String SANmove : operands) {
                        // parse SAN move
                    }
                    break;
                    
                case "rc":
                    epd.rc = Integer.parseInt(operands.get(0));                    
                    break;
                    
                case "resign":
                    epd.resign = true;
                    break;
                    
                case "sm":
                    // prase exactly one SAN move
                    break;
                    
                case "tcgs":
                    epd.tcgs = Integer.parseInt(operands.get(0));
                    break;
                    
                case "tcri":
                    epd.tcri[0] = operands.get(0);
                    epd.tcri[1] = operands.get(1);
                    break;
                    
                case "tcsi":
                    epd.tcsi[0] = operands.get(0);
                    epd.tcsi[1] = operands.get(1);
                    break;
                
                case "v0": case "v1": case "v2": case "v3": case "v4": case "v5": case "v6": case "v7": case "v8": case "v9":
                    n = opcode.charAt(1) - '0';
                    // First, all comment string registers with an index equal to or greater than N are set to null. (This is the set "cN" though "c9".) 
                    for (int k = n; k <= 9; k++) {
                        epd.v[k] = "";
                    }
                    // Second, and only if a string operand is present, the value of the corresponding comment string register is set equal to the string operand.
                    epd.v[n] = operands.size() > 0 ? operands.get(0) : "";
                    break;

                default:
                    logger.info("Unknown opcode: " + opcode);
                    break;
            }
            operands.clear();
            
        }
        return epd;
    }
}