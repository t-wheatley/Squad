package uk.ac.tees.donut.squad;

import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;
import android.os.Handler;
/**
 * Created by jlc-1 on 21/03/2017.
 */

public class SplashScreen extends Activity
{
    private final int splash_length = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}
