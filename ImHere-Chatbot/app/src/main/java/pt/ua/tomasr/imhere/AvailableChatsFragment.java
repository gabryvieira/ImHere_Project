package pt.ua.tomasr.imhere;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pt.ua.tomasr.imhere.chat.ChatActivity;
import pt.ua.tomasr.imhere.modules.GeoChat;
import pt.ua.tomasr.imhere.modules.InfoChat;
import pt.ua.tomasr.imhere.rabitt.MessageBroker;

/**
 * @author Tomás Rodrigues (tomasrodrigues@ua.pt)
 *  Setember 2016
 */

@SuppressLint("ValidFragment")
public class AvailableChatsFragment extends Fragment {

    List<GeoChat> insideCircle = new ArrayList<GeoChat>();
    List<InfoChat> chatinfos = new ArrayList<InfoChat>();
    ArrayList<Double> ids = new ArrayList();
    ArrayList<Integer> ids_info = new ArrayList();

    //Rabbit parameters
    MessageBroker msg = MainActivity.msg;
    String hash = "";
    String user_id = "";
    public static ArrayList<String> chatmessages = new ArrayList<String>();

    //Loading
    ProgressBar progress_bar;
    TextView progress_text;

    //A passar para o Chat
    ImageView imageView;


    public AvailableChatsFragment() {}
    public AvailableChatsFragment(List<GeoChat> insideCircle) {
        this.insideCircle = insideCircle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_availablechats,
                container, false);

        Bundle bundle = this.getArguments();
        hash = bundle.getString("hash");


        //Customized List
        ArrayAdapter<GeoChat> adapter = new MyListAdapter();
        try{
            ArrayAdapter<InfoChat> adapter_info = new MyListAdapter_info();
            ListView list = (ListView) view.findViewById(R.id.available_chats);
            list.setAdapter(adapter_info);

            for (GeoChat tmp:insideCircle) {
                ids.add(tmp.getID());
            }

            new RabbitGetChatInfo().execute();


            ids_info.clear();
            if(msg.getChatInfos().size()==0){
                progress_bar = (ProgressBar) view.findViewById(R.id.progress_bar);
                progress_text = (TextView) view.findViewById(R.id.progress_text);
                progress_text.setText("Looking for Chats...");
                progress_bar.setVisibility(View.VISIBLE);
                progress_text.setVisibility(View.VISIBLE);
                SystemClock.sleep(500);
                progress_bar.setVisibility(View.GONE);
                progress_text.setVisibility(View.GONE);
            }
            for (InfoChat tmp:msg.getChatInfos()) {
                ids_info.add(tmp.getID());
            }

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View viewClicked,
                                        int position, long id) {

                    GeoChat geoChat = insideCircle.get(position);

                    new RabbitJoinChat().execute(geoChat.getID().toString());
                    SystemClock.sleep(500);

                    try{
                        InfoChat infoChat = msg.getChatInfos().get(position);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    String message = "Entering Chat... ";
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                    String extraFromName = getActivity().getIntent().getExtras().getString("EXTRA_SESSION_Name");
                    String extraFromEmail = getActivity().getIntent().getExtras().getString("EXTRA_SESSION_Email");
                    Intent intent = new Intent(getActivity().getBaseContext(), ChatActivity.class);

                    try{
                        intent.putExtra("EXTRA_SESSION_Name", extraFromName);
                        intent.putExtra("EXTRA_SESSION_Email", extraFromEmail);
                        intent.putExtra("EXTRA_SESSION_Hash", hash);
                        intent.putExtra("EXTRA_SESSION_Image", imageView.getTag().toString());
                        intent.putExtra("chat_title",  msg.getChatInfos().get(position).getName());
                        intent.putExtra("chat_subtitle", msg.getChatInfos().get(position).getDescription());
                        intent.putExtra("chat_id", geoChat.getID().toString());
                        intent.putStringArrayListExtra("chat_messages", chatmessages);
                        chatmessages = msg.getChatMessages();

                        startActivityForResult(intent, 1);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            });
        }catch (JSONException e){
            e.printStackTrace();
        }






        // Inflate the layout for this fragment
        return view;
    }

    private class MyListAdapter extends ArrayAdapter<GeoChat> {
        public MyListAdapter() {
            super(getActivity(),R.layout.chat_items, insideCircle);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.chat_items, parent, false);
            }

            // Find the car to work with.
            GeoChat geoChat = insideCircle.get(position);

            // Fill the view
            imageView = (ImageView)itemView.findViewById(R.id.item_icon);
            imageView.setTag(R.id.item_icon);
            //set...

            // Direita:
            TextView radiusText = (TextView) itemView.findViewById(R.id.item_txtRadius);
            radiusText.setText("Radius: "+ geoChat.getRadius().toString());

            // Esquerda/cima:
            TextView latText = (TextView) itemView.findViewById(R.id.item_txtLat);
            latText.setText("Lat: "+ geoChat.getLat().toString());

            // Esquerda/Baixo:
            TextView lonText = (TextView) itemView.findViewById(R.id.item_txtLon);
            lonText.setText("Lon: "+ geoChat.getLon().toString());

            return itemView;
        }
    }

    private class MyListAdapter_info extends ArrayAdapter<InfoChat> {
        public MyListAdapter_info() throws JSONException {
            super(getActivity(),R.layout.chat_items, msg.getChatInfos());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.chat_items, parent, false);
            }

            // Find the car to work with.
            try {
                InfoChat infoChat = msg.getChatInfos().get(position);

                // Fill the view
                imageView = (ImageView)itemView.findViewById(R.id.item_icon);
                imageView.setTag(R.drawable.logo);
                //set...
                switch (infoChat.getEvent()){
                    case "Music Festival":
                        imageView.setImageResource(R.drawable.music_festival);
                        imageView.setTag(R.drawable.music_festival);
                        break;
                   case "Local show":
                            imageView.setImageResource(R.drawable.local_show);
                            imageView.setTag(R.drawable.local_show);
                            break;
                   case "Street market":
                            imageView.setImageResource(R.drawable.market);
                            imageView.setTag(R.drawable.market);
                            break;
                   case "Building Reunion":
                            imageView.setImageResource(R.drawable.building);
                            imageView.setTag(R.drawable.building);
                            break;
                   case "School/University":
                            imageView.setImageResource(R.drawable.uni);
                            imageView.setTag(R.drawable.uni);
                            break;
                   case "Sport related":
                            imageView.setImageResource(R.drawable.sport);
                            imageView.setTag(R.drawable.sport);
                            break;
                   case "Other":
                            //...
                            break;

                }

                // Direita:
                TextView radiusText = (TextView) itemView.findViewById(R.id.item_txtRadius);
                radiusText.setText("Event: "+ infoChat.getEvent().toString());

                // Esquerda/cima:
                TextView latText = (TextView) itemView.findViewById(R.id.item_txtLat);
                latText.setText("Name: "+ infoChat.getName().toString());

                // Esquerda/Baixo:
                TextView lonText = (TextView) itemView.findViewById(R.id.item_txtLon);
                lonText.setText(""+infoChat.getDescription().toString());
                if(infoChat.getDescription().length()>15){
                    lonText.setText(""+infoChat.getDescription().substring(0,14)+"...");
                }

            }catch (JSONException e){
                e.printStackTrace();
            }

            return itemView;
        }
    }

    private class RabbitJoinChat extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                String mensagem = "{\"op_id\":4,\"hash\":\""+hash+"\",\"chat_id\":\""+urls[0]+"\"}";
                chatmessages.clear();
                msg.publish("hello",mensagem);

                SystemClock.sleep(500);

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error","ERRO RABBIT");
            }

            return "";
        }

        protected void onPostExecute(Boolean result) {

        }

    }

    private class RabbitGetChatInfo extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {

            try {

                String mensagem = "{\"op_id\":8,\"hash\":\""+hash+"\",\"chat_id\":"+ids+"}";
                JSONObject obj = new JSONObject(mensagem);

                msg.getChatInfos().clear();
                msg.publish("hello",mensagem);
                SystemClock.sleep(500);


            }catch (Exception e){
                e.printStackTrace();
                Log.e("error","ERRO RABBIT");
            }

            return "";
        }

        protected void onPostExecute(Boolean result) {

        }

    }


}
