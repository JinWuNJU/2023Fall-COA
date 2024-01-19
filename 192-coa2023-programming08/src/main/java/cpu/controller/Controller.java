package cpu.controller;

import cpu.alu.ALU;
import memory.Memory;
import util.DataType;
import util.Transformer;

import java.util.Arrays;


public class Controller {
    // general purpose register
    char[][] GPR = new char[32][32];
    // program counter
    char[] PC = new char[32];
    // instruction register
    char[] IR = new char[32];
    // memory address register
    char[] MAR = new char[32];
    // memory buffer register
    char[] MBR =  new char[32];
    char[] ICC = new char[2];

    // 单例模式
    private static final Controller controller = new Controller();

    private Controller(){
        //规定第0个寄存器为zero寄存器
        GPR[0] = new char[]{'0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0'};
        ICC = new char[]{'0','0'}; // ICC初始化为00
    }

    public static Controller getController(){
        return controller;
    }

    public void reset(){
        PC = new char[32];
        IR = new char[32];
        MAR = new char[32];
        GPR[0] = new char[]{'0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0'};
        ICC = new char[]{'0','0'}; // ICC初始化为00
        interruptController.reset();
    }

    public InterruptController interruptController = new InterruptController();
    public ALU alu = new ALU();

    public String getByteIns(byte[] data){
        String ans = "";
        for (int i = 0; i < data.length; i++) {
            ans += Transformer.intToBinary(String.valueOf(data[i])).substring(24);
        }
        return ans;
    }

    public void tick(){
        Controller controller1 = getController();
        if(String.valueOf(controller1.ICC).equals("00")){
            getInstruct();
            controller1.ICC = new char[]{'1', '0'};
            if(getOp().equals("1101110")){
                findOperand();
            }
            else if(getOp().equals("1100111")){
                controller1.ICC = new char[]{'1', '1'};
            }
        }
        else if(String.valueOf(controller1.ICC).equals("10")){
            operate();
            controller1.ICC = new char[]{'0', '0'};
        }
        else {
            interrupt();
            interruptController.signal = false;
            controller1.ICC = new char[]{'0', '0'};
        }
    }



    /** 执行取指操作 */
    private void getInstruct(){
        Controller controller1 = getController();
        controller1.MAR = Arrays.copyOf(controller1.PC,controller1.PC.length);
        Memory memory = Memory.getMemory();
        byte[] data = memory.read(String.valueOf(controller1.MAR),4);
        String m = getByteIns(data);
        GPR[1] = Arrays.copyOf(controller1.PC,controller1.PC.length);
        controller1.PC = alu.add(new DataType(String.valueOf(controller1.PC)),new DataType(Transformer.intToBinary("4"))).toString().toCharArray();
        controller1.MBR = m.toCharArray();
        controller1.IR = Arrays.copyOf(m.toCharArray(),m.toCharArray().length);
    }

    /** 执行间址操作 */
    private void findOperand(){
        int addc_src1 = Integer.parseInt(String.valueOf(IR).substring(15,20),2);
        int addc_src2 = Integer.parseInt(String.valueOf(IR).substring(20,25),2);
        GPR[2] = alu.add(new DataType(String.valueOf(GPR[addc_src1])),new DataType(String.valueOf(GPR[addc_src2]))).toString().toCharArray();
    }

    private String getOp(){
        Controller controller1 = getController();
        return String.valueOf(controller1.IR).substring(0,7);
    }


    private String fill32bit(String s){
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < 32 - s.length(); i++) {
            sb.insert(0,"0");
        }
        return sb.toString();
    }

    /** 执行周期 */
    private void operate(){
        Controller controller1 = getController();
        String op = getOp();
        Memory memory = Memory.getMemory();
        switch (op){
            case "1100110":
                int add_dest = Integer.parseInt(String.valueOf(IR).substring(7,12),2);
                int add_src1 = Integer.parseInt(String.valueOf(IR).substring(15,20),2);
                int add_src2 = Integer.parseInt(String.valueOf(IR).substring(20,25),2);
                GPR[add_dest] = alu.add(new DataType(String.valueOf(GPR[add_src1])),new DataType(String.valueOf(GPR[add_src2]))).toString().toCharArray();
                break;
            case "1110110":
                String s = "1000000000000";
                int lui_dest = Integer.parseInt(String.valueOf(IR).substring(7,12),2);
                GPR[lui_dest] = fill32bit(s).toCharArray();
                break;
            case "1100000":
                int lw_dest = Integer.parseInt(String.valueOf(IR).substring(7,12),2);
                int lw_src = Integer.parseInt(String.valueOf(IR).substring(15,20),2);
                byte[] data = memory.read(String.valueOf(GPR[lw_src]),4);
                String load = getByteIns(data);
                GPR[lw_dest] = load.toCharArray();
                break;
            case "1100100":
                int addi_dest = Integer.parseInt(String.valueOf(IR).substring(7,12),2);
                int addi_src = Integer.parseInt(String.valueOf(IR).substring(15,20),2);
                GPR[addi_dest] = alu.add(new DataType(String.valueOf(GPR[addi_src])),new DataType(fill32bit(String.valueOf(IR).substring(24)))).toString().toCharArray();
                break;
            case "1101110":
                int addc_dest = Integer.parseInt(String.valueOf(IR).substring(7,12),2);
                byte[] addc_data = memory.read(String.valueOf(GPR[2]),4);
                String addc_load = getByteIns(addc_data);
                GPR[addc_dest] = addc_load.toCharArray();
                break;
            case "1110011":
                int jalr_dest = Integer.parseInt(String.valueOf(IR).substring(7,12),2);
                int jalr_src = Integer.parseInt(String.valueOf(IR).substring(15,20),2);
                GPR[jalr_dest] = alu.add(new DataType(String.valueOf(GPR[1])),new DataType(Transformer.intToBinary("4"))).toString().toCharArray();
                PC = alu.add(new DataType(String.valueOf(GPR[jalr_src])),new DataType(fill32bit(String.valueOf(IR).substring(24)))).toString().toCharArray();
        }
    }

    /** 执行中断操作 */
    private void interrupt(){
        interruptController.handleInterrupt();
        interruptController.signal = false;
    }

    public class InterruptController{
        // 中断信号：是否发生中断
        public boolean signal;
        public StringBuffer console = new StringBuffer();
        /** 处理中断 */
        public void handleInterrupt(){
            console.append("ecall ");
        }
        public void reset(){
            signal = false;
            console = new StringBuffer();
        }
    }

    // 以下一系列的get方法用于检查寄存器中的内容进行测试，请勿修改

    // 假定代码程序存储在主存起始位置，忽略系统程序空间
    public void loadPC(){
        PC = GPR[0];
    }

    public char[] getRA() {
        //规定第1个寄存器为返回地址寄存器
        return GPR[1];
    }

    public char[] getGPR(int i) {
        return GPR[i];
    }

}
