package com.benayoub.superettebozar;

import static android.content.Intent.getIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<DataProduct> dataList;
    private DatabaseReference mDatabase;
    int itemPosition;
    private String receivedData;  // Add a field to store the received data
    private MainActivity mainActivity;  // Add this variable
    public ProductAdapter(List<DataProduct> dataList,String receivedData,MainActivity mainActivity) {
        this.dataList = dataList;
        this.receivedData = receivedData;
        this.mainActivity = mainActivity;  // Initialize the variable
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataProduct data = dataList.get(position);
        holder.textViewName.setText(data.getProductName());
        holder.textViewcodebar.setText(data.getProductCode());
        holder.textViewpreexpired.setText(data.getPreexpireddate());
        holder.textdateexpired.setText(data.getExpereddate());
        loadProductImage(data.getUriPic(), holder.imageView);
        itemPosition = holder.getAdapterPosition();
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.more.getContext(), view);
            }
        });
        }

    private void loadProductImage(String uri, ImageView imageView) {
        // Construct the full download URL
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        if (uri!=null){
            storageReference.child(uri).getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                // Load the image using Glide
                Glide.with(imageView.getContext())
                        .load(downloadUrl)
                        .into(imageView);
            }).addOnFailureListener(exception -> {
                // Handle failure
                // This could happen if the image does not exi*st or there is an issue with Firebase Storage
            });
        }else {
            Glide.with(imageView.getContext())
                    .load(R.drawable.product_image)
                    .into(imageView);
        }

    }

    public void deleteItem(int position) {
        dataList.remove(position);
        mainActivity.adapter.notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewcodebar;
        TextView textViewpreexpired;
        TextView textdateexpired;
        ImageView imageView;
        ImageButton more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewname);
            textViewcodebar = itemView.findViewById(R.id.textViewcodebar);
            textViewpreexpired = itemView.findViewById(R.id.textViewpreexired);
            textdateexpired=itemView.findViewById(R.id.date_id);
            imageView=itemView.findViewById(R.id.imageView3);
            more=itemView.findViewById(R.id.btnMore);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Call the deleteItem method when the button is clicked
                        deleteItem(position);
                    }
                }
            });
        }
    }
    public void updateData(List<DataProduct> newData) {
        dataList.clear();
        dataList.addAll(newData);
    }


    private void showPopupMenu(Context context, View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.manage_data, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                // Handle menu item clicks here
                int id = item.getItemId();
                if (id == R.id.button_done) {
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            deleteData();

                        }
                    });
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }
    private void deleteData() {

                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Products");
                Query query = dbref.child(receivedData);

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // remove the value at reference
                        dataSnapshot.getRef().removeValue();
                        mainActivity.adapter.deleteItem(itemPosition);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle onCancelled
                    }
                });


    }


}

