package com.leichao.push;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;

/**
 * 推送消息Bean
 * Created by leichao on 2017/3/11.
 */
public class PushMessage implements Serializable {

    private String messageStr;// 原始消息字符串
    private String id = "";
    private String type = "";
    private String result = "";
    private String title = "";

    public PushMessage(String messageStr) {
        setMessageStr(messageStr);
    }

    public String getMessageStr() {
        return messageStr;
    }

    public void setMessageStr(String messageStr) {
        this.messageStr = messageStr;
        try {
            JsonObject jsonObject = new JsonParser().parse(messageStr).getAsJsonObject();
            setType(jsonObject.get("type").getAsString());
            setId(jsonObject.get("messageId").getAsString());
            setTitle(jsonObject.get("title").getAsString());
            JsonElement resultElement = jsonObject.get("result");
            if (resultElement.isJsonPrimitive()) {
                setResult(resultElement.getAsString());
            } else {
                setResult(resultElement.toString());// 这里是toString
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "type:" + getType() + "---id:" + getId() + "---result:" + getResult();
    }

    public TYPE parseType() {
        switch (type) {
            case "chat":
                return TYPE.TYPE_CHAT;
            case "201":
                return TYPE.TYPE_201;
            case "202":
                return TYPE.TYPE_202;
            case "203":
                return TYPE.TYPE_203;
            case "204":
                return TYPE.TYPE_204;
            case "205":
                return TYPE.TYPE_205;
            case "206":
                return TYPE.TYPE_206;
            case "207":
                return TYPE.TYPE_207;
            case "208":
                return TYPE.TYPE_208;
            case "209":
                return TYPE.TYPE_209;
            case "210":
                return TYPE.TYPE_210;
            case "211":
                return TYPE.TYPE_211;
            case "212":
                return TYPE.TYPE_212;
            case "213":
                return TYPE.TYPE_213;
            case "214":
                return TYPE.TYPE_214;
            case "215":
                return TYPE.TYPE_215;
            case "216":
                return TYPE.TYPE_216;
            case "220":
                return TYPE.TYPE_220;
            case "221":
                return TYPE.TYPE_221;
            case "222":
                return TYPE.TYPE_222;
            case "223":
                return TYPE.TYPE_223;
            case "224":
                return TYPE.TYPE_224;
            case "225":
                return TYPE.TYPE_225;
            case "226":
                return TYPE.TYPE_226;
            case "227":
                return TYPE.TYPE_227;
            case "228":
                return TYPE.TYPE_228;
            //拉货订单推送
            case "231":
                return TYPE.TYPE_231;
            case "232":
                return TYPE.TYPE_232;
            case "233":
                return TYPE.TYPE_233;
            case "234":
                return TYPE.TYPE_234;
            case "235":
                return TYPE.TYPE_235;
            case "236":
                return TYPE.TYPE_236;
            case "237":
                return TYPE.TYPE_237;
            case "238":
                return TYPE.TYPE_238;
            case "239":
                return TYPE.TYPE_239;
            case "240":
                return TYPE.TYPE_240;
            case "241":
                return TYPE.TYPE_241;
            case "242":
                return TYPE.TYPE_242;
            case "243":
                return TYPE.TYPE_243;
            case "244":
                return TYPE.TYPE_244;
            case "245":
                return TYPE.TYPE_245;
            case "246":
                return TYPE.TYPE_246;
            case "247":
                return TYPE.TYPE_247;
            case "248":
                return TYPE.TYPE_248;
            //中国拉人推送
            case "1":
                return TYPE.TYPE_1;
            case "2":
                return TYPE.TYPE_2;
            case "3":
                return TYPE.TYPE_3;
            default:
                return TYPE.TYPE_000;
        }
    }

    public boolean isPlaceOrderType(){
        switch (parseType()){
            case TYPE_201:
            case TYPE_231:
                return true;
        }
        return false;
    }

    public enum TYPE {
        TYPE_CHAT("chat"),// 聊天类型
        TYPE_000("000"),// 默认类型，无意义
        TYPE_201("201"),// 用户下单向司机发推送
        TYPE_202("202"),// 司机接单后向用户发推送
        TYPE_203("203"),// 司机到达出发点
        TYPE_204("204"),// 司机确认乘客上车
        TYPE_205("205"),// 司机确认目的地
        TYPE_206("206"),// 司机发起支付
        TYPE_207("207"),// 用户现金确认付款
        TYPE_208("208"),// 用户微信确认付款
        TYPE_209("209"),// 司机确认用户现金支付
        TYPE_210("210"),// 司机取消订单向用户发推送
        TYPE_211("211"),// 用户取消订单向司机发推送
        TYPE_212("212"),// 用户LR付款成功
        TYPE_213("213"),// 用户指派司机
        TYPE_214("214"),// 司机拒绝订单向用户发推送
        TYPE_215("215"),// 司机已出发（通知用户）
        TYPE_216("216"),// 司机已出发 （通知司机）
        TYPE_220("220"),// 系统派单
        TYPE_221("221"),// 异地登陆
        TYPE_222("222"),// 消息通知
        TYPE_223("223"),// 优惠券消息
        TYPE_224("224"),// 提现通知消息
        TYPE_225("225"),// 改派订单成功
        TYPE_226("226"),// 系统取消订单（用户）
        TYPE_227("227"),// 系统取消订单（司机）
        TYPE_228("228"),// 司机改派订单向用户发推送
        //拉货推送的消息
        TYPE_231("231"),// 用户下单向司机发推送
        TYPE_232("232"),// 司机接单后向用户发推送
        TYPE_233("233"),// 司机到达取货点
        TYPE_234("234"),// 司机发起收款
        TYPE_235("235"),// 用户已支付（线上支付）
        TYPE_236("236"),// 确认取货
        TYPE_237("237"),// 到达收货点
        TYPE_238("238"),// 确认收货
        TYPE_239("239"),// 司机已确认支付
        TYPE_240("240"),// 司机取消
        TYPE_241("241"),// 用户取消
        TYPE_242("242"),// 订单完后，向用户推送
        TYPE_243("243"),// 用户确认现金支付
        TYPE_244("244"),// 配送订单还有30分钟超过配送时间提醒
        TYPE_245("245"),// 30分钟无司机接单，订单已取消
        TYPE_246("246"),// 系统指派拉货订单
        TYPE_247("247"),// 系统已强制取货
        TYPE_248("248"),// 系统已强制签收
        //中国拉人推送消息
        TYPE_1("1"),// 订单消息
        TYPE_2("2"),// 优惠券消息
        TYPE_3("3");// 系统消息

        public String type;

        TYPE(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public boolean isCargoOrderMessage(){
        switch (parseType()){
            case TYPE_231:
            case TYPE_232:
            case TYPE_233:
            case TYPE_234:
            case TYPE_235:
            case TYPE_236:
            case TYPE_237:
            case TYPE_238:
            case TYPE_239:
            case TYPE_240:
            case TYPE_241:
            case TYPE_242:
            case TYPE_243:
            case TYPE_244:
            case TYPE_245:
            case TYPE_246:
            case TYPE_247:
            case TYPE_248:
                return true;
        }
        return false;
    }

    public boolean isMannedOrderMessage(){
        switch (parseType()){
            case TYPE_201:
            case TYPE_202:
            case TYPE_203:
            case TYPE_204:
            case TYPE_205:
            case TYPE_206:
            case TYPE_207:
            case TYPE_208:
            case TYPE_209:
            case TYPE_210:
            case TYPE_212:
            case TYPE_213:
            case TYPE_214:
            case TYPE_215:
            case TYPE_216:
            case TYPE_220:
            case TYPE_221:
            case TYPE_222:
            case TYPE_223:
            case TYPE_224:
            case TYPE_225:
            case TYPE_226:
            case TYPE_227:
            case TYPE_228:
                return true;
        }
        return false;
    }

}
