// 
// baseConverter.java
// baseConverter
// 
// Noah Hitz 2025
// 

import java.math.BigInteger;
import java.util.Scanner;
import java.util.Arrays;

class baseConverter {
    static String helpText = """
            \u001B[1;97mBase Converter\u001B[0m - Convert between decimal, binary and hexadecimal

            \u001B[1;97mUsage:\u001B[0m
            \u001B[94mjava baseConverter [options] <number>\u001B[0m

            \u001B[1;97mBase Prefixes:\u001B[0m
            \u001B[94m  <value>\u001B[0m           \u001B[37mDecimal (no prefix)\u001B[0m
            \u001B[94m0b<value>\u001B[0m           \u001B[37mBinary\u001B[0m
            \u001B[94m0x<value>\u001B[0m           \u001B[37mHexadecimal\u001B[0m

            \u001B[1;97mOptions:\u001B[0m
            \u001B[94m--signed\u001B[0m            \u001B[37mInterpret input as signed (two's complement)\u001B[0m
            \u001B[94m--padding\u001B[0m           \u001B[37mAdd leading zeroes to binary output\u001B[0m
            \u001B[94m--prefixed\u001B[0m          \u001B[37mInclude base prefixes in output\u001B[0m
            \u001B[94m--grouping\u001B[0m          \u001B[37mGroup digits for readability\u001B[0m
            \u001B[94m--bingroup=<value>\u001B[0m  \u001B[37mSet number of binary digits to group together\u001B[0m
            \u001B[94m--hexgroup=<value>\u001B[0m  \u001B[37mSet number of hexadecimal digits to group together\u001B[0m
            \u001B[94m--no-decoration\u001B[0m     \u001B[37mDisable decorative output formatting\u001B[0m

            \u001B[1;97mHelp Options:\u001B[0m
            \u001B[94m-h, --help\u001B[0m          \u001B[37mShow this help message\u001B[0m

            \u001B[1;97mExample:\u001B[0m
            \u001B[94mjava baseConverter --padding --grouping --signed 0xFFAB7100F\u001B[0m

            \u001B[37m  in:   0xFFAB7100F
            -----------------------------------------------------
             dec:   -88'666'097
             bin:   1111 1111 1010 1011 0111 0001 0000 0000 1111
             hex:   F FAB7 100F
            -----------------------------------------------------\u001B[0m
    """;

    static boolean isInteractive = System.console() != null;

    static boolean prefixed = false;
    static boolean signed = false;
    static boolean padding = false;
    static boolean grouping = false;
    static boolean negative = false;
    static boolean decoration = true;

    static int decgroup = 3;
    static int bingroup = 4;
    static int hexgroup = 4;

    static InputType inType = InputType.DEC;

    enum InputType {
        DEC, BIN, HEX
    }

    public static void main(String[] args) {
        if(args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.print(helpText);
            return;
        }

        int inIdx = parseOptions(args);
        String originalInput = parseInput(args, inIdx);

        if(originalInput.length() != 0) {
            setInputType(originalInput);
            String input = cleanString(originalInput);
        
            String[] values = convert(input);
            output(values, originalInput);
        } else {
            interactiveMode();
        }
    }


    public static int parseOptions(String[] args) {
        int nIndex = args.length;
        for(int i = 0; i < args.length; i++) {
            if(args[i].length() > 2 && args[i].substring(0, 2).equals("--")) {
                String argument = (args[i].contains("=")) ? args[i].substring(0, args[i].indexOf("=")) : args[i];
                switch(argument) {
                    case "--signed": signed = true; break;
                    case "--padding": padding = true; break;
                    case "--prefixed": prefixed = true; break;
                    case "--grouping": grouping = true; break;
                    case "--no-decoration": decoration = false; break;
                    case "--bingroup": 
                                            if(args[i].length() <= 11) {
                                                error("Malformed argument '" + args[i] + "'");
                                                continue;
                                            }
                                            bingroup = Integer.valueOf(args[i].substring(11, args[i].length())); 
                                            break;
                    case "--hexgroup": 
                                            if(args[i].length() <= 11) {
                                                error("Malformed argument '" + args[i] + "'");
                                                continue;
                                            }                                            
                                            hexgroup = Integer.valueOf(args[i].substring(11, args[i].length())); 
                                            break;
                    default: 
                                            error("Unknown option '" + args[i] + "'");
                }
            } else {
                nIndex = i;
                break;
            }
        }

        return nIndex;
    }

    public static String parseInput(String[] args, int index) {
        StringBuilder inputNumber = new StringBuilder();
        for(int i = index; i < args.length; i++) 
            inputNumber.append(args[i]);
        return inputNumber.toString();
    }

    public static void interactiveMode() {
        Scanner scanner = new Scanner(System.in);

        while(true) {
            if(isInteractive)
                System.out.print("Enter number to convert: ");
            String originalInput = scanner.nextLine();

            if(originalInput.equals("exit") || originalInput.equals("quit") || originalInput.equals("q"))
                break;

            if(originalInput.isEmpty())
                continue;

            setInputType(originalInput);
            String input = cleanString(originalInput);

            String[] values = convert(input);
            output(values, input);
        }

        scanner.close();
    }

    public static void setInputType(String input) {
        // reset for interactive mode
        negative = false;
        inType = InputType.DEC;
                
        if(input.length() > 2) {
            switch(input.substring(0,2)) {
                case "0b": inType = InputType.BIN; break;
                case "0B": inType = InputType.BIN; break;
                case "0x": inType = InputType.HEX; break;
                case "0X": inType = InputType.HEX; break;
            }
        }

        if(inType == InputType.DEC && input.charAt(0) == '-') 
            negative = true;
    }

    public static String[] convert(String input) {
        String[] values = new String[3];

        switch(inType) {
            case DEC:
                values[0] = input;
                values[1] = "0" + decimalToBinary(input);
                values[2] = binaryToHex(values[1]);
                break;
            case BIN: 
                values[0] = binaryToDecimal(input);
                values[1] = input;
                values[2] = binaryToHex(input);
                break;
            case HEX:
                values[0] = hexToDecimal(input);
                values[1] = hexToBinary(input);
                values[2] = input;
                break;
        }

        if(signed && (inType == InputType.BIN ||  inType == InputType.HEX)) {
            negative = (values[1].charAt(0) == '1'); 
            if(negative)
                values[0] = binaryToDecimal(binaryTwosComplement(values[1]));
        }

        if(signed && inType == InputType.DEC && negative) {
            values[1] = binaryTwosComplement(values[1]);
            values[2] = binaryToHex(values[1]);
        } 

        if(padding) {
            values[1] = binaryPadding(values[1]);
        }

        if(grouping) {
            values[0] = group(values[0], InputType.DEC);
            values[1] = group(values[1], InputType.BIN);
            values[2] = group(values[2], InputType.HEX);
        }

        if(prefixed) {
            values[1] = "0b" + values[1];
            values[2] = "0x" + values[2];
        }

        if(signed && negative) {
            values[0] = "-" + values[0];
        }

        return values;
    }

    public static void output(String[] values, String originalInput) {
        int maxLength = Math.max(Arrays.stream(values)
                .mapToInt(String::length)
                .max()
                .orElse(0), originalInput.length());
        
        if(decoration) {
            System.out.println("\t" + "  in: \t" + originalInput);
            System.out.println("\t" + repeatedChar('-', 8 + maxLength + 1));
        }
        System.out.println(((decoration) ? "\t" : "") + " dec: \t" + values[0]);
        System.out.println(((decoration) ? "\t" : "") + " bin: \t" + values[1]);
        System.out.println(((decoration) ? "\t" : "") + " hex: \t" + values[2]);

        if(decoration)
            System.out.println("\t" + repeatedChar('-', 8 + maxLength + 1));
    }

    //
    // Conversion Functions
    //
    public static String decimalToHex(String in) {
        return binaryToHex(decimalToBinary(in));
    }

    public static String hexToDecimal(String in) {
        if(in.length() > 2 && in.substring(0,3).equals("0x"))
            in = in.substring(2);

        return binaryToDecimal(hexToBinary(in));
    }

    public static String binaryToDecimal(String in) {
        int length = in.length();
        BigInteger result = BigInteger.ZERO;

        for(int i = length-1; i >= 0; i--) {
            BigInteger t = BigInteger.ONE;
            BigInteger digit = BigInteger.valueOf(in.charAt(i) - '0');
            t = t.shiftLeft((length-1) - i);
            t = t.multiply(digit);
            result = result.add(t);
        }

        return result.toString();
    }

    public static String decimalToBinary(String in) {
        StringBuilder binary = new StringBuilder(); 

        BigInteger n = parseDecString(in);
        BigInteger remainder = n;

        if(n.equals(BigInteger.ZERO))
            binary.append("0");

        while(!remainder.equals(BigInteger.ZERO)) {
            if(remainder.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) 
                binary.insert(0, "0");
            else 
                binary.insert(0,"1");
            remainder = remainder.shiftRight(1);
        } 

        return binary.toString();
    }

    public static String hexToBinary(String in) {
        if(in.length() > 2 && in.substring(0,3).equals("0x"))
            in = in.substring(2);

        StringBuilder binary = new StringBuilder();
        for(int i = 0; i < in.length(); i++) {
            binary.append(hexDigitToBinary(in.charAt(i)));
        }

        return binary.toString();
    }

    public static String binaryToHex(String in) {
        StringBuilder hex = new StringBuilder();
        in = binaryPadding(in);

        int i = 0;
        while(i <= in.length()-4) {
            hex.append(binaryNibbleToHex(in.substring(i, i+4)));
            i += 4;
        }

        return hex.toString();
    }

    //
    // Helper Functions
    //
    public static String repeatedChar(char c, int repeats) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < repeats; i++) 
            result.append(c);
        return result.toString();
    }

    public static String group(String in, InputType type) {
        int groupings = 0;
        char delim = '\0';
        switch(type) {
            case DEC: groupings = decgroup; delim = '\''; break;
            case BIN: groupings = bingroup; delim = ' '; break;
            case HEX: groupings = hexgroup; delim = ' '; break;
        }

        // Useful to deactivate grouping specifically for hex or binary
        if(groupings == 0)
            return in;

        StringBuilder result = new StringBuilder();
        int idx = 0;
        for(int i = in.length()-1; i >= 0; i--) {
            if(idx%groupings == 0 && idx != 0) 
                result.insert(0, delim);
            result.insert(0, in.charAt(i));
            idx++;
        }

        return result.toString();
    }

    public static BigInteger parseDecString(String in) {
        int length = in.length();
        BigInteger result = BigInteger.ZERO;

        for(int i = 0; i < length; i++) {
            BigInteger digit = BigInteger.valueOf(in.charAt(i)-'0');
            BigInteger t = BigInteger.valueOf(10); 
            t = t.pow((length-1) - i);
            t = t.multiply(digit);
            result = result.add(t);
        }

        return result;
    }

    public static String binaryPadding(String in) {
        if(in.length()%4 != 0) {
            StringBuilder padding = new StringBuilder();
            for(int i = 0; i < 4-(in.length()%4); i++) 
                padding.append((signed) ? in.charAt(0) : '0');
            in = padding.toString() + in;
        }
        return in;
    }

    public static String binaryTwosComplement(String in) {
        StringBuilder flipped = new StringBuilder();
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < in.length(); i++) 
            flipped.append((in.charAt(i) == '1') ? '0' : '1');

        int remainder = 1;
        for(int i = flipped.length()-1; i >= 0; i--) {
            char c = (in.charAt(i) == '1') ? '0' : '1';

            if(remainder > 0 && c == '1') {
                remainder = 1;
                c = '0';
            } else if(remainder > 0 && c == '0') {
                remainder = 0;
                c = '1';
            }

            result.insert(0, c);
        }

        return result.toString();
    }

    public static String cleanString(String in) {
        StringBuilder result = new StringBuilder();
        if(in.length() > 2) {
            boolean strip = false;
            switch(in.substring(0,2)) {
                case "0b": strip = true; break;
                case "0B": strip = true; break;
                case "0x": strip = true; break;
                case "0X": strip = true; break;
            }

            if(strip)
                in = in.substring(2, in.length());
        }

        for(int i = 0; i < in.length(); i++) {
            switch(in.charAt(i)) {
                case ' ': continue;
                case '-': continue;
                case '\'': continue;
                case '_': continue;
            }

            result.append(in.charAt(i));
        }

        return result.toString();
    }

    public static String hexDigitToBinary(char hex) {
        switch(hex) {
            case '0': return "0000";
            case '1': return "0001";
            case '2': return "0010";
            case '3': return "0011";
            case '4': return "0100";
            case '5': return "0101";
            case '6': return "0110";
            case '7': return "0111";
            case '8': return "1000";
            case '9': return "1001";
            case 'A': return "1010";
            case 'B': return "1011";
            case 'C': return "1100";
            case 'D': return "1101";
            case 'E': return "1110";
            case 'F': return "1111";
            case 'a': return "1010";
            case 'b': return "1011";
            case 'c': return "1100";
            case 'd': return "1101";
            case 'e': return "1110";
            case 'f': return "1111";
        }

        error("Invalid hexadecimal digit '" + hex + "'");
        return "";
    }

    public static char binaryNibbleToHex(String nibble) {
        switch(nibble) {
            case "0000": return '0';
            case "0001": return '1';
            case "0010": return '2';
            case "0011": return '3';
            case "0100": return '4';
            case "0101": return '5';
            case "0110": return '6';
            case "0111": return '7';
            case "1000": return '8';
            case "1001": return '9';
            case "1010": return 'A';
            case "1011": return 'B';
            case "1100": return 'C';
            case "1101": return 'D';
            case "1110": return 'E';
            case "1111": return 'F';
        }

        error("Invalid nibble '" + nibble + "'");
        return ' ';
    }

    public static void error(String msg) {
        System.err.println("[baseConverter] \u001B[31m Error:\u001B[37m " + msg + "");
    }

}
