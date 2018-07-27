package nl.quintor.studybits.studybitswallet.exchangeposition;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import nl.quintor.studybits.studybitswallet.R;
import nl.quintor.studybits.studybitswallet.exchangeposition.ExchangePositionFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ExchangePosition} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyExchangePositionRecyclerViewAdapter extends RecyclerView.Adapter<MyExchangePositionRecyclerViewAdapter.ViewHolder> {

    private final List<ExchangePosition> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyExchangePositionRecyclerViewAdapter(List<ExchangePosition> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_exchangeposition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getUniversity().getName());
        holder.mContentView.setText(mValues.get(position).getName());
        if (!holder.mItem.isFulfilled()) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorCredentialOffer));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public ExchangePosition mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
