package memory.cache.cacheReplacementStrategy;


import memory.cache.Cache;

import java.util.Arrays;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement extends ReplacementStrategy {

    public int isHit(int start, int end, char[] addrTag) {
        Cache cacheInstance= Cache.getCache();
        for(int i=start;i<=end;i++){
            if(cacheInstance.isValid(i))
                if(Arrays.equals(cacheInstance.getTag(i),addrTag)) {
                    cacheInstance.addVisited(i);
                    return i;
                }
        }
        return -1;
    }

    @Override
    public int writeCache(int start, int end, char[] addrTag, byte[] input) {
        Cache cacheInstance=Cache.getCache();
        int pointer=-1,addup=0,invalid=-1;
        int visited=Integer.MAX_VALUE;
        for(int i=start;i<=end;i++){
            if(!cacheInstance.isValid(i)){
                invalid=i;
                break;
            }else{
                if(cacheInstance.getVisited(i)<visited){
                    visited=cacheInstance.getVisited(i);
                    pointer=i;
                }
                addup++;
            }
        }
        if(pointer==-1||addup<(end-start+1)){
            cacheInstance.setCacheLine(invalid,addrTag,input);
            cacheInstance.setCacheVisited(invalid,1);
            return invalid;
        }else{
            cacheInstance.setCacheLine(pointer,addrTag,input);
            cacheInstance.setCacheVisited(pointer,1);
            return pointer;
        }
    }

}
