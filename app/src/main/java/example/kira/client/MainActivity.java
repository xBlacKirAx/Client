package example.kira.client;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Handler UIHandler;
    Thread ConnectionThread =null;
    private EditText etInput;
    private TextView tvMsgArea;
    private Button btnSend;

    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    String read;
    public static final int SERVERPORT=8818;
    public static final String SERVERIP="10.0.2.2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etInput=findViewById(R.id.etInput);
        tvMsgArea=findViewById(R.id.tvMsgArea);
        btnSend=findViewById(R.id.btnSend);
        tvMsgArea.setText("");

        UIHandler=new Handler();

        this.ConnectionThread =new Thread(new ConnectionThread());
        this.ConnectionThread.start();





    }
    class ConnectionThread implements Runnable{
        public void run(){

            try{
                InetAddress serverAddr=InetAddress.getByName(SERVERIP);
                socket=new Socket(serverAddr,SERVERPORT);
                dis=new DataInputStream(socket.getInputStream());
                dos=new DataOutputStream(socket.getOutputStream());
                HandleServerSocket commThread=new HandleServerSocket(socket);
                new Thread(commThread).start();

                return;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    class HandleServerSocket implements Runnable{
        private Socket clientSocket;
        private BufferedReader serverMsg;
        public HandleServerSocket(Socket clientSocket){
            this.clientSocket=clientSocket;
            try{
                this.serverMsg =new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        public void run(){

            while(!Thread.currentThread().isInterrupted()){

                try{
//                    System.out.println("test!!!!!!!!!!");
//                    read= serverMsg.readLine();
//                    System.out.println("test!!!!!!!!!!");
//                    if(read!=null){
////                        UIHandler.post(new updateUIThread(read));
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvMsgArea.setText(tvMsgArea.getText().toString()+"Server says: "+read+"\n");
//                                read="";
//                            }
//                        });
//                    }else{
//                        ConnectionThread =new Thread(new ConnectionThread());
//                        ConnectionThread.start();
//                        return;
//                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            btnSend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    String msgout="";
                                    try{
                                        msgout=etInput.getText().toString().trim();
                                        dos.writeUTF("Client: "+msgout);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                    }).start();

                    String line;
                    while((line=serverMsg.readLine())!=null){
                        if("quit".equalsIgnoreCase(line)){
                            break;
                        }
                        tvMsgArea.setText(tvMsgArea.getText().toString()+"Server says: "+line+"\n");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    class updateUIThread implements  Runnable{
        private String msg;
        public  updateUIThread(String str){
            this.msg=str;
        }

        @Override
        public void run() {
            tvMsgArea.setText(tvMsgArea.getText().toString()+"Server says: "+msg+"\n");
        }
    }
}
