package com.mostafazaghloul.task2.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mostafazaghloul.task2.R;
import com.mostafazaghloul.task2.model.repodata.data;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<data> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView Txttitle,Txtowner,TxtDescription;
        LinearLayout linearLayout;
        public MyViewHolder(View v) {
            super(v);
            linearLayout = (LinearLayout)v.findViewById(R.id.parent);
            Txttitle = (TextView)v.findViewById(R.id.repoTitle);
            Txtowner = (TextView)v.findViewById(R.id.owner);
            TxtDescription = (TextView)v.findViewById(R.id.description);
        }
    }
    private LayoutInflater mInflater;
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context mContext, List<data> myDataset) {
        this.mInflater = LayoutInflater.from(mContext);
        mDataset = myDataset;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
//        // create a new view
//        TextView v = (TextView) LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.repoitem, parent, false);
//        MyViewHolder vh = new MyViewHolder(v);
//        return vh;
        View view = mInflater.inflate(R.layout.repoitem, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.Txttitle.setText(mDataset.get(position).getTitle());
        holder.Txtowner.setText(mDataset.get(position).getOwner());
        holder.TxtDescription.setText(mDataset.get(position).getDescription());
        if(!mDataset.get(position).isFork()){
            holder.linearLayout.setBackgroundResource(R.color.green);
        }else{
            holder.linearLayout.setBackgroundResource(R.color.white);

        }
        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String url = mDataset.get(position).getHtml_url();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                v.getContext().startActivity(i);
                return false;
            }

        });

    }
    public void filterList(List<data> filteredList) {
        mDataset = filteredList;
        notifyDataSetChanged();
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
