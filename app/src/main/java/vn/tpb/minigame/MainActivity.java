package vn.tpb.minigame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static ai.fpt.minigame.utils.base.MiniGame.GENDER;
import ai.fpt.minigame.ui.MiniGameActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenMiniGame = findViewById(R.id.btn_open_minigame);
        // Mở mini game
        btnOpenMiniGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Truyền tham số giới tính
                Bundle extras = new Bundle();
                extras.putString(GENDER, "Male");

                // Bắt đầu mini game với giới tính
                MiniGameActivity.start(MainActivity.this, extras);
            }
        });
    }
}