package com.cookandroid.datacommunication;

import static com.cookandroid.datacommunication.MainActivity.LOG_TAG;
import static com.cookandroid.datacommunication.MainActivity.alertedCamQueue;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.ResourceUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseToolForRead {
    private MainActivity main;
    private static final String DB_PATH = "CameraList";
    private static final String CAPTURED_PHOTO = "image";
    private static final String TEST_CASE_IMAGE = "motion.gif";
    public static Uri RESULT;
    private final String tmpCamID = "CAM 1";
    private static DatabaseReference myDB = FirebaseDatabase.getInstance().getReference(DB_PATH);
    private Camera onCam;
    public DatabaseToolForRead(MainActivity main) {
        this.main = main;
        this.onCam = null;
    }
    public void initDatabase() throws IOException {
        /**
         * @param String key
         * @param Object key에 대한 값, 예상 타입 boolean, int(time)
         * */
        //makeCam(tmpCamID,System.currentTimeMillis());
        searchCamStat();
        //getCameraListTest();
    }
    /**
     * test용으로 사용중 추후 private 전환 바람.
     *
     * */
    public void makeCam(String camID, long time) throws IOException {
        String newKey = myDB.push().getKey();
        File myDir = new File(main.getFilesDir().getAbsolutePath()+"/myKey.txt");
        myDir.createNewFile();
        FileOutputStream fOS = new FileOutputStream(myDir);
        fOS.write(newKey.getBytes());
        fOS.close();
        Log.d("DB Test : ", newKey);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(newKey,new Camera(camID, time));
        myDB.updateChildren(childUpdates);
        RESULT = null;
    }
    /**
     * 직통으로 연결할땐 안됐는데, child 순회로는 왜 되는걸까?
     * */
    public void searchCamStat(){
        ValueEventListener camStatListener = new ValueEventListener() {
            /** 알람기능이 꺼져있더라도 queue에 alert시 지속적 추가 */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren())
                    if(childSnapshot.getValue(Camera.class).isAlert()) main.pushAlertedCam(childSnapshot.getValue(Camera.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d(LOG_TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        myDB.addValueEventListener(camStatListener);
    }
    /** Uri 값 불러오기는 성공했으나 이너 클래스 메소드 Uri를 다루는 방법을 모름. 이후 리펙토링요망 */
    public static void getCaptureImageUri(Camera cam){
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://raspberrypi-20c10.appspot.com");
        StorageReference storageRef = storage.getReference();
        storageRef.child(CAPTURED_PHOTO+'/'+TEST_CASE_IMAGE).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //이미지 로드 성공시
                RESULT = uri;
//                if(RESULT!=null){
//                    AlertFragment.RESULT_CONVERT = RESULT.toString().substring(0,RESULT.toString().indexOf("&token"));
//                    Log.d("myDB","result "+RESULT.toString().substring(0,RESULT.toString().indexOf("&token")));
//                }

                AlertFragment.RESULT_CONVERT = RESULT.toString().substring(0,RESULT.toString().indexOf("&token"));
                Log.d("myDB","result "+RESULT.toString().substring(0,RESULT.toString().indexOf("&token")));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //이미지 로드 실패시
                RESULT = null;
                Log.d("myDB","image is not exist");
            }
        });
    }
    public static DatabaseReference getInstance(){ return myDB; }
    public Camera getAlertedCam(){ return onCam; }
}
