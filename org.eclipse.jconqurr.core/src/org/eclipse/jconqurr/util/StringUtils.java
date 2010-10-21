package org.eclipse.jconqurr.util;

/**
 * This class is used for utility functions related to String operations. This needs to be thoroughly tested
 * and may contain bugs. If any bugs are found, please report to
 * {@link http://jconqurr.googlecode.com}
 * @author lasantha
 *
 */
public class StringUtils {
	private static int currentIndex = -1;

	/**
	 * Converts the given string to proper case and returns the converted string.The original string
	 * is not modified in anyway. 
	 * @param str
	 * 		the string to be converted
	 * @return
	 * 		the converted string formatted to be in proper case
	 */
	public static String toProperCase(String str) {
		int startIndex = 0, endIndex = 0;
		String strReturn = "";
		currentIndex = 0;
		do {
			startIndex = currentIndex;
			endIndex = getIndexOfNextWord(str);
			if(endIndex < 0) {
				endIndex = str.length();
			}
			strReturn = strReturn.concat(str.substring(startIndex, startIndex+1).toUpperCase()
					+ str.substring(startIndex+1,endIndex).toLowerCase());

		} while (currentIndex != -1);
		return strReturn;
	}

	private static int getIndexOfNextWord(String str) {
		String subStr = str.substring(currentIndex);
		currentIndex = -1;
		for (int i = 0; i < subStr.length(); i++) {
			if (subStr.charAt(i) == ' ') {
				currentIndex = i + 1;
				break;
			} else {
				String currentChar = subStr.substring(i, i + 1);
				if (currentChar.toUpperCase().equals(currentChar)) {
					currentIndex = i;
					break;
				}
			}
		}

		return currentIndex;
	}
}
