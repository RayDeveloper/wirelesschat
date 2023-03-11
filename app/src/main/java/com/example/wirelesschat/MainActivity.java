package com.example.wirelesschat;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.wirelesschat.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();

    }

    private void initialWork() {

        btnOnOff= (Button) findViewById(R.id.onOff);
        btnDiscover= (Button) findViewById(R.id.discover);
        btnSend= (Button) findViewById(R.id.sendButton);
        listView= (ListView) findViewById(R.id.peerListView);
        read_msg_box= (TextView) findViewById(R.id.readMsg);
        connectionStatus= (TextView) findViewById(R.id.connectionStatus);
        writeMsg= (EditText) findViewById(R.id.writeMsg);

    }

}