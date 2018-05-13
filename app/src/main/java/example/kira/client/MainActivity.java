package example.kira.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Thread ConnectionThread = null;
    private EditText etInput;
    private TextView tvMsgArea;
    private Button btnSend;

    Socket socket;
    String line;
    String msgout;
    Thread handleServer;
    public static final int SERVERPORT = 4001;
    public static final String SERVERIP = "10.0.2.2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etInput = findViewById(R.id.etInput);
        tvMsgArea = findViewById(R.id.tvMsgArea);
        btnSend = findViewById(R.id.btnSend);
        tvMsgArea.setText("Server is not found, please start server first then restart client app...\n");


        this.ConnectionThread = new Thread(new ConnectionThread());
        this.ConnectionThread.start();


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            msgout = "Client: " + etInput.getText().toString().trim() + "\n";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    etInput.setText("");
                                    tvMsgArea.setText(tvMsgArea.getText().toString().trim() + "\n" + msgout);
                                }
                            });

                            OutputStream outputStream = socket.getOutputStream();

                            outputStream.write(msgout.getBytes());


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        });


    }

    class ConnectionThread implements Runnable {
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVERIP);
                socket = new Socket(serverAddr, SERVERPORT);

                HandleServerSocket handleServerSocket = new HandleServerSocket(socket);
                handleServer=new Thread(handleServerSocket);
                handleServer.start();

                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class HandleServerSocket implements Runnable {
        private Socket serverSocket;
        private BufferedReader serverMsg;

        public HandleServerSocket(Socket serverSocket) {
            this.serverSocket = serverSocket;
            try {
                this.serverMsg = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        public void run() {

//            while (!Thread.currentThread().isInterrupted()) {

            try {

                while ((line = serverMsg.readLine()) != null) {
                    if(tvMsgArea.getText().toString().trim().contains("Server is not found, please start server first then restart client app...")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMsgArea.setText("");
                            }
                        });
                    }
                    if ("quit".equalsIgnoreCase(line)) {
                        break;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMsgArea.setText(tvMsgArea.getText().toString() +  line + "\n");
                        }
                    });

                    if(!socket.isConnected()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMsgArea.setText(tvMsgArea.getText().toString().trim() + "\nYou are disconnected, please start the server then restart the client...");
                            }
                        });

                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

//            }

        }
    }
}
