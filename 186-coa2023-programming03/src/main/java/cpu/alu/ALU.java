package cpu.alu;

import util.DataType;
import util.Transformer;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {
	/**
	 * 返回两个二进制整数的乘积(结果低位截取后32位)
	 * dest * src
	 *
	 * @param src  32-bits
	 * @param dest 32-bits
	 * @return 32-bits
	 */
	public DataType mul(DataType src, DataType dest) {
		String X = src.toString();
		String Y = dest.toString();
		String Y_op = "";
		//得到Y的负数补码表示（取反加一）
		String temp = "";
		String Y_op_re = "";
		for (int i = 0; i < Y.length(); i++) {
			temp += (Y.charAt(i) == '0') ? 1 : 0;
		}

		int carry = 1;
		for (int i = temp.length() - 1; i >= 0; i--) {
			int temp2 = toInt(temp.charAt(i));
			int res = 0 ^ carry ^ temp2;
			carry = (0 & carry) | (0 & temp2) | (carry & temp2);
			Y_op_re += res;
		}
		for (int i = Y_op_re.length() - 1; i >= 0; i--) {
			Y_op += Y_op_re.charAt(i);
		}
		//接下来使用布斯乘法
		String fore = "00000000000000000000000000000000";
		String ans = fore + X;
		for (int i = Y_op.length(); i > 0; i--) {
			int tempNum = (i == X.length()) ?
					(-toInt(X.charAt(X.length() - 1))) :
					(X.charAt(i) - X.charAt(i - 1));
			if (tempNum == 0) {
				ans = rightMove(ans);
			}
			else {
				DataType add1 = new DataType(ans.substring(0, 32));
				DataType add2 = new DataType((tempNum == 1) ? Y : Y_op);
				DataType add = add(add1, add2);
				String new_fore = add.toString();
				ans = new_fore + ans.substring(32);
				ans = rightMove(ans);
			}
		}
		DataType baga = new DataType(ans.substring(32));
		return baga;
	}

	DataType remainderReg = new DataType("00000000000000000000000000000000");

	/**
	 * 返回两个二进制整数的除法结果
	 * dest ÷ src
	 *
	 * @param src  32-bits
	 * @param dest 32-bits
	 * @return 32-bits
	 */
	public DataType div(DataType src, DataType dest) {
		String dived = dest.toString();// 被除数
		String divisor = src.toString();// 除数
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

	public int toInt(char a) {
		return a - 48;
	}

	public DataType add(DataType src, DataType dest) {
		String s1 = src.toString();
		String s2 = dest.toString();
		// carry 为进位
		int carry = 0;
		String ans_reverse = "";
		for (int i = s1.length() - 1; i >= 0; i--) {
			int temp1 = toInt(s1.charAt(i));
			int temp2 = toInt(s2.charAt(i));
			// res 为该位的值，由被加数，加数，进位三者做异或运算得
			int res = temp1 ^ temp2 ^ carry;
			// 更新carry的值
			carry = (temp1 & temp2) | (temp2 & carry) | (temp1 & carry);
			ans_reverse += res;
		}
		String res = "";
		for (int i = ans_reverse.length() - 1; i >= 0; i--) {
			res += ans_reverse.charAt(i);
		}
		DataType m = new DataType(res);
		return m;
	}

	public String rightMove(String m) {
		return ((m.charAt(0) == '0') ? '0' : '1') + m.substring(0, m.length() - 1);
	}

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
		for (int i = s2.length() - 1; i >= 0; i--) {
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


		// step 02:
		carry = 0;
		String res_re = "";
		for (int i = s1.length() - 1; i >= 0; i--) {
			int temp1 = toInt(s1.charAt(i));
			int temp2 = toInt(temp_re.charAt(i));
			int res = temp1 ^ temp2 ^ carry;
			carry = (temp1 & temp2) | (temp2 & carry) | (temp1 & carry);
			res_re += res;
		}
		String res = "";
		for (int i = res_re.length() - 1; i >= 0; i--) {
			res += res_re.charAt(i);
		}
		DataType m = new DataType(res);
		return m;
	}

	public void swap(String m, String n) {
		String temp = m;
		m = n;
		n = temp;
	}

	public static void main(String[] args) {
		DataType m1 = new DataType(Transformer.intToBinary("2"));
		DataType m2 = new DataType(Transformer.intToBinary("-8"));
		//DataType m3 = new DataType(Transformer.intToBinary("5"));
		String m = "1111";
		String n = "2222";
		ALU alu = new ALU();
		System.out.println(alu.div(m1, m2));
	}
}

