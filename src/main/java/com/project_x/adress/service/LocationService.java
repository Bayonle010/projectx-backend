package com.project_x.listing.adress.service;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.listing.adress.dto.LgaResponse;
import com.project_x.listing.adress.dto.StateResponse;
import com.project_x.listing.adress.entity.Lga;
import com.project_x.listing.adress.entity.State;
import com.project_x.listing.adress.repository.LgaRepository;
import com.project_x.listing.adress.repository.StateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LocationService {
    private final StateRepository stateRepository;
    private final LgaRepository lgaRepository;

    public LocationService(StateRepository stateRepository, LgaRepository lgaRepository) {
        this.stateRepository = stateRepository;
        this.lgaRepository = lgaRepository;
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

    public List<LgaResponse> getLgasByStateId(UUID stateId) {
        validateStateExists(stateId);

        return lgaRepository.findByStateIdOrderByNameAsc(stateId)
                .stream()
                .map(lga -> new LgaResponse(
                        lga.getId(),
                        lga.getName()
                ))
                .toList();
    }

    public State findState(UUID stateId){
        return stateRepository.findById(stateId).orElseThrow(()-> new ResourceNotFoundException("state not found"));
    }

    public Lga findLga(UUID lgaId){
        return lgaRepository.findById(lgaId).orElseThrow(()-> new ResourceNotFoundException("Lga not found"));
    }


    private void validateStateExists(UUID stateId) {
        stateRepository.findById(stateId)
                .orElseThrow(() -> new ResourceNotFoundException("State not found"));
    }
}
