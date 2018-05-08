package biz.cstevens.oddsgame.Util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import biz.cstevens.oddsgame.R;
import biz.cstevens.oddsgame.Documents.UserDocument;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private ArrayList<DocumentSnapshot> users;
    private int selectedPos = RecyclerView.NO_POSITION;

    public UserListAdapter() {
        this.users = new ArrayList<>();
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        final UserDocument user = users.get(position).toObject(UserDocument.class);

        holder.relativeLayout.setSelected(position == selectedPos);
        holder.name.setText(user.name);
        new DownloadImageTask(holder.avatar).execute(user.imgUri);
    }


    public void setUsers(List<DocumentSnapshot> userList) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        users.clear();
        for (DocumentSnapshot user : userList) {
            if (!user.getId().equals(userId)) users.add(user);
        }
        this.notifyDataSetChanged();
    }

    public DocumentSnapshot getUser() {
        if (selectedPos != RecyclerView.NO_POSITION) return users.get(selectedPos);
        else return null;
    }

    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        } else {
            return 0;
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout relativeLayout;
        public ImageView avatar;
        public TextView name;

        public UserViewHolder(View itemView) {
            super(itemView);

            this.relativeLayout = itemView.findViewById(R.id.user_relative_layout);
            this.avatar = itemView.findViewById(R.id.user_img);
            this.name = itemView.findViewById(R.id.user_name);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPos = getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
        }
    }
}


