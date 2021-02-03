package com.example.devoir1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //Local variables
    protected static int attemptCounter = 1;
    protected static long startTime;
    protected static TextView txtEssai;
    protected static TextView txtTimer;
    protected static Button btnReact;
    protected static AlertDialog alert;
    protected static long averageTime = 0;
    protected static long millis = 0;
    protected static int seconds = 0;
    //Runnable timer basé sur: https://medium.com/@evan.x.liu/using-ascending-timers-in-android-studio-650268348c9d
    protected static Handler Handler = new Handler();
    protected static Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            seconds = (int) (millis/1000);
            seconds = seconds % 60;
            txtTimer.setText(String.format("%d.%02d", seconds, millis-1000*seconds));
            Handler.postDelayed(this, 0);
        }
    };

    //État d'initialisation de l'application
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnReact = (Button)findViewById(R.id.btnReact);
        txtEssai = (TextView)findViewById(R.id.txtEssai);
        txtTimer = (TextView)findViewById(R.id.txtTimer);
        txtEssai.setText(getResources().getString(R.string.nbEssaiTxt) + attemptCounter + " de 5");
        txtTimer.setText(getResources().getString(R.string.initialTimerTxt));
    }

    //État quand l'application commence à run
    @Override
    protected void onStart() {
        super.onStart();
        Button btnReact = (Button)findViewById(R.id.btnReact);
        btnReact.setOnClickListener(v -> waitState());
    }

    //État repos de l'application
    private void waitState() {
        int maxWait = 10000;
        int minWait = 3000;
        int range = maxWait-minWait;
        long waitTime = (long) (Math.random()*range)+minWait;

        txtEssai.setVisibility(View.VISIBLE);
        txtTimer.setVisibility(View.VISIBLE);
        txtEssai.setText(getResources().getString(R.string.nbEssaiTxt) + attemptCounter + " de 5");
        btnReact.setBackgroundColor(getResources().getColor(R.color.grey));
        btnReact.setText(getResources().getString(R.string.btnTxtWait));
        //Délai attente entre 3-10 secondes
        Handler.postDelayed(this::startState, waitTime);
        //Si clique avant que bouton devient jaune, affiche message erreur
        btnReact.setOnClickListener(v -> errorState());
    }

    //État début du jeu où utilisateur doit peser sur le bouton le plus vite possible
    private void startState() {
        startTime = System.currentTimeMillis();
        btnReact.setBackgroundColor(getResources().getColor(R.color.yellow));
        btnReact.setText(getResources().getString(R.string.btnTxtStart));
        Handler.postDelayed(timerRunnable, 0);
        //Une fois utilisateur clique sur le bouton affiche message succès
        btnReact.setOnClickListener(v -> successState());
    }

    //État erreur (utilisateur clique avant que le timer commence)
    private void errorState() {
        btnReact.setBackgroundColor(getResources().getColor(R.color.red));
        btnReact.setText(getResources().getString(R.string.btnTxtError));
        Handler.postDelayed(this::waitState, 1500);
    }

    //État succès (utilisateur clique après que le bouton tourne jaune)
    private void successState() {
        attemptCounter++;
        averageTime += millis;
        Handler.removeCallbacks(timerRunnable);
        btnReact.setText(getResources().getString(R.string.btnTxtSuccess));
        btnReact.setBackgroundColor(getResources().getColor(R.color.green));
        txtTimer.setText(getResources().getString(R.string.initialTimerTxt));
        //Nombre d'essai excédé, application arrête, sinon continue
        if(attemptCounter > 5){
            Handler.postDelayed(this::endState, 1500);
        }
        else{
            Handler.postDelayed(this::waitState, 1500);
        }
    }

    //État fin, montre un alert dialog qui affiche le temps moyen avant de recommencer le jeu
    private void endState() {
        Handler.removeCallbacks(timerRunnable);
        seconds = (int) (averageTime/1000)/5;
        String showAverage = String.format("%d.%02d", seconds, (averageTime/5)-1000*seconds);
        //Reinitialisation du jeu
        attemptCounter = 1;
        averageTime = 0;
        //Affichage AlertDialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.completedTestTxt))
                .setMessage(getResources().getString(R.string.averageTimeTxt) + showAverage)
                .setPositiveButton(getResources().getString(R.string.okBtnTxt), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        waitState();
                    }
                });
        alert = builder1.create();
        alert.show();
    }
}