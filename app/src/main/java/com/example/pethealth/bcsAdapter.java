package com.example.pethealth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pethealth.fragments.PetAccount;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class bcsAdapter extends RecyclerView.Adapter<bcsAdapter.ViewHolder>
{
    private List<bcsItem> bcsItems = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private FirebaseStorage storage;
    private Context context;



    public bcsAdapter(List<bcsItem> bcsItem, List<String> uidList){
        this.bcsItems = bcsItem;
        this.uidList = uidList;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.bcslist,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
    {
        // holder.textViewUser.setText(imageDTOList.get(position).getUserid());
        //holder.textViewTitle.setText(imageDTOList.get(position).getTitle());
        holder.list_bcs.setText(bcsItems.get(position).getBcsReport());
        // holder.imageViewHeart.setImageResource(R.drawable.favorite_border_black_24dp);

        context = holder.itemView.getContext();

    }

    @Override
    public int getItemCount() {
        return bcsItems.size();
    }

    //ViewHolder 클래스
    class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView list_bcs;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            //textViewUser = itemView.findViewById(R.id.item_user);
            // textViewTitle = itemView.findViewById(R.id.item_title); //파라메타 id 찾기
            list_bcs = itemView.findViewById(R.id.list_bcs);
            // imageViewHeart= itemView.findViewById(R.id.item_heart);
        }
    }
}