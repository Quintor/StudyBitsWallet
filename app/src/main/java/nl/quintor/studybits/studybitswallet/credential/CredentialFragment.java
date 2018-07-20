package nl.quintor.studybits.studybitswallet.credential;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.Credential;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.dto.CredentialWithRequest;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.studybitswallet.AgentClient;
import nl.quintor.studybits.studybitswallet.R;
import nl.quintor.studybits.studybitswallet.WalletActivity;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CredentialFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    protected IndyPool indyPool;
    protected IndyWallet studentWallet;

    public static final String STUDENT_DID = "Xepuw1Y1k9DpvoSvZaoVJr";

    @Override
    public void onResume() {
        super.onResume();
        initWallet();
    }

    private void initWallet() {
        try {
            if (indyPool == null || studentWallet == null) {
                indyPool = new IndyPool("testPool");
                studentWallet = IndyWallet.open(indyPool, "student_wallet", STUDENT_DID);
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
    public CredentialFragment() {
    }

    @SuppressWarnings("unused")
    public static CredentialFragment newInstance(int columnCount) {
        CredentialFragment fragment = new CredentialFragment();
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
        View view = inflater.inflate(R.layout.fragment_credential_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }


            List<University> endpoints = AppDatabase.getInstance(context).universityDao().getStatic();

            final CredentialOfferViewModel credentialOfferViewModel = ViewModelProviders.of(this)
                    .get(CredentialOfferViewModel.class);

            initWallet();

            credentialOfferViewModel.initCredentialOffers(endpoints, studentWallet);

            credentialOfferViewModel.getCredentials().observe(this, credentials -> {
                List<CredentialOrOffer> credentialOrOffers = credentials.stream()
                        .map(credential -> {
                            University university = AppDatabase.getInstance(context).universityDao().getByDid(credential.getIssuerDid());
                            return CredentialOrOffer.fromCredential(university.getName(), credential);
                        }).collect(Collectors.toList());

                if (credentialOfferViewModel.getCredentialOffers().getValue() != null) {
                    credentialOrOffers.addAll(credentialOfferViewModel.getCredentialOffers().getValue());
                }

                ((CredentialRecyclerViewAdapter) recyclerView.getAdapter()).setDataset(credentialOrOffers);
            });

            credentialOfferViewModel.getCredentialOffers().observe(this, credentialOffers ->
            {
                List<CredentialOrOffer> credentialOrOffers = new ArrayList<>(credentialOffers);
                if (credentialOfferViewModel.getCredentials().getValue() != null) {
                    List<CredentialOrOffer> credentials = credentialOfferViewModel.getCredentials().getValue().stream()
                            .map(credential -> {
                                University university = AppDatabase.getInstance(context).universityDao().getByDid(credential.getIssuerDid());
                                return CredentialOrOffer.fromCredential(university.getName(), credential);
                            }).collect(Collectors.toList());
                    credentialOrOffers.addAll(credentials);
                }

                Log.d("STUDYBITS", "Setting credential offers adapter");
                recyclerView.setAdapter(new CredentialRecyclerViewAdapter(credentialOrOffers, credentialOrOffer -> {
                    if (credentialOrOffer.getCredentialOffer() != null) {
                        acceptCredentialOffer(credentialOrOffer.getCredentialOffer());

                    }
                    mListener.onListFragmentInteraction(credentialOrOffer);

                    credentialOfferViewModel.initCredentialOffers(endpoints, studentWallet);
                }));
            });


        }
        return view;
    }

    public void acceptCredentialOffer(CredentialOffer credentialOffer) {
        try {
            Log.d("STUDYBITS", "Accepting credential offer");

            initWallet();

            Prover studentProver = new Prover(studentWallet, WalletActivity.STUDENT_SECRET_NAME);

            AuthcryptedMessage authcryptedCredentialRequest = studentProver.createCredentialRequest(credentialOffer)
                    .thenCompose(AsyncUtil.wrapException(studentProver::authEncrypt)).get();
            MessageEnvelope credentialRequestEnvelope = new MessageEnvelope(authcryptedCredentialRequest.getDid(), MessageEnvelope.MessageType.CREDENTIAL_REQUEST,
                    new TextNode(new String(Base64.encode(authcryptedCredentialRequest.getMessage(), Base64.NO_WRAP), Charset.forName("utf8"))));

            University university = AppDatabase.getInstance(getContext()).universityDao().getByDid(credentialOffer.getTheirDid());

            MessageEnvelope credentialEnvelope = AgentClient.postAndReturnMessage(university.getEndpoint(), credentialRequestEnvelope);

            AuthcryptedMessage authcryptedCredential = new AuthcryptedMessage(Base64.decode(credentialEnvelope.getMessage().asText(), Base64.NO_WRAP), credentialEnvelope.getId());

            Credential credential = studentProver.authDecrypt(authcryptedCredential, CredentialWithRequest.class).get().getCredential();

            AppDatabase.getInstance(getContext()).credentialDao().insert(
                    new nl.quintor.studybits.studybitswallet.room.entity.Credential(credential.getCredDefId(), university.getTheirDid(), credential.getValues().toString()));
            Log.d("STUDYBITS", "Accepted credential offer");
        }
        catch (Exception e) {
            Log.e("STUDYBITS", "Exception when accepting credential offer" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
        void onListFragmentInteraction(CredentialOrOffer credentialOffer);
    }

    public IndyWallet getStudentWallet() {
        return studentWallet;
    }
}
