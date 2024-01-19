package memory.disk;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Scheduler {

    /**
     * 先来先服务算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        int length = request.length;
        double sum = 0.0;
        for(int i = 0;i < length;i++){
            sum += Math.abs(request[i] - start);
            start = request[i];
        }
        return sum / length;
    }

    /**
     * 最短寻道时间优先算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        double sum = 0.0;
        Arrays.sort(request);
        int min = request[0];
        int max = request[request.length-1];
        sum += Math.min(Math.abs(start - min), Math.abs(start - max));
        sum += (max - min);
        sum /= request.length;
        return sum;
    }

    /**
     * 扫描算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        int length = request.length;
        Arrays.sort(request);
        if(start <= request[0]){
            return (double) (request[length - 1] - start) / length;
        }else if(start == 255){
            return (double) (start - request[0]) / length;
        }
        if(direction){
            return (double) (255 - start + 255 - request[0]) / length;
        }else {
            return (double) (start + request[length - 1]) / length;
        }
    }

    /**
     * C-SCAN算法：默认磁头向磁道号增大方向移动
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @return 平均寻道长度
     */
    public double CSCAN(int start,int[] request){
        int length = request.length;
        Arrays.sort(request);
        if(start <= request[0]){
            return (double) (request[length - 1] - start) / length;
        }else {
            int index = -1;
            for (int i = 0;i < length;i++){
                if(request[i] >= start){
                    index = i;
                    break;
                }
            }
            if(index == -1){
                return (double) (255 - start + 255 + request[length - 1]) / length;
            }else {
                return (double) (255 - start + 255 + request[index - 1]) / length;
            }
        }
    }

    /**
     * LOOK算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double LOOK(int start,int[] request,boolean direction){
        int length = request.length;
        Arrays.sort(request);
        if(start <= request[0]){
            return (double) (request[length - 1] - start) / length;
        }else if(start >= request[length - 1]){
            return (double) (start - request[0]) / length;
        }
        if(direction){
            return (double) (request[length - 1] - start + request[length - 1] - request[0]) / length;
        }else {
            return (double) (start - request[0] + request[length - 1] - request[0]) / length;
        }
    }

    /**
     * C-LOOK算法：默认磁头向磁道号增大方向移动
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @return 平均寻道长度
     */
    public double CLOOK(int start,int[] request){
        int length = request.length;
        Arrays.sort(request);
        if(start <= request[0]){
            return (double) (request[length - 1] - start) / length;
        }else {
            int index = -1;
            for (int i = 0;i < length;i++){
                if(request[i] >= start){
                    index = i;
                    break;
                }
            }
            if(index == -1){
                return (double) (start - request[0] + request[length - 1] - request[0]) / length;
            }else {
                return (double) (request[length - 1] - start + request[length - 1] - request[0] + request[index - 1] - request[0]) / length;
            }
        }
    }
}
