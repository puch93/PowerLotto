package kr.core.powerlotto.network;

import android.content.Context;

import kr.core.powerlotto.network.netUtil.BaseReq;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.util.StringUtil;

public abstract class ReqBasic extends BaseReq {
    public ReqBasic(Context context, String url) {
        super(context, url);
    }

    @Override
    public HttpResult onParse(String jsonString) {

        HttpResult res = new HttpResult();
        if (StringUtil.isNull(jsonString)){
            res.setResult(null);
        }else{
            res.setResult(jsonString);
        }
        return res;
    }
}
