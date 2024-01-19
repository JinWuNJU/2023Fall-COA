package memory.cache.cacheMappingStrategy;

import memory.cache.cacheReplacementStrategy.ReplacementStrategy;
public abstract class MappingStrategy {

    protected ReplacementStrategy replacementStrategy;

    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }
    public abstract char[] getTag(int blockNO);

    public abstract int map(int blockNO);

    public abstract int writeCache(int blockNO);
}
