package com.raczkowski.app.history;

import com.raczkowski.app.enums.HistoryMethodType;
import com.raczkowski.app.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLException;
@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    @Transactional
    public void create(AppUser user, Long entityId, String entityType, String payload) {

        try {
            PGobject jsonPayload = new PGobject();
            jsonPayload.setType("jsonb");
            jsonPayload.setValue(payload);

            History history = new History(user, entityId, HistoryMethodType.CREATE, entityType, jsonPayload);
            historyRepository.save(history);
        } catch (SQLException e) {
            throw new RuntimeException("Błąd konwersji JSON na jsonb", e);
        }
    }
}
