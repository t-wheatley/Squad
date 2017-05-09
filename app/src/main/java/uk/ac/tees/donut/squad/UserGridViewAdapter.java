package uk.ac.tees.donut.squad;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.ac.tees.donut.squad.activities.ProfileActivity;

public class UserGridViewAdapter extends BaseAdapter
{

    private Context mContext;

    // Array of names
    private final List<String> userNames;
    // Array of pictures
    private final List<String> userPics;
    // Array of uIds
    private final List<String> userIds;


    public UserGridViewAdapter(Context context, List<String> userNames, List<String> userPics,
                               List<String> userIds)
    {
        mContext = context;
        this.userNames = userNames;
        this.userPics = userPics;
        this.userIds = userIds;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup parent)
    {
        View view;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
        {

            view = new View(mContext);
            view = inflater.inflate(R.layout.gridview_user, null);

            TextView userNameView = (TextView) view.findViewById(R.id.gridview_user_text);
            ImageView userPicView = (CircleImageView) view.findViewById(R.id.gridview_user_image);

            //Display name
            userNameView.setText(userNames.get(i));

            // Display picture
            Glide.with(mContext)
                    .load(userPics.get(i))
                    .listener(new RequestListener<String, GlideDrawable>()
                    {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource)
                        {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource)
                        {
                            return false;
                        }
                    })
                    .dontAnimate()
                    .fitCenter()
                    .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                    .into(userPicView);

            // OnClick
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Sends the user's id to the profile activity
                    Intent detail = new Intent(mContext, ProfileActivity.class);
                    detail.putExtra("uId", userIds.get(i));
                    mContext.startActivity(detail);
                }
            });

        } else
        {
            view = (View) convertView;
        }

        return view;
    }

    @Override
    public int getCount()
    {
        return userNames.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }
}
