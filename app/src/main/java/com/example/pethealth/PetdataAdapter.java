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

public class PetdataAdapter extends RecyclerView.Adapter<PetdataAdapter.ViewHolder>
{
    private List<PetAccount> petAccountList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private FirebaseStorage storage;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    private PetdataAdapter.OnItemClickListener onItemClickListener = null;

    public void setOnItemClickListener(PetdataAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public PetdataAdapter(List<PetAccount> petAccountList, List<String> uidList){
        this.petAccountList = petAccountList;
        this.uidList = uidList;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_petdata,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
    {
       // holder.textViewUser.setText(imageDTOList.get(position).getUserid());
        //holder.textViewTitle.setText(imageDTOList.get(position).getTitle());
        holder.et_bithday.setText(petAccountList.get(position).getBirthday());
        holder.et_species.setText(petAccountList.get(position).getSpecies());
        holder.et_gender.setText(petAccountList.get(position).getGender());
        holder.et_name.setText(petAccountList.get(position).getName());
      // holder.imageViewHeart.setImageResource(R.drawable.favorite_border_black_24dp);

        context = holder.itemView.getContext();
        String url = petAccountList.get(position).getImage();
        Glide.with(context)
                .load(url)
                //.placeholder(R.drawable.g3)
                .into(holder.et_images);


    }

    @Override
    public int getItemCount() {
        return petAccountList.size();
    }

    //ViewHolder 클래스
    class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView et_name, et_bithday, et_gender, et_species;
        public ImageView et_images;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            //textViewUser = itemView.findViewById(R.id.item_user);
           // textViewTitle = itemView.findViewById(R.id.item_title); //파라메타 id 찾기
            et_name = itemView.findViewById(R.id.tv_name2);
            et_bithday = itemView.findViewById(R.id.tv_birthday2);
            et_gender = itemView.findViewById(R.id.tv_gender2);
            et_species = itemView.findViewById(R.id.tv_species2);
            et_images = itemView.findViewById(R.id.tv_image);
           // imageViewHeart= itemView.findViewById(R.id.item_heart);
        }
    }
}