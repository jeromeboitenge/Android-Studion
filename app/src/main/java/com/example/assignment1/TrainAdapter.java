package com.example.assignment1;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.TrainViewHolder> {

    private List<Train> trainList;

    public TrainAdapter(List<Train> trainList) {
        this.trainList = trainList;
    }

    @NonNull
    @Override
    public TrainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the single item layout we created in Step 5
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_train, parent, false);
        return new TrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainViewHolder holder, int position) {
        Train train = trainList.get(position);

        // Bind data to views
        holder.tvTrainName.setText(train.getName());
        holder.tvTrainNumber.setText("(" + train.getNumber() + ")");
        holder.tvRoute.setText(train.getSourceCode() + " → " + train.getDestCode());
        holder.tvTimes.setText(train.getDepartureTime() + " - " + train.getArrivalTime());
        holder.tvDuration.setText("|  Travel Time: " + train.getDuration());
    }

    @Override
    public int getItemCount() {
        return trainList.size();
    }

    // Inner class to hold references to views within the item layout
    public static class TrainViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrainName, tvTrainNumber, tvRoute, tvTimes, tvDuration;

        public TrainViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrainName = itemView.findViewById(R.id.tvTrainName);
            tvTrainNumber = itemView.findViewById(R.id.tvTrainNumber);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
