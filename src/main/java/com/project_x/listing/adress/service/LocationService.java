package com.project_x.listing.adress.service;

import com.project_x.listing.adress.dto.StateResponse;
import com.project_x.listing.adress.repository.StateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    private final StateRepository stateRepository;

    public LocationService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public List<StateResponse> getStates() {
        return stateRepository.findAllByOrderByNameAsc()
                .stream()
                .map(state -> new StateResponse(
                        state.getId(),
                        state.getName()
                ))
                .toList();
    }
}
