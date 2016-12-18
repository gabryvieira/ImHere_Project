package pt.ua.tomasr.imhere.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pt.ua.tomasr.imhere.MainActivity;
import pt.ua.tomasr.imhere.R;
import pt.ua.tomasr.imhere.rabitt.MessageBroker;

public class ChatActivity extends AppCompatActivity {

    private ListView mListView;
    private Button mButtonSend;
    private Button DeleteButton;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    private String mensagem_do_bot="Bot: ";

    //messages
    public ArrayList<String> chatmessages = new ArrayList<String>();
    String user_name = "";

    //one_message
    public String one_message = "";
    public String test = "";
    int primeira_vez = 0;


    //Loading
    ProgressBar progress_bar;
    TextView progress_text;

    //Rabbit parameters
    MessageBroker msg = MainActivity.msg;
    String hash = "";
    String user_id = "";
    String extraTitle = "";
    String chat_id = "";
    Double d_chat_id = -1.0;


    private ChatMessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        //Extras
        chatmessages = getIntent().getStringArrayListExtra("chat_messages");

        extraTitle = getIntent().getStringExtra("chat_title");
        TextView Title = (TextView) findViewById(R.id.chat_title);
        Title.setText(extraTitle); //tittle

        String extrasubTitle = getIntent().getStringExtra("chat_subtitle");
        TextView subTitle = (TextView) findViewById(R.id.chat_subtitle);
        subTitle.setText(extrasubTitle);//subtittle

        if(getIntent().getStringExtra("EXTRA_SESSION_Image")!=null) {
            Integer background = Integer.parseInt(getIntent().getStringExtra("EXTRA_SESSION_Image"));
            ImageView rel = (ImageView) findViewById(R.id.background);
            rel.setBackgroundResource(background);
        }

        hash = getIntent().getStringExtra("EXTRA_SESSION_Hash");
        user_id = getIntent().getStringExtra("EXTRA_SESSION_Email");

        String[] tmp = user_id.split("@");
        user_name = tmp[0];
        chat_id = getIntent().getStringExtra("chat_id");
        d_chat_id = Double.parseDouble(chat_id);

        //Fazer algo com isto
        String extraId = getIntent().getStringExtra("chat_id");
        //Fazer algo com isto

        mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (Button) findViewById(R.id.btn_send);
        //DeleteButton = (Button) findViewById(R.id.delete_btn);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mImageView = (ImageView) findViewById(R.id.iv_image);

        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(mAdapter);


//        DeleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String URL = "http://192.168.8.217:5011/location/point/"+chat_id;
//                // pass second argument as "null" for GET requests
//                JsonObjectRequest req = new JsonObjectRequest(URL, null,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                try {
//                                    VolleyLog.v("Response:%n %s", response.toString(4));
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        VolleyLog.e("Error: ", error.getMessage());
//                    }
//                });
//
//// add the request object to the queue to be executed
//                ApplicationController.getInstance().addToRequestQueue(req);
//
//
//            }
//        });


        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Google Auth Info
                String extraFromName = getIntent().getStringExtra("EXTRA_SESSION_Name");
                Log.i("nome", "" + extraFromName);

                String message = mEditTextMessage.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                sendMessage(user_name + ": " + message);
                mEditTextMessage.setText("");

                //Rabbit message
                new RabbitSendMessage().execute(message);

                //ChatBot
//                if(message.startsWith("@")) {
//
//                    //      HTTP GET CLOSESTS POINTS
//                    JSONObject json_obtido;
//                    message = message.substring(1); //tirar o @
//                    if(message.contains(" "))message = message.replace(" ","%20");
//                    Log.i("mensagem",message);
//                    String URL = "http://192.168.8.217:5010/runService?message="+message;
//                    new ChatActivity.GETChatBotMessage().execute(URL);
//                    if(mensagem_do_bot.equals("Bot: ")) SystemClock.sleep(1500);
//                    mimicOtherMessage(mensagem_do_bot);
//                }

            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //historico de mensagens
        //chatmessages.clear();

        Log.i("chat-messages", chatmessages.toString());
        if (chatmessages.size() == 0) {
            progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
            progress_text = (TextView) findViewById(R.id.progress_text);
            progress_text.setText("Loading old messages...");
            progress_bar.setVisibility(View.VISIBLE);
            progress_text.setVisibility(View.VISIBLE);
            SystemClock.sleep(1000);
            progress_bar.setVisibility(View.GONE);
            progress_text.setVisibility(View.GONE);
        }

        //enviar para chat
        for (int i = 0; i < chatmessages.size(); i++) {
            if (chatmessages.get(i).toString().contains(user_name)) {
                sendMessage(chatmessages.get(i));
            } else mimicOtherMessage(chatmessages.get(i));
        }

        //Ver novas mensagens
        callAsynchronousTask();

    }

    public void callAsynchronousTask(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {

                            PerformBackgroundTask performBackgroundTask = new PerformBackgroundTask();
                            // PerformBackgroundTask this class is the class that extends AsynchTask
                            performBackgroundTask.execute();

                            String[] tmp_msg = one_message.split("#");
                            Integer id_chat = Integer.parseInt(tmp_msg[0]);
                            if(!one_message.equals("") && (id_chat==(d_chat_id.intValue()))){
                                if(!test.equals(one_message)){
                                    mimicOtherMessage(tmp_msg[1]);
                                }

                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 6000); //execute in every 50000 ms
    }

    public void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);

        //mimicOtherMessage(message);
    }

    private void mimicOtherMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false, false);
        mAdapter.add(chatMessage);
    }

    private void sendMessage() {
        ChatMessage chatMessage = new ChatMessage(null, true, true);
        mAdapter.add(chatMessage);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        ChatMessage chatMessage = new ChatMessage(null, false, true);
        mAdapter.add(chatMessage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RabbitSendMessage extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                String mensagem = "{\"op_id\":2,\"hash\":\""+hash+"\",\"chat_id\":\""+chat_id+"\",\"msg\":\""+urls[0]+"\"}";
                msg.publish("hello",mensagem);

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error","ERRO RABBIT");
            }

            return "";
        }

        protected void onPostExecute(Boolean result) {

        }

    }

    private class PerformBackgroundTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {
            if(primeira_vez!=0) test = one_message;
            one_message = msg.one_message;
            Log.i("one_message",""+one_message);
            Log.i("teste_message",""+test);

            primeira_vez +=1;
            return one_message;
        }

        protected void onPostExecute(Boolean result) {

        }

    }
}
