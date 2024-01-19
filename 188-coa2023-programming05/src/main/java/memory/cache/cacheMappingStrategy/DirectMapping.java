package memory.cache.cacheMappingStrategy;

import memory.cache.Cache;
import memory.Memory;
import util.Transformer;

import java.util.Arrays;
public class DirectMapping extends MappingStrategy {

    @Override
    public char[] getTag(int blockNO) {
        return ("000000000" + Transformer.intToBinary(String.valueOf(blockNO)).substring(6, 23)).toCharArray();
    }


    /**
     * 根据内存地址找到对应的行是否命中，直接映射不需要用到替换策略
     *
     * @param blockNO
     * @return -1 表示未命中
     */
    @Override
    public int map(int blockNO) {
        int lineN0 = Integer.parseInt("0" + Transformer.intToBinary(String.valueOf(blockNO)).substring(23, 32),2);
        Cache cacheInstance = Cache.getCache();
        if (cacheInstance.isValid(lineN0))
            if (Arrays.equals(cacheInstance.getTag(lineN0), getTag(blockNO)))
                return lineN0;
        return -1;
    }

    /**
     * 在未命中情况下重写cache，直接映射不需要用到替换策略
     *
     * @param blockNO
     * @return
     */
    @Override
    public int writeCache(int blockNO) {
        Cache cacheInstance=Cache.getCache();
        Memory memory=Memory.getMemory();
        int lineN0=Integer.parseInt("0" + Transformer.intToBinary(String.valueOf(blockNO)).substring(23, 32),2);
        String eip=Transformer.intToBinary(String.valueOf(blockNO)).substring(6,32)+"000000";
        cacheInstance.setCacheLine(lineN0,getTag(blockNO),memory.read(eip,Cache.LINE_SIZE_B));
        return lineN0;
    }


}
