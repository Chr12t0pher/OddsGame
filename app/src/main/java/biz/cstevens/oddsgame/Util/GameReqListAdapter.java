package biz.cstevens.oddsgame.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;

import biz.cstevens.oddsgame.Documents.OddsDocument;
import biz.cstevens.oddsgame.InGameFragment;
import biz.cstevens.oddsgame.R;

public class GameReqListAdapter extends RecyclerView.Adapter<GameReqListAdapter.GameReqViewHolder> {

    private ArrayList<DocumentSnapshot> games;
    private int selectedPos = RecyclerView.NO_POSITION;
    private Context context;

    public GameReqListAdapter() {
        this.games = new ArrayList<>();
    }

    @Override
    public GameReqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.oddsreq_list_item, parent, false);
        return new GameReqViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final GameReqViewHolder holder, int position) {
        final OddsDocument game = games.get(position).toObject(OddsDocument.class);

        // build each list item
        holder.constraintLayout.setSelected(position == selectedPos);
        holder.name.setText(game.a_id.equals(FirebaseAuth.getInstance().getUid()) ? game.b_name : game.a_name);
        holder.oddsMsg.setText(context.getString(R.string.odds_of_to_msg, game.odds, game.message));
        FirebaseFirestore.getInstance().collection("users") // get the opponents' image
                .document(game.a_id.equals(FirebaseAuth.getInstance().getUid()) ? game.b_id : game.a_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                new DownloadImageTask(holder.avatar).execute(documentSnapshot.get("imgUri").toString());
            }
        });
    }


    public void setGames(List<DocumentSnapshot> userList) {
        games.clear();
        games.addAll(userList);
        this.notifyDataSetChanged();
    }

    public DocumentSnapshot getGame() {
        if (selectedPos != RecyclerView.NO_POSITION) return games.get(selectedPos);
        else return null;
    }

    @Override
    public int getItemCount() {
        if (games != null) {
            return games.size();
        } else {
            return 0;
        }
    }

    class GameReqViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout constraintLayout;
        public ImageView avatar;
        public TextView name;
        public TextView oddsMsg;

        public GameReqViewHolder(View itemView) {
            super(itemView);

            this.constraintLayout = itemView.findViewById(R.id.oddsreq_constraint_layout);
            this.avatar = itemView.findViewById(R.id.request_odds_user_img);
            this.name = itemView.findViewById(R.id.request_odds_user);
            this.oddsMsg = itemView.findViewById(R.id.request_odds_msg);
            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // if the list item is clicked...
                    selectedPos = getAdapterPosition();

                    FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                    Fragment fragment = InGameFragment.newInstance( // Create a new InGameFragment with the relevant oddsId and creator
                            games.get(selectedPos).getId(),
                            games.get(selectedPos).get("a_id").equals(FirebaseAuth.getInstance().getUid())
                    );

                    // Switch over the fragments.
                    fragmentManager.beginTransaction().replace(R.id.frag_content, fragment).addToBackStack("").commit();
                }
            });
        }
    }
}


