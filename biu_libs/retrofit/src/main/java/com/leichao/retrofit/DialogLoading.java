package com.leichao.retrofit;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leichao.network.R;

/**
 * 加载提示dialog
 * Created by leichao on 2017/3/7.
 */
public class DialogLoading {

    private Dialog dialog;
    private boolean cancelable=true;
    private ImageView tipImage;

    public DialogLoading(Context context) {
        this.cancelable=true;
        dialog=createLoadingDialog(context,context.getString(R.string.loading_data));
    }

    public DialogLoading(Context context, String message) {
        this.cancelable=true;
        dialog=createLoadingDialog(context,message);
    }

    public DialogLoading(Context context, String message, boolean cancelable) {
        this.cancelable=cancelable;
        dialog=createLoadingDialog(context,message);
    }

    public void show() {
        if(dialog!=null){
            dialog.show();
        }
    }

    public void dismiss() {
        if(tipImage!=null){
            tipImage.clearAnimation();
        }
        if(dialog!=null){
            dialog.dismiss();
        }
    }

    /**
     * 得到自定义的progressDialog
     *
     */
    private Dialog createLoadingDialog(Context context, String msg) {
        View v = View.inflate(context,R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        tipImage = (ImageView) v.findViewById(R.id.tipImage);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        tipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.net_dialog_no_frame_no_bg);// 创建自定义样式dialog
        loadingDialog.setCancelable(cancelable);// 可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
        return loadingDialog;
    }

}