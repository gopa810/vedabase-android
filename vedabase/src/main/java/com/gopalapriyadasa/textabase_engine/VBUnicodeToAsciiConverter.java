package com.gopalapriyadasa.textabase_engine;

public class VBUnicodeToAsciiConverter {

	private static int unicode_to_oem_tbl[] = { 39, 39, 42, 42, 46, 46, 48, 48,
			49, 49, 50, 50, 51, 51, 52, 52, 53, 53, 54, 54, 55, 55, 56, 56, 57,
			57, 63, 63, 65, 97, 66, 98, 67, 99, 68, 100, 69, 101, 70, 102, 71,
			103, 72, 104, 73, 105, 74, 106, 75, 107, 76, 108, 77, 109, 78, 110,
			79, 111, 80, 112, 81, 113, 82, 114, 83, 115, 84, 116, 85, 117, 86,
			118, 87, 119, 88, 120, 89, 121, 90, 122, 97, 97, 98, 98, 99, 99,
			100, 100, 101, 101, 102, 102, 103, 103, 104, 104, 105, 105, 106,
			106, 107, 107, 108, 108, 109, 109, 110, 110, 111, 111, 112, 112,
			113, 113, 114, 114, 115, 115, 116, 116, 117, 117, 118, 118, 119,
			119, 120, 120, 121, 121, 122, 122, 192, 97, 193, 97, 194, 97, 195,
			97, 196, 97, 197, 97, 200, 101, 201, 101, 202, 101, 203, 101, 204,
			105, 205, 105, 206, 105, 207, 105, 208, 100, 209, 110, 210, 111,
			211, 111, 212, 111, 213, 111, 214, 111, 217, 117, 218, 117, 219,
			117, 220, 117, 221, 121, 224, 97, 225, 97, 226, 97, 227, 97, 228,
			97, 229, 97, 231, 99, 232, 101, 233, 101, 234, 101, 235, 101, 236,
			105, 237, 105, 238, 105, 239, 105, 241, 110, 242, 111, 243, 111,
			244, 111, 245, 111, 246, 111, 249, 117, 250, 117, 251, 117, 252,
			117, 253, 121, 256, 97, 257, 97, 298, 105, 299, 105, 305, 105, 346,
			115, 347, 115, 362, 117, 363, 117, 7692, 100, 7693, 100, 7716, 104,
			7717, 104, 7734, 108, 7735, 108, 7737, 108, 7744, 109, 7745, 109,
			7748, 110, 7749, 110, 7750, 110, 7751, 110, 7770, 114, 7771, 114,
			7772, 114, 7773, 114, 7778, 115, 7779, 115, 7788, 116, 7789, 116 };

	static int unicode_tbl_inits = 0;
	static int[] unicode_tbl_0_370 = new int[371];
	static int[] unicode_tbl_7690_7790 = new int[101];

	private static int codeAtIndex(int index) {
		return unicode_to_oem_tbl[index * 2];
	}

	private static int valueAtIndex(int index) {
		return unicode_to_oem_tbl[index * 2 + 1];
	}

	private static void initUnicodeTables() {
		int count_total = unicode_to_oem_tbl.length;
		int count_log = count_total / 2;
		for (int j = 0; j < 371; j++)
			unicode_tbl_0_370[j] = 32;
		for (int j = 0; j < 101; j++)
			unicode_tbl_7690_7790[j] = 32;
		for (int i = 0; i < count_log; i++) {
			int code = VBUnicodeToAsciiConverter.codeAtIndex(i);
			if (code >= 0 && code < 370)
				unicode_tbl_0_370[code] = VBUnicodeToAsciiConverter
						.valueAtIndex(i);
			else if (code >= 7690 && code < 7790)
				unicode_tbl_7690_7790[code - 7690] = VBUnicodeToAsciiConverter
						.valueAtIndex(i);
		}
		unicode_tbl_inits = 1;
	}

	public static char unicodeToAscii(char code) {
		if (unicode_tbl_inits == 0)
			VBUnicodeToAsciiConverter.initUnicodeTables();
		if (code >= 0 && code < 370)
			return (char) unicode_tbl_0_370[code];
		if (code >= 7690 && code < 7790)
			return (char) unicode_tbl_7690_7790[code - 7690];
		return 32;
	}

}
