package memory.cache.cacheMappingStrategy;

import memory.cache.Cache;
import memory.Memory;
import util.Transformer;
public class SetAssociativeMapping extends MappingStrategy{


    /**
     *
     * @param blockNO 内存数据块的块号
     * @return cache数据块号 22-bits  [前14位有效]
     */
    @Override
    public char[] getTag(int blockNO) {
        Cache cacheInstance = Cache.getCache();
        int setSize = (int)(Math.log(cacheInstance.getSETS()) / Math.log(2));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Transformer.intToBinary(String.valueOf(blockNO)).substring(6, 32 - setSize));
        while (stringBuilder.length() < 26){
            stringBuilder.insert(0,0);
        }


        return stringBuilder.toString().toCharArray();
    }

    /**
     *
     * @param blockNO 目标数据内存地址前22位int表示
     * @return -1 表示未命中
     */

    @Override
    public int map(int blockNO) {
        Cache cacheInstance = Cache.getCache();
        int log_row_size = (int)(Math.log(cacheInstance.getSetSize()) / Math.log(2));
        int start = Integer.parseInt(getSetBits(blockNO),2) << log_row_size;
        int end = start + (int)Math.pow(2,log_row_size) - 1;
        return super.replacementStrategy.isHit(start,end,getTag(blockNO));
    }

    @Override
    public int writeCache(int blockNO) {
        Memory memoryInstance=Memory.getMemory();
        String eip=Transformer.intToBinary(String.valueOf(blockNO)).substring(6,32)+"000000";
        byte[] data=memoryInstance.read(eip,Cache.LINE_SIZE_B);
        Cache cacheInstance = Cache.getCache();
        int log_row_size = (int)(Math.log(cacheInstance.getSetSize()) / Math.log(2));
        int start = Integer.parseInt(getSetBits(blockNO),2) << log_row_size;
        int end = start + (int)Math.pow(2,log_row_size) - 1;
        return super.replacementStrategy.writeCache(start,end,getTag(blockNO),data);
    }

    private static String getSetBits(int blockN0){
        Cache cacheInstance = Cache.getCache();
        int setSize = (int)(Math.log(cacheInstance.getSETS()) / Math.log(2));
        return "0"+Transformer.intToBinary(String.valueOf(blockN0)).substring(32 - setSize,32);
    }

    public static void main(String[] args) {
        String aa = "abcde";
        System.out.println(aa.substring(0));
    }
}
