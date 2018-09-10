package assembulator;

import gui.DialogFactory;
import instructions.BadInstructionException;
import parsing.Parser;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Simple parser that converts MIPS code into machine code using the ISA
 * provided in instruction_codes.txt
 * 
 * @author pf51, ghb5
 **/
public class Assembulator implements Assembler{

    private static final String ADDR_RADIX = "ADDRESS_RADIX = DEC;";
	private static final String DATA_RADIX = "DATA_RADIX = BIN;";

	private static final String DEPTH_FORMAT = "DEPTH = %d;";
	private static final String WIDTH_FORMAT = "WIDTH = %d;";

	private static final int DEPTH = 4096;
	private static final int WIDTH = 32;

	private static final int NOP_PAD = 1;
    public static final List<String> NOOP = List.of("noop", "noop", "noop", "noop");

    private List<String> rawAssembly = new ArrayList<>();
	private Map<String, Integer> jumpTargets = new HashMap<>();
	
	private String filename;

	public Assembulator(){};

    /**
     *  @Deprecated Each call to writeTo should specify the target to read from.
    */
	public Assembulator(String filename) {
		this.filename = filename.substring(filename.lastIndexOf(File.separator)+1);
		loadFile(filename);
	}

	@Override
	public void writeTo(InputStream is, OutputStream os, boolean padding) throws BadInstructionException{
        loadFile(is, padding);
		List<String> filteredCode = filterCode(rawAssembly);
		List<String> parsedCode = parseCode(filteredCode);		
		writeCode(new PrintStream(os), filteredCode, parsedCode);
	}

	private void loadFile(InputStream is, boolean shouldPad){
	   String line;
	   BufferedReader r = new BufferedReader(new InputStreamReader(is));
	   try{
	       while((line = r.readLine()) != null){
               rawAssembly.add(line);
               if(shouldPad)
                   rawAssembly.addAll(NOOP);
           }
           r.close();
       } catch (IOException e){
           DialogFactory.showError(e);
       }

    }
	
	private void loadFile(String filename) {
		File file = new File(filename);		
		
		Scanner codeScan;
		try {
			codeScan = new Scanner(file);
			while (codeScan.hasNextLine()) {
				rawAssembly.add(codeScan.nextLine());
			}
			codeScan.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error - File not found!: " + filename);
			System.exit(1);
		}
	}

	/**
	 * Takes raw assembly file line by line and filters out to instructions
	 * 
	 * @param rawAssembly code list
	 * @return filtered assembly code
	 */
	private List<String> filterCode(List<String> rawAssembly) {

		Predicate<String> EmptyAndCommentOnlyLines = s -> {
			// Ignore comments and then split by comma/spaces
			String[] split = s.split("\\#")[0].split("[,\\s]+");
			boolean atLeastOneCommand = split.length > 0 && !split[0].isEmpty();
			return atLeastOneCommand;
		};

		Function<String, String> addNopToEmptyTargetLines = s -> {
			boolean hasColon = s.contains(":");
			boolean canSplitByColon = s.split(":\\s+").length == 2;
			if (!hasColon || canSplitByColon) {
				return s;
			}
			return s + " nop";
		};

		Function<String, String> removeSemicolons = s-> s.split(";")[0];
		
		List<String> filteredCode = rawAssembly.stream().map(addNopToEmptyTargetLines)
														.filter(EmptyAndCommentOnlyLines)
                                                        .map(removeSemicolons)
														.collect(Collectors.toList());

		trimTargetsFromCode(filteredCode);

		return filteredCode;
	}
	
	private void trimTargetsFromCode(List<String> code) {
		for (int i = 0; i < code.size(); i++) {
			String line = code.get(i);
			if (line.contains(":")) {
				String[] splitLine 	= line.split(":\\s+"); // Split by colon and spaces
				String command 		= splitLine[1];
				String target 		= splitLine[0];

				// trim target tag from command
				// store address of target tag
				code.set(i, command);
				jumpTargets.put(target, i);
			}
		}
	}
	
	/**
	 * Takes filtered code and converts it to assembly, as well as 
	 * replaces branch targets with address lines.
	 * 
	 * @param filteredCode
	 * @return parsed code
	 */
	private List<String> parseCode(List<String> filteredCode) throws BadInstructionException {
		Function<String, String> targetReplacer = s -> {
			if (!s.matches("\\d+[a-zA-z_-]+")) {
				return s;
			}
			String encoding = s.replaceAll("[a-zA-Z_-]", "");
			int address = jumpTargets.get(s.replaceAll("\\d", ""));
			return encoding + Parser.toBinary(NOP_PAD*address, 27);
		};

		return filteredCode.parallelStream().map(Parser::parseLine)
											.map(targetReplacer)
											.collect(Collectors.toList());
	}

	/**
	 * Writes code to stream
	 * 
	 * @param filteredCode
	 * @param parsedCode
	 */
	private void writeCode(PrintStream ps, List<String> filteredCode, List<String> parsedCode) {
		writeHeader(ps);
		writeContent(ps, filteredCode, parsedCode);
	}

	private void writeHeader(PrintStream ps) {
		// Print Header
		String n = System.lineSeparator();
		String FILE_FORMAT = "-- %s";
		ps.printf(FILE_FORMAT + n, filename);
		ps.printf(DEPTH_FORMAT + n, DEPTH);
		ps.printf(WIDTH_FORMAT + n, WIDTH);
		ps.println();

		ps.println(ADDR_RADIX);
		ps.println(DATA_RADIX);
		ps.println();
	}
	
	private void writeContent(PrintStream ps, List<String> filtCode, List<String> parseCode) {
		String n = System.lineSeparator();

		ps.println("CONTENT");
		ps.println("BEGIN");
		
		Map<Integer, String> reverseTargets = new HashMap<>();
		jumpTargets.forEach((s, i) -> reverseTargets.put(i, s));

		for (int i = 0; i < filtCode.size(); i++) {
			String rawLine = filtCode.get(i);

			if (!reverseTargets.containsKey(i)) {
				ps.printf("%4s-- %s%s", "", rawLine, n);
			} else {
				String target = reverseTargets.get(i);
				ps.printf("%4s-- %s: %s%s", "", target, rawLine, n);
			}

			int address = NOP_PAD*i;
			String instrCode = parseCode.get(i);
			ps.printf("%04d : %s;%s", address, instrCode, n);
		}
		
		ps.printf("[%04d .. %4d] : %032d;%s", filtCode.size(), DEPTH-1, 0, n);
		ps.println("END;");
	}
}