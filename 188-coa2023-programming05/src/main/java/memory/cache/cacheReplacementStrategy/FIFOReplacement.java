package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;
import util.Transformer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;

/**
 * TODO 先进先出算法
 */
public class FIFOReplacement extends ReplacementStrategy {

    //倒序的链表，方便从前往后遍历找到最早入队的
    LinkedList<Integer> queue = new LinkedList<Integer>();

    @Override
    public int isHit(int start, int end, char[] addrTag) {
        Cache cacheInstance = Cache.getCache();
        for (int i = start; i <= end; i++) {
            if (cacheInstance.isValid(i))
                if (Arrays.equals(addrTag, cacheInstance.getTag(i))) {
                    return i;
                }
        }
        return -1;
    }

    @Override
    public int writeCache(int start, int end, char[] addrTag, byte[] input) {
        Cache cacheInstance = Cache.getCache();
        /*int pointer = -1,addup=0,invalid=-1;
        for (int element : queue) {
            if (element >= start && element <= end) {
                if(!cacheInstance.isValid(element)){
                    invalid=element;
                    break;
                }
                else{
                    pointer = element;
                    addup++;
                }
            }
        }
        if(pointer==-1||addup<(end-start+1)){ //区间未满
            int lineN0=(invalid==-1?start+addup:Math.min(invalid,start+addup));
            cacheInstance.setCacheLine(lineN0,addrTag,input);
            queue.add(0,lineN0);
            return lineN0;
        }else{
            StringBuilder s = new StringBuilder();
            s.append(String.valueOf(cacheInstance.getTag(pointer),7,19));
            int row_num = pointer / 4;
            s.append(Transformer.intToBinary(String.valueOf(row_num)).substring(25,32));
            s.append("000000");
            if(cacheInstance.isDirty(pointer)){
                Memory memoryInstance = Memory.getMemory();
                memoryInstance.write(s.toString(),64,cacheInstance.getData(pointer));
            }
            cacheInstance.setCacheLine(pointer,addrTag,input);
            queue.remove((Integer) pointer);
            queue.add(0,pointer);
            return pointer;
        }
    }*/
        for (int i = start; i <= end; i++) {
            if (!cacheInstance.isValid(i)) {
                cacheInstance.setCacheLine(i, addrTag, input);
                return i;
            }
        }
        int sum = 0;
        int index = 0;
        for (int i = start; i <= end; i++) {
            if (cacheInstance.getTimeStamp(i) == 0) {
                sum++;
            } else if (cacheInstance.getTimeStamp(i) == 2) {
                index = i;
                break;
            }
        }
        if (sum == end - start + 1) {
            StringBuilder s = new StringBuilder();
            s.append(String.valueOf(cacheInstance.getTag(start),7,19));
            int row_num = start / 4;
            s.append(Transformer.intToBinary(String.valueOf(row_num)).substring(25,32));
            s.append("000000");
            if(cacheInstance.isDirty(start)){
                Memory memoryInstance = Memory.getMemory();
                memoryInstance.write(s.toString(),64,cacheInstance.getData(start));
            }
            cacheInstance.setCacheLine(start, addrTag, input);
            cacheInstance.setTimeStampFIFO(start);
            return start;
        } else {
            StringBuilder s = new StringBuilder();
            s.append(String.valueOf(cacheInstance.getTag(index),7,19));
            int row_num = index / 4;
            s.append(Transformer.intToBinary(String.valueOf(row_num)).substring(25,32));
            s.append("000000");
            if(cacheInstance.isDirty(index)){
                Memory memoryInstance = Memory.getMemory();
                memoryInstance.write(s.toString(),64,cacheInstance.getData(index));
            }
            cacheInstance.setCacheLine(index, addrTag, input);
            cacheInstance.setTimeStampFIFO(index);
            return index;
        }
    }
}
