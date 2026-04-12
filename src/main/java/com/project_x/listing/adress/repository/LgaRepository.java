package com.project_x.listing.adress.repository;

import com.project_x.listing.adress.entity.Lga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LgaRepository extends JpaRepository<Lga, UUID> {
}
