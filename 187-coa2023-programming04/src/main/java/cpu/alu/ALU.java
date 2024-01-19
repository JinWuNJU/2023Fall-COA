package cpu.alu;

import cpu.fpu.FPU;
import util.DataType;
import util.Transformer;

public class ALU {


	public StringBuilder addOne(StringBuilder m){
		for (int i = 7; i >= 0; i--) {
			int temp = toInt(m.charAt(i));
			if (temp == 0) {
				m.replace(i,i+1,"1");
				break;
			}
			else {
				m.replace(i,i+1,"0");
			}
		}
		return m;
	}

	public int getInt(String a){
		int sum =0;
		for (int i = 0; i < 8; i++) {
			sum += (a.charAt(i) == '1') ? 1 << (7 - i) : 0;
		}
		return sum;
	}
	public int subE(String a, String b){
		int res =0;
		for (int i = 0; i < 8; i++) {
			int exp = a.charAt(i)-b.charAt(i);
			if(exp == 0){
				continue;
			}
			res += (exp >= 0) ? (1 << (7 - i)) : (- (1 << (7 - i)));
		}
		return res;
	}
	public  String intToBinary(String numStr) {
		int m = Integer.parseInt(numStr);
		if (m == -2147483648) {
			return "10000000000000000000000000000000";
		}
		String s = "";
		int zheng = m > 0 ? m : -m;
		while (zheng != 0) {
			s += zheng % 2;
			zheng /= 2;
		}
		for (int i = s.length(); i < 31; i++) {
			s += '0';
		}
		if (m < 0) {
			s += '1';
		}
		else {
			s += '0';
		}
		char ree[] = s.toCharArray();
		char ans[] = new char[32];
		ans[0] = ree[31];
		for (int i = ree.length - 2; i >= 0; i--) {
			ans[ree.length - 1 - i] = (m >= 0) ? (ree[i]) : ver(ree[i]);
		}
		if (m >= 0) {
			return String.valueOf(ans);
		}
		for (int i = 31; i >= 0; i--) {
			int temp = toInt(ans[i]);
			if (temp == 0) {
				ans[i] = '1';
				break;
			}
			else {
				ans[i] = '0';
			}
		}
		return String.valueOf(ans);
	}
	public char ver(char a) {
		if (a == '0') {
			return '1';
		}
		return '0';
	}

	public DataType add(DataType src, DataType dest) {
		String s1 = src.toString();
		String s2 = dest.toString();
		// carry 为进位
		int carry = 0;
		String ans_reverse = "";
		for (int i = s1.length()-1; i >=0 ; i--) {
			int temp1 = toInt(s1.charAt(i));
			int temp2 = toInt(s2.charAt(i));
			// res 为该位的值，由被加数，加数，进位三者做异或运算得
			int res = temp1 ^ temp2 ^ carry;
			// 更新carry的值
			carry = (temp1 & temp2) | (temp2 & carry) | (temp1 & carry);
			ans_reverse += res;
		}
		String res = "";
		for (int i = ans_reverse.length()-1; i >= 0; i--){
			res += ans_reverse.charAt(i);
		}
		DataType m = new DataType(res);
		return m;
	}
	public String getOppo(String dived) {
		String s2_re = "";
		int carry = 1;
		String temp = "";
		for (int i = 0; i < dived.length(); i++) {
			s2_re += (dived.charAt(i) == '0') ? 1 : 0;
		}
		for (int i = dived.length() - 1; i >= 0; i--) {
			int temp2 = toInt(s2_re.charAt(i));
			// res 为该位的值，由被加数，加数，进位三者做异或运算得
			int res = 0 ^ temp2 ^ carry;
			// 更新carry的值
			carry = (0 & temp2) | (temp2 & carry) | (0 & carry);
			temp += res;
		}
		String temp_re = "";
		for (int i = temp.length() - 1; i >= 0; i--) {
			temp_re += temp.charAt(i);
		}
		return temp_re;
	}


	public DataType div(DataType src, DataType dest) {
		String dived = dest.toString();// 被除数
		String divisor = src.toString();// 除数
		DataType remainderReg;
		if (dived.equals("00000000000000000000000000000000") &&
				!(divisor.equals("00000000000000000000000000000000"))) {
			remainderReg = new DataType("00000000000000000000000000000000");
			return new DataType("00000000000000000000000000000000");
		}
		else if (divisor.equals("00000000000000000000000000000000")) {
			throw new ArithmeticException();
		}
		String reminder = expend(dived);
		for (int i = 0; i < dived.length(); i++) {
			String[] s = leftMove(reminder, dived);
			reminder = s[0];
			dived = s[1];
			String[] s2 = tempDiv(reminder, divisor, dived);
			reminder = s2[0];
			dived = s2[1];
		}
		if (src.toString().charAt(0) != dest.toString().charAt(0)) {
			dived = getOppo(dived);
		}
		//  对bug的处理
		if (reminder.equals(divisor)) {
			DataType temp = add(new DataType(dived), new DataType(Transformer.intToBinary("1")));
			dived = temp.toString();
			reminder = "00000000000000000000000000000000";
		}
		else if (reminder.equals(getOppo(divisor))) {
			DataType temp = sub(new DataType(Transformer.intToBinary("1")), new DataType(dived));
			dived = temp.toString();
			reminder = "00000000000000000000000000000000";
		}

		remainderReg = new DataType(reminder);
		return new DataType(dived);
	}

	public String[] leftMove(String reminder, String quot) {
		String[] ans = new String[2];
		ans[0] = new String(reminder.substring(1) + quot.charAt(0));
		ans[1] = quot.substring(1);
		return ans;
	}

	public String[] tempDiv(String reminder, String divisor, String quot) {
		String[] ans = new String[2];
		boolean isPlus = reminder.charAt(0) != divisor.charAt(0);
		DataType add1 = new DataType(reminder);
		DataType add2 = new DataType(divisor);
		DataType add = (isPlus) ? add(add1, add2) : sub(add2, add1);
		if (add.toString().charAt(0) == reminder.charAt(0)) {
			//reminder够减
			ans[0] = add.toString();
			quot += '1';
			ans[1] = quot;
		}
		else {
			ans[0] = reminder;
			quot += '0';
			ans[1] = quot;
		}
		return ans;
	}

	public String expend(String m) {
		return (m.charAt(0) == '0') ? "00000000000000000000000000000000" :
				"11111111111111111111111111111111";
	}

	/**
	 * 返回两个二进制整数的差
	 * dest - src
	 *
	 * @param src  32-bits
	 * @param dest 32-bits
	 * @return 32-bits
	 * 
	 */
	public DataType sub(DataType src, DataType dest) {
		String s2 = src.toString();
		String s1 = dest.toString();
		int carry = 1;
		String temp = "";
		// step 01:
		// 首先s2取反再加1
		String s2_re = "";
		for (int i = 0; i < s2.length(); i++) {
			s2_re += (s2.charAt(i) == '0') ? 1 : 0;
		}
		for (int i = s2.length()-1; i >=0 ; i--) {
			int temp2 = toInt(s2_re.charAt(i));
			// res 为该位的值，由被加数，加数，进位三者做异或运算得
			int res = 0 ^ temp2 ^ carry;
			// 更新carry的值
			carry = (0 & temp2) | (temp2 & carry) | (0 & carry);
			temp += res;
		}
		String temp_re = "";
		for (int i = temp.length()-1; i >= 0; i--){
			temp_re += temp.charAt(i);
		}


		// step 02:
		carry = 0;
		String res_re = "";
		for (int i = s1.length()-1; i >= 0 ; i--) {
			int temp1 = toInt(s1.charAt(i));
			int temp2 = toInt(temp_re.charAt(i));
			int res = temp1 ^ temp2 ^ carry;
			carry = (temp1 & temp2) | (temp2 & carry) | (temp1 & carry);
			res_re += res;
		}
		String res = "";
		for (int i = res_re.length()-1; i >= 0; i--){
			res += res_re.charAt(i);
		}
		DataType m = new DataType(res);
		return m;
	}
	public int toInt(char a){
		return a-48;
	}


}
