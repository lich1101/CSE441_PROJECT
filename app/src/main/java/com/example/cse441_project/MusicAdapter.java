package com.example.cse441_project;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> implements Filterable {

    public ArrayList<AudioModel> songsList;
    public ArrayList<AudioModel> songsListAll;
    Context context;

    public MusicAdapter(ArrayList<AudioModel> songsList, Context context){
        this.songsList = songsList;
        this.context = context;
        this.songsListAll = new ArrayList<>(songsList);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder , int position) {
        AudioModel SongData = songsList.get(position);
        holder.getAdapterPosition();
        holder.titleTextView.setText(SongData.getTitle());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMediaPlayer.getInstance().reset();
                MyMediaPlayer.currentIndex = holder.getAdapterPosition();
                Intent intent = new Intent(context, MusicPlayerActivity.class);
                intent.putExtra("LIST",songsList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {

        return songsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

    TextView titleTextView;
    ImageView iconView;

        public ViewHolder( View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_view);
            iconView = itemView.findViewById(R.id.icon_view);
        }
    }
    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence){
                String strSearch = charSequence.toString();
                if(strSearch.isEmpty()){
                    songsList = songsListAll;
                } else {
                    ArrayList<AudioModel> list = new ArrayList<>();
                    for(AudioModel audioModel : songsListAll){
                        if(audioModel.getTitle().toLowerCase().contains(strSearch.toLowerCase())){
                            list.add(audioModel);
                        }

                    }
                    songsList = (ArrayList<AudioModel>) list;

                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = songsList;
                return filterResults;
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                songsList = (ArrayList<AudioModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
