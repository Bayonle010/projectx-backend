package com.project_x.listing.adress.repository;

import com.project_x.listing.adress.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StateRepository extends JpaRepository<State, UUID> {
}
