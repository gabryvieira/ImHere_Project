package pt.ua.tomasr.imhere;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author Tomás Rodrigues (tomasrodrigues@ua.pt)
 *  Setember 2016
 */

@SuppressLint("ValidFragment")
public class UserProfileFragment extends Fragment {
    String gname, gemail;
    Uri gphoto;

    public UserProfileFragment() {

    }

    public UserProfileFragment(String gname, String gemail, Uri gphoto) {
        this.gname = gname;
        this.gemail = gemail;
        this.gphoto = gphoto;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_screen,
                container, false);

        //Get's
        TextView namebot = (TextView) view.findViewById(R.id.user_profile_name);
        TextView emailbot = (TextView) view.findViewById(R.id.user_profile_short_bio);
        ImageButton photo = (ImageButton) view.findViewById(R.id.user_profile_photo);

        TextView nametop = (TextView) view.findViewById(R.id.user_name);
        TextView emailtop = (TextView) view.findViewById(R.id.user_email);

        //Set's
        nametop.setText(gname);
        emailtop.setText(gemail);
        namebot.setText(gname);
        emailbot.setText(gemail);
        //photo.setImageResource(gphoto);
        photo.setImageURI(gphoto); //VER!!!

        // Inflate the layout for this fragment
        return view;

    }

}
