package com.project_x.listing.adress.component;

import com.project_x.listing.adress.entity.Lga;
import com.project_x.listing.adress.entity.State;
import com.project_x.listing.adress.repository.LgaRepository;
import com.project_x.listing.adress.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocationSeeder implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final StateRepository stateRepository;
    private final LgaRepository lgaRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (stateRepository.count() > 0 || lgaRepository.count() > 0) {
            return;
        }

        InputStream inputStream =
                new ClassPathResource("data/nigeria-locations.json").getInputStream();

        Map<String, List<String>> data = objectMapper.readValue(
                inputStream,
                new TypeReference<Map<String, List<String>>>() {}
        );

        List<State> statesToSave = new ArrayList<>();

        for (String stateName : data.keySet()) {
            statesToSave.add(
                    State.builder()
                            .name(cleanStateName(stateName))
                            .build()
            );
        }

        List<State> savedStates = stateRepository.saveAll(statesToSave);

        Map<String, State> stateMap = savedStates.stream()
                .collect(Collectors.toMap(State::getName, s -> s));

        List<Lga> lgasToSave = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            String stateName = cleanStateName(entry.getKey());
            State state = stateMap.get(stateName);

            for (String lgaName : entry.getValue()) {
                lgasToSave.add(
                        Lga.builder()
                                .name(cleanLgaName(lgaName))
                                .state(state)
                                .build()
                );
            }
        }

        lgaRepository.saveAll(lgasToSave);
    }

    private String cleanStateName(String stateName) {
        return stateName == null ? null : stateName.trim();
    }

    private String cleanLgaName(String lgaName) {
        if (lgaName == null) {
            return null;
        }

        return lgaName.trim().replace("|", " / ");
    }
}