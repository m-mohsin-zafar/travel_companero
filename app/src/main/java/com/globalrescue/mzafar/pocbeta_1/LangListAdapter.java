package com.globalrescue.mzafar.pocbeta_1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.globalrescue.mzafar.pocbeta_1.Models.LanguageListModel;

import java.util.ArrayList;
import java.util.List;

public class LangListAdapter extends RecyclerView.Adapter<LangListAdapter.LangListViewHolder>{

    private static final String TAG = LangListAdapter.class.getSimpleName();
    LanguageSelection languageSelection;
//    private ArrayList<String> mLangName = new ArrayList<>();
//    private ArrayList<String> mLangCountry = new ArrayList<>();
    private List<LanguageListModel> mLangModelList = new ArrayList<>();
    private Context mContext;

    public LangListAdapter(List<LanguageListModel> mLangModelList, Context mContext) {
        this.mLangModelList = mLangModelList;
        this.mContext = mContext;
    }
    /**
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new LangListViewHolder that holds the View for each list item
     */
    @Override
    public LangListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.language_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        LangListViewHolder viewHolder = new LangListViewHolder(view);

        return viewHolder;
    }

    void setOnLanguageSelection(LanguageSelection languageSelection){
        this.languageSelection = languageSelection;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(LangListViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder Called: " + position);
        holder.langName.setText(mLangModelList.get(position).getmLangName());
        holder.langCountry.setText(mLangModelList.get(position).getmLangCountry());
        holder.langlistContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick -> Clicked on: "+ mLangModelList.get(position).getmLangName());
                Toast.makeText(mContext, mLangModelList.get(position).getmLangName()+" Selected", Toast.LENGTH_LONG).show();
                LangListAdapter.this.languageSelection.onLanguageSelected(mLangModelList.get(position));
                // mContext.startActivity(new Intent(mContext, MainActivity.class));

            }
        });
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return mLangModelList.size();
    }

    /**
     * Cache of the children views for a list item.
     */
    class LangListViewHolder extends RecyclerView.ViewHolder {

        //Views that are to be Inserted into ViewHolder
        TextView langName;
        TextView langCountry;
        LinearLayout langlistContainer;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link LangListAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public LangListViewHolder(View itemView) {
            super(itemView);

            langName = itemView.findViewById(R.id.tv_lang_name);
            langCountry = itemView.findViewById(R.id.tv_lang_country);
            langlistContainer = itemView.findViewById(R.id.ll_langlist_container);
        }
    }

    interface LanguageSelection {
        void onLanguageSelected(LanguageListModel language);
    }
}
