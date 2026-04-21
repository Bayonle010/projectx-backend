package com.project_x.adress.repository;

import com.project_x.adress.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import java.util.List;
public interface StateRepository extends JpaRepository<State, UUID> {
    List <State> findAllByOrderByNameAsc();
}
