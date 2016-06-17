package gusu.muzhe.plugin.alipay;

import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AliPayPlugin extends CordovaPlugin {
    private static String TAG = "AliPayPlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            JSONObject arguments = args.getJSONObject(0);
            String payInfo = arguments.getString("payInfo");

            this.pay(payInfo, callbackContext);
        } catch (JSONException e) {
            callbackContext.error(new JSONObject());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void pay(final String payInfo, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(cordova.getActivity());
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo);
                PayResult payResult = new PayResult(result);
                if (TextUtils.equals(payResult.getResultStatus(), "9000")) {
                    callbackContext.success(payResult.toJson());
                } else {
                    // 判断resultStatus 为非“9000”则代表可能支付失败
                    // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    if (TextUtils.equals(payResult.getResultStatus(), "8000")) {
                        callbackContext.success(payResult.toJson());
                    } else {
                        callbackContext.error(payResult.toJson());
                    }
                }
            }
        });
    }
}
