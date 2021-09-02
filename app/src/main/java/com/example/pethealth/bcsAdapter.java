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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;


public class bcsAdapter extends RecyclerView.Adapter<bcsAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    private OnItemClickListener onItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
private List<ImageDTO> imageDTOList = new ArrayList<>();
private List<String> uidList = new ArrayList<>();
private FirebaseStorage storage;
private Context context;

public bcsAdapter(){}
public bcsAdapter(List<ImageDTO> imageDTOList, List<String> uidList)
        {
        this.imageDTOList = imageDTOList;
        this.uidList = uidList;
        storage = FirebaseStorage.getInstance();
        }

@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_list,parent,false);

        return new ViewHolder(view);
        }

@Override
public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
        {
//        holder.textViewUser.setText(imageDTOList.get(position).getUserid());
      //  holder.textViewTitle.setText(imageDTOList.get(position).getTitle());
//        holder.textViewDesc.setText(imageDTOList.get(position).getDescription());
        //holder.imageViewHeart.setImageResource(R.drawable.favorite_border_black_24dp);

        context = holder.itemView.getContext();
        String url = imageDTOList.get(position).getImageUrl();
        Glide.with(context)
        .load(url)
        //.placeholder(R.drawable.g3)
        .into(holder.imageView);

        }

@Override
public int getItemCount() {
        return imageDTOList.size();
        }

//ViewHolder 클래스
class ViewHolder extends RecyclerView.ViewHolder
{
    public TextView textViewUser;
    public TextView textViewTitle;
    public TextView textViewDesc;
    public ImageView imageView;
    public ImageView imageViewHeart;

    public ViewHolder(@NonNull View itemView)
    {
        super(itemView);
        //textViewUser = itemView.findViewById(R.id.item_user);
        //textViewTitle = itemView.findViewById(R.id.item_title); //파라메타 id 찾기
        //textViewDesc = itemView.findViewById(R.id.item_desc);
        imageView = itemView.findViewById(R.id.list_item);
        //imageViewHeart= itemView.findViewById(R.id.item_heart);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(view, pos);
                    }
                }
            }
        });
    }
}
}