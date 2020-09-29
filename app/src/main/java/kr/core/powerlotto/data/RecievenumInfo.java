package kr.core.powerlotto.data;

import org.json.JSONArray;

import lombok.Data;

@Data
public class RecievenumInfo {
    String regidx;
    String drwNo;
    String date;
    String type;

    JSONArray gameResult = new JSONArray();
}
