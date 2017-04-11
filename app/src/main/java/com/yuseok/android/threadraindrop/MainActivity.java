package com.yuseok.android.threadraindrop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnStart, btnStop, btnPause;
    FrameLayout layout;
    Stage stage;

    int devicewidth, deviceheight;

    boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기기의 해상도값을 받아오는 함수
        DisplayMetrics matrix = getResources().getDisplayMetrics();
        devicewidth = matrix.widthPixels;
        deviceheight = matrix.heightPixels;

        layout = (FrameLayout)findViewById(R.id.layout);

        // Button설정
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnPause = (Button) findViewById(R.id.btnPause);

        // Click Listener설정
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        // 커스텀뷰를 FrameLayout에 add
        stage = new Stage(this);
        layout.addView(stage);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                DrawStage drawStage = new DrawStage(stage);
                drawStage.start();
                makeRaindrop rain = new makeRaindrop(stage);
                rain.start();
                break;
            case R.id.btnPause:
                break;
            case R.id.btnStop:
                break;
        }
    }

    class DrawStage extends Thread {
        Stage stage;
        public DrawStage(Stage stage) {
            this.stage = stage;
        }

        public void run() {
            while(running) {
                stage.postInvalidate();
            }
        }
    }

    class makeRaindrop extends Thread {

        boolean flag = true;
        Stage stage;
        public makeRaindrop(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void run() {
            while (flag) {
                new Raindrop(stage);
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Raindrop extends Thread {
        int x;
        int y;
        int radius;
        int speed;

        boolean stopflag = false;
        boolean pauseflag = false;

        Stage stage;

        public Raindrop(Stage stage) {
            Random random = new Random();
            x = random.nextInt(devicewidth);
            y = 0;
            radius = random.nextInt(30) +5;
            speed = random.nextInt(10) +2;

            this.stage = stage;
            stage.addRaindrop(this);
        }

        @Override
        public void run() {
            while(!stopflag) {
                if(!pauseflag)
                y = y + speed;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(y >= deviceheight) {
                stopflag = true;
            }
            stage.removeRaindrop(this);
        }
    }

    class Stage extends View {

        Paint rainColor;
        List<Raindrop> raindrops;

        public Stage(Context context) {
            super(context);
            raindrops = new CopyOnWriteArrayList<>(); // ArrayList를 동기화 해주는 함수. // 향상된 포문을 사용했을시에 오류발생 // ArrayList<>();
            rainColor = new Paint();
            rainColor.setColor(Color.BLUE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Log.i("Rain Size","=======================" + raindrops.size());
            for(Raindrop raindrop : raindrops) {
                    canvas.drawCircle(raindrop.x, raindrop.y, raindrop.radius, rainColor);
            }

            /*
            // 리무브를 추가하지 않았을 시에 향상된 포문을 사용하지 않아도 된다.
            for(int i=0; i<raindrops.size(); i++) {
                Raindrop raindrop = raindrops.get(i);
                canvas.drawCircle(raindrop.x, raindrop.y, raindrop.radius, rainColor);
            }
            */
        }

        public void addRaindrop(Raindrop raindrop) {
            raindrops.add(raindrop);
            raindrop.start();
        }

        public void removeRaindrop(Raindrop raindrop) {
            raindrops.remove(raindrop);
            raindrop.interrupt();
        }
    }
}
