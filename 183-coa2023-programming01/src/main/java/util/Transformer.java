package util;


import java.util.HashMap;

public class Transformer {
	public static String bag (long m){
		StringBuilder ans = new StringBuilder();
		while (m > 0){
			ans.append(m%2);
			m /= 2;
		}
		ans.reverse();
		return ans.toString();
	}

	public static String intToBinary(String numStr) {
		long m = Long.parseLong(numStr);
		if (m < 0){
			m += (1L << 32);
		}
		String s = bag(m);
		return String.format("%32s",s).replaceAll(" ","0");
//		int m = Integer.parseInt(numStr);
//		if (m == -2147483648) {
//			return "10000000000000000000000000000000";
//		}
//		String s = "";
//		int zheng = m > 0 ? m : -m;
//		while (zheng != 0) {
//			s += zheng % 2;
//			zheng /= 2;
//		}
//		for (int i = s.length(); i < 31; i++) {
//			s += '0';
//		}
//		if (m < 0) {
//			s += '1';
//		}
//		else {
//			s += '0';
//		}
//		char ree[] = s.toCharArray();
//		char ans[] = new char[32];
//		ans[0] = ree[31];
//		for (int i = ree.length - 2; i >= 0; i--) {
//			ans[ree.length - 1 - i] = (m >= 0) ? (ree[i]) : ver(ree[i]);
//		}
//		if (m >= 0) {
//			return String.valueOf(ans);
//		}
//		for (int i = 31; i >= 0; i--) {
//			int temp = toInt(ans[i]);
//			if (temp == 0) {
//				ans[i] = '1';
//				break;
//			}
//			else {
//				ans[i] = '0';
//			}
//		}
//		return String.valueOf(ans);
	}

	public static String binaryToInt(String binStr) {
		int sum = 0;
		for (int i = 31; i > 0; i--) {
			if (binStr.charAt(i) != '0') {
				sum += 1 << (31 - i);
			}
		}
		if (binStr.charAt(0) == '1') {
			sum -= 1 << 31;
		}
		return String.valueOf(sum);
	}

	public static String decimalToNBCD(String decimalStr) {
		HashMap<Integer, String> map = new HashMap<>();
		map.put(0, "0000");
		map.put(1, "0001");
		map.put(2, "0010");
		map.put(3, "0011");
		map.put(4, "0100");
		map.put(5, "0101");
		map.put(6, "0110");
		map.put(7, "0111");
		map.put(8, "1000");
		map.put(9, "1001");
		int wei = (decimalStr.charAt(0) == '-') ? decimalStr.length() - 1 : decimalStr.length();
		String ans = "";
		ans += (decimalStr.charAt(0) == '-') ? "1101" : "1100";
		for (int i = 0; i < 7 - wei; i++) {
			ans += "0000";
		}
		for (int i = (decimalStr.charAt(0) == '-') ? 1 : 0; i < decimalStr.length(); i++) {
			ans += map.get(toInt(decimalStr.charAt(i)));
		}
		return ans;
	}

	public static String NBCDToDecimal(String NBCDStr) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("0000", 0);
		map.put("0001", 1);
		map.put("0010", 2);
		map.put("0011", 3);
		map.put("0100", 4);
		map.put("0101", 5);
		map.put("0110", 6);
		map.put("0111", 7);
		map.put("1000", 8);
		map.put("1001", 9);
		String ans = "";
		ans += (NBCDStr.charAt(3) == '0') ? "" : "-";
		int count = 0;
		for (int i = 4; i < NBCDStr.length(); i += 4) {
			if (NBCDStr.startsWith("0000", i)) {
				continue;
			}
			else {
				count = i;
				break;
			}
		}
		if (count == 0) {
			return "0";
		}
		for (int i = count; i < NBCDStr.length(); i += 4) {
			ans += map.get(NBCDStr.substring(i, i + 4));
		}
		return ans;
	}

	public static String floatToBinary(String floatStr) {
		String ans = "";
		double num = Double.parseDouble(floatStr);
		if (num > Float.MAX_VALUE) {
			return "+Inf";
		}
		if (num < -Float.MAX_VALUE) {
			return "-Inf";
		}
		float tar = (float) num;
		ans += (floatStr.charAt(0) == '-') ? 1 : 0;
		if (tar == 0) {
			return ans + "0000000000000000000000000000000";
		}
		tar = (tar < 0) ? (-tar) : tar;
		int ex = 0;
		while (tar >= 2.0) {
			ex += 1;
			tar /= 2.0;
		}
		while (tar < 1.0) {
			ex -= 1;
			tar *= 2.0;
		}
		//非规格化
		if (ex <= -127) {
			tar /= (float) Math.pow(2.0, -126 - ex);
			ans += "00000000";
		}
		//规格化
		else {
			int E = ex + 127;
			String jiema = "";
			while (E != 0) {
				jiema += E % 2;
				E /= 2;
			}
			char temp[] = new char[8];
			for (int i = 0; i < jiema.length(); i++) {
				temp[7 - i] = jiema.charAt(i);
			}
			for (int i = 0; i < 8 - jiema.length(); i++) {
				temp[i] = '0';
			}
			ans += String.valueOf(temp);
		}
		String wei = "";
		int count = 0;
		tar -= (int) tar;
		while (tar != 1) {
			tar *= 2;
			wei += (tar >= 1) ? 1 : 0;
			tar -= (tar == 1) ? 0 : (int) tar;
			count++;
			if (count == 23) {
				break;
			}
		}
		for (int i = wei.length(); i < 23; i++) {
			wei += 0;
		}
		ans += wei;
		return ans;
	}

	public static String binaryToFloat(String binStr) {
		String a = binStr.substring(1, 9);
		int ex = Integer.parseInt(a, 2) - 127;
		int sign = (binStr.charAt(0) == '0') ? 0 : 1;
		String tailstr = binStr.substring(9);
		int tailCount = -1;
		float tailNum = 0;
		for (int i = 0; i < tailstr.length(); i++) {
			tailNum += (float) (Math.pow(2, tailCount) * (tailstr.charAt(i) - '0'));
			tailCount--;
		}
		float ans;
		if (ex == 128) {
			if (tailNum == 0) {
				return (sign == 0) ? "Inf" : "-Inf";
			}
			else {
				return "NaN";
			}

		}
		else if (ex == -127) {
			ans = (float) (Math.pow(2, -126) * tailNum);
		}
		else {
			tailNum += 1;
			ans = (float) (Math.pow(2, ex) * tailNum);
		}
		return (sign == 0) ? String.valueOf(ans) : "-" + ans;
	}

	public static char ver(char a) {
		if (a == '0') {
			return '1';
		}
		return '0';
	}

	public static int toInt(char a) {
		return a - '0';
	}

	public static void main(String[] args) {
//		System.out.println(binaryToInt("01001100000000000000000000001000"));
//		System.out.println(binaryToInt("11100000000000001100000000000100"));
//		System.out.println(binaryToInt("01101011111111110100000000000100"));
		System.out.println(intToBinary("-6"));
		System.out.println(intToBinary("-7"));
		System.out.println(intToBinary("42"));
//		System.out.println(1275068424+536821756);
	}
}
