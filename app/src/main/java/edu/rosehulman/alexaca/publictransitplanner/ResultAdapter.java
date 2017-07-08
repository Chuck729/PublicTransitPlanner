package edu.rosehulman.alexaca.publictransitplanner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alexaca on 7/8/2017.
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    //TODO make this a result model object
    private ArrayList<String> results;

    public ResultAdapter() {
        results = new ArrayList<>();
        results.add("Sample Result");
    }
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ResultAdapter.ViewHolder holder, int position) {
        String text = results.get(position);
        holder.resultTV.setText(text);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView resultTV;
        public ViewHolder(View itemView) {
            super(itemView);
            resultTV = (TextView)itemView.findViewById(R.id.result_text);
        }
    }
}
