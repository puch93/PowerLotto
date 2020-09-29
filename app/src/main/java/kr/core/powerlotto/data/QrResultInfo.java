package kr.core.powerlotto.data;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Data;

@Data
public class QrResultInfo {

    String key_clr1;
    String date;
    String bx_notice_winner1;
    String price;
    String bx_notice_winner2;

    public String[] winnums = new String[7];

    public ArrayList<String> resPos;

    public ArrayList<GameNum> game;

    public HashMap<String,ArrayList<GameNum>> games;

}
