package nl.quintor.studybits.studybitswallet.exchangeposition;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ch.qos.logback.core.net.SocketConnector;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.studybitswallet.AgentClient;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class ExchangePositionViewModel extends AndroidViewModel {
    private MutableLiveData<List<ExchangePosition>> exchangePositions = new MutableLiveData<>();

    public ExchangePositionViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(List<University> universities) {
        List<ExchangePosition> newExchangePositions =  universities
                .stream()
                .map(AsyncUtil.wrapException(AgentClient::getExchangePositions))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());


        exchangePositions.setValue(newExchangePositions);
    }

    public LiveData<List<ExchangePosition>> getExchangePositions() {
        return exchangePositions;
    }
}
