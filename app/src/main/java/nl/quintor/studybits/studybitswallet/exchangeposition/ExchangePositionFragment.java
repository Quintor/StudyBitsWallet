package nl.quintor.studybits.studybitswallet.exchangeposition;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.TextNode;

import org.hyperledger.indy.sdk.IndyException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.studybitswallet.AgentClient;
import nl.quintor.studybits.studybitswallet.IndyClient;
import nl.quintor.studybits.studybitswallet.R;
import nl.quintor.studybits.studybitswallet.TestConfiguration;
import nl.quintor.studybits.studybitswallet.WalletActivity;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ExchangePositionFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    protected IndyPool indyPool;
    protected IndyWallet studentWallet;
    protected MessageEnvelopeCodec studentCodec;

    @Override
    public void onResume() {
        super.onResume();
        initWallet();
    }

    private void initWallet() {
        try {
            if (indyPool == null || studentWallet == null) {
                indyPool = new IndyPool("testPool");
                studentWallet = IndyWallet.open(indyPool, "student_wallet", TestConfiguration.STUDENT_SEED, TestConfiguration.STUDENT_DID);
                studentCodec = new MessageEnvelopeCodec(studentWallet);
            }
        } catch (IndyException | ExecutionException | InterruptedException | JsonProcessingException e) {
            Log.e("STUDYBITS", "Exception on resume " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            studentWallet.close();
            indyPool.close();
        } catch (Exception e) {
            Log.e("STUDYBITS", "Exception on pause" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ExchangePositionFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ExchangePositionFragment newInstance(int columnCount) {
        ExchangePositionFragment fragment = new ExchangePositionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchangeposition_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            final ExchangePositionViewModel exchangePositionViewModel = ViewModelProviders.of(this)
                    .get(ExchangePositionViewModel.class);
            initWallet();
            refreshPositions();

            exchangePositionViewModel.getExchangePositions().observe(this, exchangePositions -> {
                recyclerView.setAdapter(new MyExchangePositionRecyclerViewAdapter(exchangePositions, exchangePosition -> {
                    try {
                        IndyClient indyClient = new IndyClient(studentWallet, AppDatabase.getInstance(getContext()));
                        ProofRequest proofRequest = exchangePosition.getProofRequest();

                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setMessage("Requested attributes: " + proofRequest.getRequestedAttributes().entrySet().stream()
                                .map(entry -> entry.getValue().getName()).collect(Collectors.joining(", ")))
                                .setPositiveButton("Send", (dialogInterface, i) -> {
                                    try {
                                        MessageEnvelope proofEnvelope = indyClient.fulfillExchangePosition(exchangePosition);
                                        new AgentClient(exchangePosition.getUniversity().getEndpoint()).postMessage(proofEnvelope);
                                    } catch (Exception e) {
                                        Log.e("STUDYBITS", "Exception while fulfilling exchange position (sending)");
                                        e.printStackTrace();
                                    }

                                    refreshPositions();
                                    Snackbar.make(view, "You're going abroad!", Snackbar.LENGTH_SHORT).show();

                                })
                                .create();

                        alertDialog.show();
                    } catch (Exception e) {
                        Log.e("STUDYBITS", "Exception while fulfilling exchange position");
                        e.printStackTrace();
                    }
                    if (mListener != null) {
                        mListener.onListFragmentInteraction(exchangePosition);
                    }
                }));
            });
        }

        return view;
    }

    private void refreshPositions() {
        final ExchangePositionViewModel exchangePositionViewModel = ViewModelProviders.of(this)
                .get(ExchangePositionViewModel.class);

        List<University> universityList = AppDatabase.getInstance(getContext()).universityDao().getStatic();
        exchangePositionViewModel.init(universityList, studentCodec);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ExchangePosition exchangePosition);
    }
}
