package com.cookandroid.datacommunication;

import static com.cookandroid.datacommunication.MainActivity.LOG_TAG;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;

public class AlertFragment extends Fragment {
    Button offAlertBtn;
    TextView alertedCamText;
    MainActivity main;
    Timer timer;
    ViewGroup viewGroup;
    TimerTask uiEffect;
    Camera onCam;
    DatabaseToolForRead tmpDB;
    public AlertFragment(MainActivity main, Timer timer,Camera camera,DatabaseToolForRead tmpDB){
        this.main = main;
        this.timer = timer;
        this.onCam = camera;
        this.tmpDB = tmpDB;
    }
    @Nullable
    @Override
    /**
     * @param Resource = 객체화될 프래그먼트 xml
     * @param root = 객체화될 View의 parent layout 지정, 선택사항
     * attachToRoot == true ~ parent에 객체화된 View를 바로 add
     *              == false ~ parent 레이아웃의 레이아웃 파라미터만 받아옴
     * @param attachToRoot = 부모 ViewGroup에 프래그먼트가 바로 자식으로 들어갈지 말지를 결정
     */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.alert, container, false);
        offAlertBtn = (Button) viewGroup.findViewById(R.id.offAlertBtn);
        alertedCamText = (TextView) viewGroup.findViewById(R.id.alertedCamText);
        //alertedCamText.append(tmpDB.);
        offAlertBtn.setOnClickListener(v -> {
            main.resumeMainUI();
            uiEffect.cancel();
            Log.d(LOG_TAG, "Timer Cancled");
        });
        doEffect();
        timer.scheduleAtFixedRate(uiEffect,50,600);
        Log.d(LOG_TAG, "onCreateView");
        return viewGroup;
    }
    private void doEffect(){
        uiEffect = new TimerTask() {
            @Override
            public void run(){
                try{
                    Log.d(LOG_TAG, "Timer On Going");
                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!main.isOn())return;
                            int color = Color.TRANSPARENT;
                            Drawable background = viewGroup.getBackground();
                            if (background instanceof ColorDrawable){
                                color = ((ColorDrawable) background).getColor();
                            }
                            if(color == Color.RED){
                                viewGroup.setBackgroundColor(Color.BLUE);
                            }
                            else viewGroup.setBackgroundColor(Color.RED);
                        }
                    });
                    Thread.sleep(300);
                }catch (InterruptedException e){
                    System.out.println("Interrupted Exception at AlertFragment.");
                    cancel();
                }
            }
        };
    }
}
