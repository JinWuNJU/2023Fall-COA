package cpu.alu;

import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
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

    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
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
// 00000000000100
// 11111111111011
// 11111111111100