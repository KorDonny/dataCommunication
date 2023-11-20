package com.cookandroid.datacommunication;

import static com.cookandroid.datacommunication.MainActivity.LOG_TAG;
import static com.cookandroid.datacommunication.MainActivity.alertedCamQueue;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseToolForRead {
    private MainActivity main;
    private static final String DB_PATH = "Camera";
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
        getCameraListTest();
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
    public void getCameraListTest(){
        ArrayList<Camera> treeList = new ArrayList<>();
        Query myTopPostsQuery = myDB.orderByChild("isAlert").equalTo(true);
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                treeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Camera treeListData = dataSnapshot.getValue(Camera.class);
                    treeList.add(treeListData);
                }
                for(Camera camera : treeList){
                    Log.w("myDB", camera+" ID : "+camera.getCamID());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("myDB", "onCancelled");
            }
        });
    }
    public static DatabaseReference getInstance(){ return myDB; }
    public Camera getAlertedCam(){ return onCam; }
}
