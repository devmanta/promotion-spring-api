package com.dndn.promotions.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class DrawUtils {

//    	if( isPercentWin(50) ) {
//        //확률 50%로, 이벤트 당첨 처리
//        }
    public boolean isPercentWin(int percentNum) {
        boolean result = false;
        List<Integer> arrPick = getScopeList(1, 100);
        int pickNum = getRandom(1, 100); //백분율, 1 ~ 100개 숫자중 택1(이 숫자가 확률배열에 포함여부 체크)
        if(0 < percentNum && percentNum < 100) {
            Collections.shuffle(arrPick);
            for(int i = 1; i <= percentNum; i++) {
                if(arrPick.get(i - 1) == pickNum) {
                    result = true;
                    break;
                }
            }
        } else if(percentNum >= 100) {
            result = true;
        }
        return result;
    }

    /**
     * 범위 숫자 리스트 반환
     */
    public List<Integer> getScopeList(int pstart, int pend) {
        List<Integer> list = new ArrayList<Integer>();

        for(int i = pstart; i <= pend; i++) {
            list.add(i);
        }

        return list;
    }

    public int getRandom(int p_start, int p_end) {
        Random rnd = new Random();
        if(p_start >= p_end) {
            return 0;
        } else {
            return rnd.nextInt(p_end - p_start + 1) + p_start;
        }
    }

}
