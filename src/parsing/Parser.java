package parsing;


import instructions.Instruction;

public class Parser {

	private static final int MAX_SHAMT = (1 << 5) -1 ;
	
	public static String parseLine(String line) {
		
		// Ignore comments
		line = line.split("\\#")[0];
		
		// Split by commas
		String[] split_line = line.split("[,\\s]+");
		
		// Must have at least one command
		if(split_line.length < 1 || split_line[0].isEmpty()) {
			return "";
		}
		
		Instruction instr = Instruction.getByName(split_line[0]);
		
		switch (instr.type) {
		case R: 	return parseRType(split_line, instr);
		case I: 	return parseIType(split_line, instr);
		case JI: 	return parseJIType(split_line, instr);
		case JII: 	return parseJIIType(split_line, instr);
		case L:		return parseLType(split_line, instr);
		case NOOP: 	return toBinary(0, 32);
		default: 	return "";
		}
		
	}
	
	
	static String parseLType(String[] splitLine, Instruction instr) {
		if(instr == Instruction.WLI) {
			if (splitLine.length != 2) {
				return "BAD L: " + instr.name();
			}
			
			String opcode = instr.getOpcode();
			String filler = toBinary(0, 19);
			int ascii = 0;
			try {
				ascii = Integer.valueOf(splitLine[1]);
			} catch (NumberFormatException nfe) {
				if	(splitLine[1].length() == 1) {
					char ch = splitLine[1].charAt(0);
					ascii = ch;
				} else {
					return "BAD L: ascii " + nfe.getClass().getName();
				}
			}
			
			String asciiCode = toBinary(ascii, 8);
			
			return opcode + filler + asciiCode;
		}

		if (instr == Instruction.WP) {
			return instr.getOpcode() + toBinary(parseRegister(splitLine[1]), 5) + toBinary(0, 22);
		}
		
		return "BAD L: Not Supported";
	}
	
	static String parseJIIType(String[] splitLine, Instruction instr) {
		if (splitLine.length != 2) {
			return "BAD JII";
		}
		
		String opcode = instr.getOpcode();
		
		String rd = splitLine[1];
		String rdCode = toBinary(parseRegister(rd), 5);
		
		return opcode + rdCode + toBinary(0, 22);
	}
	
	static String parseJIType(String[] splitLine, Instruction instr) {
		String opcode = instr.getOpcode();
		
		if (splitLine.length != 2) {
			return "BAD JI";
		}
		
		String target = splitLine[1];
		String T;
		try {
			int tNum = Integer.valueOf(target);
			T = toBinary(tNum, 27);
		}catch (NumberFormatException nfe) {
			T = target;
		}
		
		return opcode + T;
	}
	
	static String parseIType(String[] splitLine, Instruction instr) {
		
		boolean memInstr = 
				   instr == Instruction.LW 
				|| instr == Instruction.SW 
				|| instr == Instruction.SV
				|| instr == Instruction.LP;

		if (memInstr && splitLine.length != 3 
		|| !memInstr && splitLine.length != 4) {
			return "BAD I";
		}
		
		String arg2;
		String rd, rs, N;
		rd = splitLine[1];
		arg2 = splitLine[2];
		
		if (memInstr) {
			String[] addressCombo = arg2.split("[\\(\\)]");
			N = addressCombo[0];
			rs = addressCombo[1];
		} else {
			rs = arg2;
			N = splitLine[3];
		}
		
		String opcode = instr.getOpcode();
		String rsCode = toBinary(parseRegister(rs), 5);
		String rdCode = toBinary(parseRegister(rd), 5);
		String immediate = parseImmediate(N);
		
		return opcode + rdCode + rsCode + immediate;
	}
	
	static String parseImmediate(String N) {
		int value;
		
		try {
			value = Integer.valueOf(N);
		} catch ( NumberFormatException nfe) {
			return "BAD IMMEDIATE";
		}
		
		
		
		return toBinary(value, 17);
	}
	
	/**
	 * Parses R type instructions
	 * 
	 * @param splitLine
	 * @param instr
	 * @return binary encoded R type instruction 
	 */
	static String parseRType(String[] splitLine, Instruction instr) {

		if(splitLine.length != 4) {
			return "BAD R";
		}
		
		String rdArg = splitLine[1];
		String arg0 = splitLine[2];
		String arg1 = splitLine[3];
		
		// Get opcode
		String opcode = instr.getOpcode();
		String rdCode = toBinary(parseRegister(rdArg), 5);
		String rsCode = toBinary(parseRegister(arg0), 5);
		String rtCode = "00000";
		String shamt  = "00000";
		String aluCode = instr.getALUopcode();
		
		boolean shiftInstr = instr == Instruction.SRA || instr == Instruction.SLL || instr == Instruction.SRL;
		if (!shiftInstr) {
			rtCode = toBinary(parseRegister(arg1), 5);
		} else {
			shamt = toBinary(parseShamt(arg1), 5);
		}
		
		return opcode + rdCode + rsCode + rtCode + shamt + aluCode + "00";
	}

	/**
	 * Parses Shamt string, returns 0 if bad
	 * 
	 * @param shamt
	 * @return
	 */
	static int parseShamt(String shamt) {
		int value;
		
		try {
			value = Integer.valueOf(shamt);
		} catch (NumberFormatException nfe) {
			return 0;
		}
				
		if (value < 0 || MAX_SHAMT < value ) {
			return 0;
		}
		
		return value;
	}

	/**
	 * parses register, returns 0 if you pick a bad register value
	 * 
	 * @param regCode
	 * @return
	 */
	static int parseRegister(String regCode) {

		if (!regCode.contains("$")) {
			return 0;
		}

		// Replace snow flake registers with equivalent
		if (regCode.equals("$rstatus")) {
			regCode = "30";
		} else if (regCode.equals("$ra")) {
			regCode = "31";
		}

		// Remove all non digit characters
		regCode = regCode.replaceAll("[\\$r]", "");

		Integer regNum;

		try {
			regNum = new Integer(regCode);
		} catch (NumberFormatException nfe) {
			return 0;
		}

		if (regNum < 0 || 32 <= regNum) {
			return 0;
		}

		return regNum;
	}
	
	public static String toBinary(int b, int d) {
		String output = "";

		for (int i = d - 1; i >= 0; i--) {
			int masked = b >> i;
			output += (masked & 1);
		}

		return output;
	}
}
