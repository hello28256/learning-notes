package com.learning.notes.repository.system;

import com.learning.notes.entity.system.MentalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentalModelRepository extends JpaRepository<MentalModel, Long> {
    List<MentalModel> findByUserId(Long userId);
    List<MentalModel> findByUserIdAndIsActiveTrue(Long userId);
}
