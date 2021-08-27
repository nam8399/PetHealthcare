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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private ArrayList<PP1> arrayList;
    private Context context;
    private ListItemClickListener listItemClickListener;


    public PetAdapter(ArrayList<PP1> arrayList, Context context, ListItemClickListener listItemClickListener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listItemClickListener = listItemClickListener;
    }


    @NotNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        PetViewHolder holder = new PetViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PetViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getImage())
                .into(holder.iv_profile);
        holder.tv_name.setText(arrayList.get(position).getName());
        holder.tv_birthday.setText(arrayList.get(position).getBirthday());
        holder.tv_species.setText(arrayList.get(position).getSpecies());
        holder.tv_gender.setText(arrayList.get(position).getGender());
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class PetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView iv_profile;
        TextView tv_name;
        TextView tv_birthday;
        TextView tv_species;
        TextView tv_gender;

        public PetViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.iv_profile = itemView.findViewById(R.id.iv_profile);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_birthday = itemView.findViewById(R.id.tv_birthday);
            this.tv_species = itemView.findViewById(R.id.tv_species);
            this.tv_gender = itemView.findViewById(R.id.tv_gender);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listItemClickListener.listItemClick(getAdapterPosition());
        }
    }

    public interface ListItemClickListener {
        void listItemClick(int position);
    }
}
