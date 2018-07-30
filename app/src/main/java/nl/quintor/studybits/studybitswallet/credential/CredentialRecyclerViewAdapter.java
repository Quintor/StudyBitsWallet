package nl.quintor.studybits.studybitswallet.credential;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.stream.Collectors;

import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.studybitswallet.R;
import nl.quintor.studybits.studybitswallet.credential.CredentialFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CredentialOffer} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class CredentialRecyclerViewAdapter extends RecyclerView.Adapter<CredentialRecyclerViewAdapter.ViewHolder> {

    private List<CredentialOrOffer> credentials;
    private final OnListFragmentInteractionListener credentialInteractionListener;

    public CredentialRecyclerViewAdapter(List<CredentialOrOffer> items, OnListFragmentInteractionListener listener) {
        credentials = items;
        credentialInteractionListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_credential, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.credentialOffer = credentials.get(position);
        holder.mIdView.setText(credentials.get(position).getUniversityName());
        if (holder.credentialOffer.getCredential() != null) {
            String text = holder.credentialOffer.getCredential().getAttrs()
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining("\n"));
            holder.mContentView.setText(text);
        }
        else {
            holder.mContentView.setText(holder.credentialOffer.getValue());
        }



        if (holder.credentialOffer.getCredentialOffer() != null) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorCredentialOffer));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != credentialInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    credentialInteractionListener.onListFragmentInteraction(holder.credentialOffer);

                }
            }
        });
    }

    public void setDataset(List<CredentialOrOffer> credentialOrOffers) {
        this.credentials = credentialOrOffers;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return credentials.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public CredentialOrOffer credentialOffer;

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
