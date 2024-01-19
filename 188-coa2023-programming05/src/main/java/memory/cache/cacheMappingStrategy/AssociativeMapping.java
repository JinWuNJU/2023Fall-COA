package memory.cache.cacheMappingStrategy;

import memory.cache.Cache;
import memory.Memory;
import util.Transformer;

import java.util.Arrays;
public class AssociativeMapping extends MappingStrategy {  // 全相联映射

    /**
     * @param blockNO 内存数据块的块号
     * @return cache数据块号 22-bits  [前22位有效]
     */
    @Override
    public char[] getTag(int blockNO) {
        return Transformer.intToBinary(String.valueOf(blockNO)).substring(6, 32).toCharArray();
    }

    @Override
    public int map(int blockNO) {
        return this.replacementStrategy.isHit(0, (Cache.CACHE_SIZE_B / Cache.LINE_SIZE_B) - 1, getTag(blockNO));
    }

    @Override
    public int writeCache(int blockNO) {
        Memory memoryInstance = Memory.getMemory();
        String eip = Transformer.intToBinary(String.valueOf(blockNO)).substring(6, 32) + "000000";
        byte[] data = memoryInstance.read(eip, Cache.LINE_SIZE_B);
        return this.replacementStrategy.writeCache(0, (Cache.CACHE_SIZE_B / Cache.LINE_SIZE_B) - 1, getTag(blockNO), data);
    }
}