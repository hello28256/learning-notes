package com.learning.notes.service.system;

import com.learning.notes.entity.system.MentalModel;
import com.learning.notes.repository.system.MentalModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentalModelService {

    private final MentalModelRepository mentalModelRepository;

    public List<MentalModel> getUserModels(Long userId) {
        return mentalModelRepository.findByUserId(userId);
    }

    public List<MentalModel> getActiveModels(Long userId) {
        return mentalModelRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public MentalModel getModelById(Long id, Long userId) {
        return mentalModelRepository.findById(id)
                .filter(m -> m.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("认知模型不存在"));
    }

    @Transactional
    public MentalModel createModel(MentalModel model, Long userId) {
        model.setUserId(userId);
        model.setUsageCount(0);
        model.setMasteryLevel(1);
        model.setIsActive(true);
        return mentalModelRepository.save(model);
    }

    @Transactional
    public MentalModel updateModel(Long id, MentalModel model, Long userId) {
        MentalModel existing = getModelById(id, userId);
        existing.setModelName(model.getModelName());
        existing.setDefinition(model.getDefinition());
        existing.setApplicationScenarios(model.getApplicationScenarios());
        existing.setRealCases(model.getRealCases());
        existing.setRelatedReadings(model.getRelatedReadings());
        existing.setMasteryLevel(model.getMasteryLevel());
        existing.setIsActive(model.getIsActive());
        return mentalModelRepository.save(existing);
    }

    @Transactional
    public MentalModel incrementUsage(Long id, Long userId) {
        MentalModel model = getModelById(id, userId);
        model.setUsageCount(model.getUsageCount() + 1);
        return mentalModelRepository.save(model);
    }

    @Transactional
    public void deleteModel(Long id, Long userId) {
        MentalModel model = getModelById(id, userId);
        mentalModelRepository.delete(model);
    }
}
