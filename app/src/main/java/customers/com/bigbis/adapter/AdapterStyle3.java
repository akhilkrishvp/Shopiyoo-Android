package customers.com.bigbis.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import customers.com.bigbis.R;
import customers.com.bigbis.activity.ProductDetailActivity;
import customers.com.bigbis.helper.AppController;
import customers.com.bigbis.helper.Constant;
import customers.com.bigbis.helper.DatabaseHelper;
import customers.com.bigbis.model.PriceVariation;
import customers.com.bigbis.model.Product;

public class AdapterStyle3 extends RecyclerView.Adapter<AdapterStyle3.VideoHolder> {

    public ArrayList<Product> productList;

    public Activity activity;
    public int itemResource;
    SpannableString spannableString;
    ImageLoader netImageLoader = AppController.getInstance().getImageLoader();
    ArrayList<PriceVariation> priceVariationslist =  new ArrayList<>();
    DatabaseHelper databaseHelper;
    public AdapterStyle3(Activity activity, ArrayList<Product> productList, int itemResource) {
        this.activity = activity;
        this.productList = productList;
        this.itemResource = itemResource;
        databaseHelper = new DatabaseHelper(activity);

    }

    public class VideoHolder extends RecyclerView.ViewHolder {

        public NetworkImageView thumbnail;
        public TextView v_title,txtOriginalPrice,txtPrice,txtqty;
        ImageView btnminusqty,btnaddqty;
        public ConstraintLayout parent;

        public VideoHolder(View itemView) {
            super(itemView);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
            v_title = (TextView) itemView.findViewById(R.id.title);
            txtOriginalPrice = (TextView) itemView.findViewById(R.id.txtOriginalPrice);
            txtPrice = (TextView) itemView.findViewById(R.id.txtPrice);
            txtqty = (TextView) itemView.findViewById(R.id.txtqty);
            parent = (ConstraintLayout) itemView.findViewById(R.id.parent);
            btnminusqty = (ImageView) itemView.findViewById(R.id.btnminusqty);
            btnaddqty = (ImageView) itemView.findViewById(R.id.btnaddqty);

        }


    }


    @Override
    public int getItemCount() {
        int product;
        if (productList.size() > 6) {
            product = 6;
        } else {
            product = productList.size();
        }
        return product;
    }

    @Override
    public void onBindViewHolder(final VideoHolder holder, final int position) {
        final Product product = productList.get(position);
        priceVariationslist = product.getPriceVariations();
        final ArrayList<PriceVariation> priceVariations = product.getPriceVariations();
        product.setGlobalStock(Double.parseDouble(priceVariations.get(0).getStock()));
        holder.thumbnail.setImageUrl(product.getImage(), netImageLoader);
        holder.v_title.setText(product.getName());
        setPrice(holder.txtOriginalPrice,holder.txtPrice,holder.txtqty,priceVariationslist,product.getId());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity, ProductDetailActivity.class).putExtra("vpos", 0).putExtra("model", product));

            }
        });
        holder.btnaddqty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(priceVariationslist.size() > 0){
                    addQty(product,holder,priceVariationslist.get(0));
                }
            }
        });
        holder.btnminusqty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(priceVariationslist.size() > 0){
                    reduceQty(product,holder,priceVariationslist.get(0));
                }
            }
        });

    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemResource, parent, false);
        return new VideoHolder(view);
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    private void setPrice(TextView originalPrice,TextView price,TextView txtQty,ArrayList<PriceVariation> priceVariationslist,String id){
        if (priceVariationslist.size() == 1) {
            PriceVariation priceVariation = priceVariationslist.get(0);
            price.setText(activity.getString(R.string.rs) + Constant.SETTING_CURRENCY_SYMBOL + priceVariation.getProductPrice());
            if (priceVariation.getDiscounted_price().equals("0") || priceVariation.getDiscounted_price().equals("")) {
                originalPrice.setText("");
            }
            else {
                spannableString = new SpannableString(activity.getString(R.string.rs) + Constant.SETTING_CURRENCY_SYMBOL + priceVariation.getPrice());
                spannableString.setSpan(new StrikethroughSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                originalPrice.setText(spannableString);
            }
            txtQty.setText(databaseHelper.CheckOrderExists(priceVariation.getId(), id));
        }

    }

    private void addQty(final Product product, final VideoHolder holder, final PriceVariation extra){
        double addQty = Double.parseDouble(databaseHelper.CheckOrderExists(extra.getId(), product.getId())) ;
        Log.e("Max Qty : ", product.getMaxPurchaseQty());
        Log.e("Add Qty : ", String.valueOf(addQty));

        if (product.getMaxPurchaseQty().equals("null")) {
            if (extra.getType().equals("loose")) {
                String measurement = extra.getMeasurement_unit_name();
                if (measurement.equals("kg") || measurement.equals("ltr") || measurement.equals("gm") || measurement.equals("ml")) {
                    double totalKg;
                    if (measurement.equals("kg") || measurement.equals("ltr"))
                        totalKg = (Integer.parseInt(extra.getMeasurement()) * 1000);
                    else
                        totalKg = (Integer.parseInt(extra.getMeasurement()));
                    double cartKg = ((databaseHelper.getTotalKG(product.getId()) + totalKg) / 1000);


                    if (cartKg <= product.getGlobalStock()) {
                        holder.txtqty.setText(databaseHelper.AddUpdateOrder(extra.getId(), product.getId(), true, activity, false, Double.parseDouble(extra.getProductPrice()), extra.getMeasurement() + extra.getMeasurement_unit_name() + "==" + product.getName() + "==" + extra.getProductPrice()).split("=")[0]);


                    } else {
                        Toast.makeText(activity, activity.getResources().getString(R.string.kg_limit), Toast.LENGTH_LONG).show();
                    }

                } else {
                    RegularCartAdd(product, holder, extra);
                }
            } else {
                RegularCartAdd(product, holder, extra);
            }
        } else if (!product.getMaxPurchaseQty().equals("null")) {
            if (Double.parseDouble(product.getMaxPurchaseQty()) != addQty) {
                if (extra.getType().equals("loose")) {
                    String measurement = extra.getMeasurement_unit_name();
                    if (measurement.equals("kg") || measurement.equals("ltr") || measurement.equals("gm") || measurement.equals("ml")) {
                        double totalKg;
                        if (measurement.equals("kg") || measurement.equals("ltr"))
                            totalKg = (Integer.parseInt(extra.getMeasurement()) * 1000);
                        else
                            totalKg = (Integer.parseInt(extra.getMeasurement()));
                        double cartKg = ((databaseHelper.getTotalKG(product.getId()) + totalKg) / 1000);


                        if (cartKg <= product.getGlobalStock()) {
                            holder.txtqty.setText(databaseHelper.AddUpdateOrder(extra.getId(), product.getId(), true, activity, false, Double.parseDouble(extra.getProductPrice()), extra.getMeasurement() + extra.getMeasurement_unit_name() + "==" + product.getName() + "==" + extra.getProductPrice()).split("=")[0]);


                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.kg_limit), Toast.LENGTH_LONG).show();
                        }

                    } else {
                        RegularCartAdd(product, holder, extra);
                    }
                } else {
                    RegularCartAdd(product, holder, extra);
                }
            } else if (Double.parseDouble(product.getMaxPurchaseQty()) == addQty){
                Toast.makeText(activity, "Max Purchase Product limit " + product.getMaxPurchaseQty(), Toast.LENGTH_SHORT).show();
            }
        }

        activity.invalidateOptionsMenu();

    }
    private void reduceQty(final Product product, final VideoHolder holder, final PriceVariation extra){
        holder.txtqty.setText(databaseHelper.AddUpdateOrder(extra.getId(), product.getId(), false, activity, false, Double.parseDouble(extra.getProductPrice()), extra.getMeasurement() + extra.getMeasurement_unit_name() + "==" + product.getName() + "==" + extra.getProductPrice()).split("=")[0]);
        activity.invalidateOptionsMenu();
    }
    public void RegularCartAdd(final Product product, final VideoHolder holder, final PriceVariation extra) {

        if (Double.parseDouble(databaseHelper.CheckOrderExists(extra.getId(), product.getId())) < Double.parseDouble(extra.getStock()))
            holder.txtqty.setText(databaseHelper.AddUpdateOrder(extra.getId(), product.getId(), true, activity, false, Double.parseDouble(extra.getProductPrice()), extra.getMeasurement() + extra.getMeasurement_unit_name() + "==" + product.getName() + "==" + extra.getProductPrice()).split("=")[0]);
        else
            Toast.makeText(activity, activity.getResources().getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
    }
}
