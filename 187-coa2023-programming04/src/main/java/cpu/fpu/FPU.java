package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };

    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        ALU alu = new ALU();
        // 1. 检查边界情况
        String a = dest.toString();
        String b = src.toString();
        // NaN
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        // InF | 0
        String check = cornerCheck(addCorner,a,b);
        if (check != null){
            return new DataType(check);
        }

        if(a.equals(IEEE754Float.P_ZERO) || a.equals(IEEE754Float.N_ZERO)){
            return src;
        }
        if(b.equals(IEEE754Float.P_ZERO) || b.equals(IEEE754Float.N_ZERO)){
            return dest;
        }
        // 2. 提取，对阶
        char a_sign = a.charAt(0);
        StringBuilder a_exp = new StringBuilder(a.substring(1, 9));
        StringBuilder a_tail = new StringBuilder(a.substring(9));
        char b_sign = b.charAt(0);
        StringBuilder b_exp = new StringBuilder(b.substring(1, 9));
        StringBuilder b_tail = new StringBuilder(b.substring(9));

        StringBuilder ans_exp = a_exp;
        char a_hidden = '1';
        char b_hidden = '1';
		char ans_sign = a_sign;
        // 特殊情况：
		if (a_exp.toString().equals("00000000")) {
			a_exp.replace(7,8,"1");
            a_hidden = '0';
		}
        else if(a_exp.toString().equals("11111111")){
            return dest;
        }
        if (b_exp.toString().equals("00000000") ) {
            b_exp.replace(7,8,"1");
            b_hidden = '0';
        }
        else if(b_exp.toString().equals("11111111")){
            return src;
        }

        a_tail.insert(0,a_hidden);
        a_tail.append("000");
        b_tail.insert(0,b_hidden);
        b_tail.append("000");

        int delta = alu.subE(a_exp.toString(),b_exp.toString());
        if(delta > 0){
            b_tail = new StringBuilder(rightShift(b_tail.toString(),delta));
            ans_exp = a_exp;
        }
        else if(delta < 0){
            a_tail = new StringBuilder(rightShift(a_tail.toString(),-delta));
            ans_exp = b_exp;
        }

        String temp = "";
        DataType C;

        if(a_sign == b_sign){
            C = alu.add(new DataType(extend(a_tail.toString(),32)),new DataType(extend(b_tail.toString(),32)));
            temp = C.toString().substring(4);
            ans_sign = a_sign;
            if(temp.charAt(0) == '1'){
                ans_exp = alu.addOne(ans_exp);
                temp = rightShift(temp,1);
                temp = temp.substring(1,28);
				if(ans_exp.toString().equals("11111111")){
					return (a_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
				}
            }
            else {
                temp = temp.substring(1,28);
			}
        }
        else {
            if(a_sign == '1' && b_sign == '0'){// b-a
                a_tail = new StringBuilder(alu.getOppo(String.valueOf(a_tail)));
                C = alu.add(new DataType(extend(a_tail.toString(),32)),new DataType(extend(b_tail.toString(),32)));
                temp = C.toString().substring(4);

                if(temp.charAt(0) == '1'){
                    ans_sign = b_sign;
                    temp = temp.substring(1);
                }
                else {
                    ans_sign = a_sign;
                    temp = alu.getOppo(temp.substring(1));
                }
            }
            else {
                b_tail = new StringBuilder(alu.getOppo(String.valueOf(b_tail)));
                C = alu.add(new DataType(extend(a_tail.toString(),32)),new DataType(extend(b_tail.toString(),32)));
                temp = C.toString().substring(4);

                if(temp.charAt(0) == '1'){
                    ans_sign = a_sign;
                    temp = temp.substring(1);
                }
                else {
                    ans_sign = b_sign;
                    temp = alu.getOppo(temp.substring(1));
                }

            }
        }
        
        int flag = -1;
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == '1'){
                flag = i;
                break;
            }
        }
        if(flag < 0){//尾数全为0
            return new DataType(IEEE754Float.P_ZERO);
        }
        int currentExp = alu.getInt(ans_exp.toString());

        if(currentExp > flag){
            int val = currentExp - flag;
            ans_exp = new StringBuilder(extend(Integer.toBinaryString(val),8));
            temp = temp.substring(flag);
            for (int i = 0; i < flag; i++) {
                temp += "0";
            }
        }
        else {
            ans_exp = new StringBuilder("00000000");
            temp = temp.substring(currentExp-1);
            for (int i = 0; i < currentExp-1; i++) {
                temp += "0";
            }
        }
		return new DataType(round(ans_sign,ans_exp.toString(),temp));
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        ALU alu = new ALU();
        // 1. 检查边界情况
        String a = dest.toString();
        String b = src.toString();
        // NaN
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        // InF | 0
        String check = cornerCheck(subCorner,a,b);
        if (check != null){
            return new DataType(check);
        }

        if(a.equals(IEEE754Float.P_INF) || a.equals(IEEE754Float.N_INF)){
            return dest;
        }

        if(b.equals(IEEE754Float.P_INF) || b.equals(IEEE754Float.N_INF)){
            return new DataType(alu.ver(b.charAt(0)) + b.substring(1));
        }

        if(a.equals(IEEE754Float.P_ZERO) || a.equals(IEEE754Float.N_ZERO)){
            return new DataType(alu.ver(b.charAt(0)) + b.substring(1));
        }
        if(b.equals(IEEE754Float.P_ZERO) || b.equals(IEEE754Float.N_ZERO)){
            return dest;
        }
        // 2. 提取，对阶
        char a_sign = a.charAt(0);
        StringBuilder a_exp = new StringBuilder(a.substring(1, 9));
        StringBuilder a_tail = new StringBuilder(a.substring(9));
        char b_sign = b.charAt(0);
        StringBuilder b_exp = new StringBuilder(b.substring(1, 9));
        StringBuilder b_tail = new StringBuilder(b.substring(9));

        StringBuilder ans_exp = a_exp;
        char a_hidden = '1';
        char b_hidden = '1';
        char ans_sign = a_sign;
        // 特殊情况：
        if (a_exp.toString().equals("00000000")) {
            a_exp.replace(7,8,"1");
            a_hidden = '0';
        }
        else if(a_exp.toString().equals("11111111")){
            return dest;
        }
        if (b_exp.toString().equals("00000000") ) {
            b_exp.replace(7,8,"1");
            b_hidden = '0';
        }
        else if(b_exp.toString().equals("11111111")){
            return src;
        }

        a_tail.insert(0,a_hidden);
        a_tail.append("000");
        b_tail.insert(0,b_hidden);
        b_tail.append("000");

        int delta = alu.subE(a_exp.toString(),b_exp.toString());
        if(delta > 0){
            b_tail = new StringBuilder(rightShift(b_tail.toString(),delta));
            ans_exp = a_exp;
        }
        else if(delta < 0){
            a_tail = new StringBuilder(rightShift(a_tail.toString(),-delta));
            ans_exp = b_exp;
        }

        String temp = "";
        DataType C;

        if(a_sign != b_sign){
            C = alu.add(new DataType(extend(a_tail.toString(),32)),new DataType(extend(b_tail.toString(),32)));
            temp = C.toString().substring(4);
            ans_sign = a_sign;
            if(temp.charAt(0) == '1'){
                ans_exp = alu.addOne(ans_exp);
                temp = rightShift(temp,1);
                temp = temp.substring(1,28);
                if(ans_exp.toString().equals("11111111")){
                    return (a_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
                }
            }
            else {
                temp = temp.substring(1,28);
            }
        }
        else {
            if(a_sign == '1' && b_sign == '1'){// b-a
                a_tail = new StringBuilder(alu.getOppo(String.valueOf(a_tail)));
                C = alu.add(new DataType(extend(a_tail.toString(),32)),new DataType(extend(b_tail.toString(),32)));
                temp = C.toString().substring(4);

                if(temp.charAt(0) == '1'){
                    ans_sign = '0';
                    temp = temp.substring(1);
                }
                else {
                    ans_sign = '1';
                    temp = alu.getOppo(temp.substring(1));
                }
            }
            else {
                b_tail = new StringBuilder(alu.getOppo(String.valueOf(b_tail)));
                C = alu.add(new DataType(extend(a_tail.toString(),32)),new DataType(extend(b_tail.toString(),32)));
                temp = C.toString().substring(4);

                if(temp.charAt(0) == '1'){
                    ans_sign = '0';
                    temp = temp.substring(1);
                }
                else {
                    ans_sign = '1';
                    temp = alu.getOppo(temp.substring(1));
                }

            }
        }

        int flag = -1;
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == '1'){
                flag = i;
                break;
            }
        }
        if(flag < 0){//尾数全为0
            return new DataType(IEEE754Float.P_ZERO);
        }
        int currentExp = alu.getInt(ans_exp.toString());

        if(currentExp > flag){
            int val = currentExp - flag;
            ans_exp = new StringBuilder(extend(Integer.toBinaryString(val),8));
            temp = temp.substring(flag);
            for (int i = 0; i < flag; i++) {
                temp += "0";
            }
        }
        else {
            ans_exp = new StringBuilder("00000000");
            temp = temp.substring(currentExp-1);
            for (int i = 0; i < currentExp-1; i++) {
                temp += "0";
            }
        }
        return new DataType(round((Float.parseFloat(Transformer.binaryToFloat(a)) > Float.parseFloat(Transformer.binaryToFloat(b)) ? '0' : '1'),ans_exp.toString(),temp));
    }

    /**
     * compute the float mul of (dest * src)
     */
    public DataType mul(DataType src,DataType dest){
        ALU alu = new ALU();
        // 1. 检查边界情况
        String a = dest.toString();
        String b = src.toString();
        char a_sign = a.charAt(0);
        char b_sign = b.charAt(0);
        char ans_sign = (a_sign == b_sign) ? '0' : '1';
        // NaN
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        // InF | 0
        String check = cornerCheck(mulCorner,a,b);
        if (check != null){
            return new DataType(check);
        }

        if(a.equals(IEEE754Float.P_ZERO) || a.equals(IEEE754Float.N_ZERO)  || b.equals(IEEE754Float.P_ZERO) || b.equals(IEEE754Float.N_ZERO)){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_ZERO) : new DataType(IEEE754Float.N_ZERO);
        }

        // 2. 提取

        StringBuilder a_exp = new StringBuilder(a.substring(1, 9));
        StringBuilder a_tail = new StringBuilder(a.substring(9));
        StringBuilder b_exp = new StringBuilder(b.substring(1, 9));
        StringBuilder b_tail = new StringBuilder(b.substring(9));

        StringBuilder ans_exp = a_exp;
        char a_hidden = '1';
        char b_hidden = '1';

        // 特殊情况：
        if (a_exp.toString().equals("00000000")) {
            a_exp.replace(7,8,"1");
            a_hidden = '0';
        }
        else if(a_exp.toString().equals("11111111")){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        }
        if (b_exp.toString().equals("00000000") ) {
            b_exp.replace(7,8,"1");
            b_hidden = '0';
        }
        else if(b_exp.toString().equals("11111111")){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        }

        a_tail.insert(0,a_hidden);
        a_tail.append("000");
        b_tail.insert(0,b_hidden);
        b_tail.append("000");

        int a_expo = alu.getInt(String.valueOf(a_exp));
        int b_expo = alu.getInt(String.valueOf(b_exp));

        //对正负1单独处理
        if(a_expo == 127 &&a_tail.toString().equals("100000000000000000000000000")){
            return new DataType(ans_sign + b.substring(1));
        }
        if(b_expo == 127 && b_tail.toString().equals("100000000000000000000000000")){
            return new DataType(ans_sign + a.substring(1));
        }

        int ans_expo = a_expo + b_expo - 127 + 1;
        ans_exp = new StringBuilder(extend(Integer.toBinaryString(ans_expo), 8));


        String temp = mul(a_tail.toString(),b_tail.toString());

        while (temp.charAt(0) == '0' && ans_expo > 0){
            ans_expo --;
            temp = temp.substring(1) + "0";
        }

        while (ans_expo < 0 && !temp.substring(0,27).equals("000000000000000000000000000")){
            temp = rightShift(temp,1);
            ans_expo ++;
        }

        if(ans_expo >= 255){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        }
        else if(ans_expo < 0){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_ZERO) : new DataType(IEEE754Float.N_ZERO);
        }
        else if(ans_expo == 0){
            temp = rightShift(temp,1);
        }

        return new DataType(round(ans_sign,extend(Integer.toBinaryString(ans_expo),8),temp));
    }

    /**
     * compute the float mul of (dest / src)
     */
    public DataType div(DataType src,DataType dest){
        ALU alu = new ALU();
        // 1. 检查边界情况
        String a = dest.toString();
        String b = src.toString();
        char a_sign = a.charAt(0);
        char b_sign = b.charAt(0);
        char ans_sign = (a_sign == b_sign) ? '0' : '1';
        // NaN
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        // InF | 0
        String check = cornerCheck(divCorner,a,b);
        if (check != null){
            return new DataType(check);
        }

        if(a.equals(IEEE754Float.P_ZERO) || a.equals(IEEE754Float.N_ZERO)){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_ZERO) : new DataType(IEEE754Float.N_ZERO);
        }

        else if(a.equals(IEEE754Float.N_INF) || a.equals(IEEE754Float.P_INF)){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        }

        if(b.equals(IEEE754Float.P_ZERO) ||b.equals(IEEE754Float.N_ZERO)){
            throw new ArithmeticException();
        }

        else if(b.equals(IEEE754Float.N_INF) || b.equals(IEEE754Float.P_INF)){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_ZERO) : new DataType(IEEE754Float.N_ZERO);
        }

        // 2. 提取

        StringBuilder a_exp = new StringBuilder(a.substring(1, 9));
        StringBuilder a_tail = new StringBuilder(a.substring(9));
        StringBuilder b_exp = new StringBuilder(b.substring(1, 9));
        StringBuilder b_tail = new StringBuilder(b.substring(9));

        StringBuilder ans_exp = a_exp;
        char a_hidden = '1';
        char b_hidden = '1';

        // 特殊情况：
        if (a_exp.toString().equals("00000000")) {
            a_exp.replace(7,8,"1");
            a_hidden = '0';
        }
        else if(a_exp.toString().equals("11111111")){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        }
        if (b_exp.toString().equals("00000000") ) {
            b_exp.replace(7,8,"1");
            b_hidden = '0';
        }
        else if(b_exp.toString().equals("11111111")){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        }

        a_tail.insert(0,a_hidden);
        a_tail.append("000");
        b_tail.insert(0,b_hidden);
        b_tail.append("000");

        int a_expo = alu.getInt(String.valueOf(a_exp));
        int b_expo = alu.getInt(String.valueOf(b_exp));

        //对正负1单独处理
        if(b_expo == 127 && b_tail.toString().equals("100000000000000000000000000")){
            return new DataType(ans_sign + a.substring(1));
        }

        int ans_expo = a_expo - b_expo + 127;
        ans_exp = new StringBuilder(extend(Integer.toBinaryString(ans_expo), 8));

        String temp = div(b_tail.toString(),a_tail.toString());
//        String temp = alu.div(new DataType(extend(b_tail.toString(),32)),new DataType(extend(a_tail.toString(),32))).toString().substring(5);
        while (temp.charAt(0) == '0' && ans_expo > 0){
            ans_expo --;
            temp = temp.substring(1) + "0";
        }

        while (ans_expo < 0 && !temp.substring(0,27).equals("000000000000000000000000000")){
            temp = rightShift(temp,1);
            ans_expo ++;
        }

        if(ans_expo >= 255){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        }
        else if(ans_expo < 0){
            return (ans_sign == '0') ? new DataType(IEEE754Float.P_ZERO) : new DataType(IEEE754Float.N_ZERO);
        }
        else if(ans_expo == 0){
            temp = rightShift(temp,1);
        }

        return new DataType(round(ans_sign,extend(Integer.toBinaryString(ans_expo),8),temp));

    }

    public String mul(String src, String dest){
        ALU alu = new ALU();
        String res = "000000000000000000000000000" + dest;
        for (int i = 0; i < 27; i++) {
            if(dest.charAt(26 - i) == '0'){
                res = "0" + res.substring(0,53);
            }
            else {
                //取4的妙处：有符号右移
                String temp = alu.add(new DataType(extend(res.substring(0,27),32)), new DataType(extend(src, 32))).toString().substring(4);
                res = temp + res.substring(27,53);
            }
        }
        return res;
    }

    public String div(String src,String dest){
        String quot = "";
        String temp = dest;
        ALU alu = new ALU();
        for (int i = 0; i < 27; i++) {
            temp = alu.sub(new DataType(extend(src,32)),new DataType(extend(dest,32))).toString().substring(5);
            if (temp.charAt(0) == '0'){
                quot += '1';
                dest = temp.substring(1) + "0";
            }
            else {
                quot += '0';
                dest = dest.substring(1) + "0";
            }
        }
        return quot;
    }

    public String extend(String s,int len){
        String res = "";
        for (int i = 0; i < len - s.length(); i++) {
            res += "0";
        }
        res += s;
        return res;
    }

    /**
     * check corner cases of mul and div
     *
     * @param cornerMatrix corner cases pre-stored
     * @param oprA first operand (String)
     * @param oprB second operand (String)
     * @return the result of the corner case (String)
     */
    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return sign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carry to the next)
     *         and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

}
