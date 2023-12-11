package com.cookandroid.datacommunication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    @Override
    public File getFilesDir() {
        Log.d("DB TAG", super.getFilesDir().getAbsolutePath());
        return super.getFilesDir();
    }
    public static final String LOG_TAG = "FragmentTracking";
    RecyclerView cameraStatus;
    Button logicSwitch;
    protected FragmentManager fragmentManager;
    protected FragmentTransaction fragmentTransaction;
    FrameLayout fragmentDisplay;
    Timer timer;
    private static boolean isOn, taskOn;
    static DatabaseToolForRead tmpDB;
    static Queue<Camera> alertedCamQueue;
    static Camera onCam;
    static TimerTask raisedAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraStatus = (RecyclerView) findViewById(R.id.cameraStatus);
        logicSwitch = (Button) findViewById(R.id.logicSwitch);
        fragmentDisplay = (FrameLayout) findViewById(R.id.fragmentDisplay);

        timer = new Timer();
        isOn = false;
        alertedCamQueue = new LinkedList<>();
        //프래그먼트 매니저 획득
        fragmentManager = getSupportFragmentManager();

        //프래그먼트 Transaction 획득
        //프래그먼트를 올리거나 교체하는 작업을 Transaction이라고 합니다.
        //프래그먼트를 FrameLayout의 자식으로 등록해줍니다.
        logicSwitch.setOnClickListener(v -> {
            if(!isOn()){
                try {
                    initDB();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                toggleSwitch();
                logicSwitch.setText("정지");
                logicSwitch.setBackgroundColor(Color.RED);
                //timer.scheduleAtFixedRate(trackingAlert(),);
            }
            else {
                toggleSwitch();
                logicSwitch.setText("재시작");
                logicSwitch.setBackgroundColor(Color.BLUE);
            }
        });
    }
    private void initDB() throws IOException {
        tmpDB = new DatabaseToolForRead(this);
        tmpDB.initDatabase();
    }
    public void resumeMainUI(){
        toggleSwitch();
        fragmentTransaction.remove(getSupportFragmentManager().findFragmentByTag("alertScreen"));
        fragmentDisplay.setVisibility(View.GONE);
        logicSwitch.setVisibility(View.VISIBLE);
        cameraStatus.setVisibility(View.VISIBLE);
    }
    private TimerTask trackingAlert(){
        if(!taskOn) {
            taskOn = true;
            return new TimerTask() {
                @Override
                public void run() {
                    onCam = popAlertedCam();
                    if(onCam != null) {
                        showAlert(onCam);
                        try {
                            raisedAlert = this;
                            this.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            };
        }
        else return null;
    }
    protected void showAlert(Camera camera){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentDisplay,new AlertFragment(this,timer,camera,tmpDB),"alertScreen").commit();
        logicSwitch.setVisibility(View.GONE);
        cameraStatus.setVisibility(View.GONE);
        fragmentDisplay.setVisibility(View.VISIBLE);
    }
    public boolean isOn(){ return isOn; }
    public void toggleSwitch(){ isOn = !isOn(); }
    /** add는 queue에서 추출할게 없거나 기타 오류시 throw exception / offer는 null반환 */
    public void pushAlertedCam(Camera camera){
        alertedCamQueue.offer(camera);
        showAlert(alertedCamQueue.peek());
    }
    /** remove는 queue에서 추출할게 없거나 기타 오류시 throw exception / poll은 null반환 */
    public Camera popAlertedCam(){ return alertedCamQueue.poll(); }
}