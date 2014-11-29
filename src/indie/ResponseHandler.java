package indie;

public class ResponseHandler {

	public static boolean isEndOfFile(String s) {
		if (s.length() < 5)
			return false;

		if (s.substring(s.length() - 5, s.length()).equals("~!@#$"))
			return true;
		return false;
	}

	public static String removeEndFileDelimiter(String s) {
		if (s.length() == 5)
			return "";

		return s.substring(0, s.length() - 5);
	}

	public static boolean isStartOfFile(String s) {
		if (s.split("###").length >= 2)
			return true;
		return false;
	}

	public static String getFirstLineContent(String s) {
		String[] tokens = s.split("###");
		if (tokens.length == 2)
			return "";
		else
			return tokens[2];
	}
}
